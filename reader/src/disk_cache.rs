use std::collections::HashSet;
use std::fmt::Debug;
use std::fs;
use std::marker::PhantomData;
use std::ops::Deref;
use std::sync::Arc;

use log::warn;
use redb::{Database, ReadableTable, TableDefinition};
use serde::{Deserialize, Serialize};
use uuid::Uuid;

use crate::types::{Channel, ChannelId, ChannelName, PlaylistId};

#[derive(Clone)]
pub struct DiskCache<K, V> {
    db: Arc<Database>,
    table_name: String,
    key_type: PhantomData<K>,
    value_type: PhantomData<V>,
}

impl<
    K: Deref<Target = String> + From<String> + Debug,
    V: Serialize + Debug + for<'a> Deserialize<'a>,
> DiskCache<K, V>
{
    pub fn new(name: &str, mode: Mode) -> Self {
        let path_str = match mode {
            Mode::Prod => format!("data/redb/{}.redb", name),
            Mode::Test => format!("target/data/redb-{}/{}.redb", Uuid::new_v4(), name),
        };
        let path = std::path::Path::new(&path_str);
        if let Some(parent) = path.parent() {
            fs::create_dir_all(parent).unwrap();
        }

        let db = Database::create(path).unwrap();
        let table_name = name.to_string();

        let write_txn = db.begin_write().unwrap();
        {
            // The table must be opened once to create it.
            let _ = write_txn.open_table(TableDefinition::<&str, &[u8]>::new(&table_name)).unwrap();
        }
        write_txn.commit().unwrap();

        Self {
            db: Arc::new(db),
            table_name,
            key_type: PhantomData,
            value_type: PhantomData,
        }
    }

    pub fn get(&self, key: K) -> Option<V> {
        let read_txn = self.db.begin_read().expect("Failed to begin read transaction");
        let table = read_txn
            .open_table(TableDefinition::<&str, &[u8]>::new(self.table_name.as_str()))
            .expect("Failed to open table");

        match table.get(key.deref().as_str()) {
            Ok(Some(value)) => {
                match serde_json::from_slice(value.value()) {
                    Ok(v) => Some(v),
                    Err(e) => {
                        warn!("Failed to deserialize value from disk: {:?}", e);
                        None
                    }
                }
            }
            Ok(None) => None,
            Err(e) => {
                warn!("Failed to read from disk: {:?}", e);
                None
            }
        }
    }

    pub fn insert(&self, key: K, value: V) {
        let write_txn = self.db.begin_write().unwrap();
        {
            let mut table = write_txn
                .open_table(TableDefinition::<&str, &[u8]>::new(self.table_name.as_str()))
                .unwrap();
            let value_bytes = serde_json::to_vec(&value).unwrap();
            table.insert(key.deref().as_str(), value_bytes.as_slice()).unwrap();
        }
        write_txn.commit().unwrap();
    }

    pub fn keys(&self) -> impl Iterator<Item = K> {
        let read_txn = self.db.begin_read().unwrap();
        let table = read_txn.open_table(TableDefinition::<&str, &[u8]>::new(&self.table_name)).unwrap();

        let collected_keys: Vec<K> = table
            .iter()
            .unwrap()
            .map(|result| {
                let (key, _value) = result.expect("Failed to read key-value pair");
                String::from(key.value()).into()
            })
            .collect();

        collected_keys.into_iter()
    }

    pub fn values(&self) -> impl Iterator<Item = V> {
        let read_txn = self.db.begin_read().unwrap();
        let table = read_txn.open_table(TableDefinition::<&str, &[u8]>::new(&self.table_name)).unwrap();

        let collected_values: Vec<V> = table
            .iter()
            .unwrap()
            .filter_map(|result| {
                let (_key, value) = result.expect("Failed to read key-value pair");
                match serde_json::from_slice(value.value()) {
                    Ok(v) => Some(v),
                    Err(e) => {
                        warn!("Failed to deserialize value from disk: {:?}", e);
                        None
                    }
                }
            })
            .collect();

        collected_values.into_iter()
    }
}

#[derive(Copy, Clone)]
pub enum Mode {
    Prod,
    Test,
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_cache() {
        let cache: DiskCache<ChannelId, Channel> = DiskCache::new("test", Mode::Test);
        let id = ChannelId(Uuid::new_v4().to_string());
        let name = ChannelName(Uuid::new_v4().to_string());
        let playlist = PlaylistId(Uuid::new_v4().to_string());
        let feed = Channel {
            id: id.clone(),
            name,
            playlist,
        };

        assert!(cache.get(id.clone()).is_none());
        cache.insert(id.clone(), feed.clone());
        assert_eq!(cache.get(id), Some(feed));
    }

    #[test]
    fn test_arc() {
        let cache: DiskCache<ChannelId, Channel> = DiskCache::new("test2", Mode::Test);
        let id = ChannelId(Uuid::new_v4().to_string());
        let name = ChannelName(Uuid::new_v4().to_string());
        let playlist = PlaylistId(Uuid::new_v4().to_string());
        let feed = Channel {
            id: id.clone(),
            name,
            playlist,
        };

        let cache1 = Arc::new(cache);
        let cache2 = cache1.clone();
        let cache3 = cache1.clone();

        assert!(cache1.get(id.clone()).is_none());
        cache2.insert(id.clone(), feed.clone());
        assert_eq!(cache3.get(id), Some(feed));
    }

    #[test]
    fn test_clone() {
        let cache: DiskCache<ChannelId, Channel> = DiskCache::new("test3", Mode::Test);
        let id = ChannelId(Uuid::new_v4().to_string());
        let name = ChannelName(Uuid::new_v4().to_string());
        let playlist = PlaylistId(Uuid::new_v4().to_string());
        let feed = Channel {
            id: id.clone(),
            name,
            playlist,
        };

        let cache1 = cache.clone();
        let cache2 = cache.clone();
        let cache3 = cache.clone();

        assert!(cache1.get(id.clone()).is_none());
        cache2.insert(id.clone(), feed.clone());
        assert_eq!(cache3.get(id.clone()), Some(feed));
    }

    #[test]
    fn test_iterate_values() {
        let cache: DiskCache<ChannelName, String> = DiskCache::new("test4", Mode::Test);
        cache.insert(ChannelName("0".into()), "zero".into());
        cache.insert(ChannelName("1".into()), "one".into());
        cache.insert(ChannelName("2".into()), "two".into());

        let mut result = cache.values().map(|s| s.to_uppercase()).collect::<Vec<_>>();
        result.sort();
        let mut expected = vec!["ZERO".to_string(), "ONE".to_string(), "TWO".to_string()];
        expected.sort();
        assert_eq!(result, expected);
    } 

    #[test]
    fn test_iterate_keys() {
        let cache: DiskCache<ChannelName, String> = DiskCache::new("test5", Mode::Test);
        cache.insert(ChannelName("zero".into()), "0".into());
        cache.insert(ChannelName("one".into()), "1".into());
        cache.insert(ChannelName("two".into()), "2".into());

        let result = cache
            .keys()
            .map(|s| s.to_uppercase())
            .collect::<HashSet<_>>();
        assert_eq!(
            result,
            vec!["ZERO".to_string(), "ONE".to_string(), "TWO".to_string()]
                .into_iter()
                .collect::<HashSet<_>>(),
        );
    }
}
