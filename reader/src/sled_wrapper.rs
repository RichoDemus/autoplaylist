use std::fmt::Debug;
use std::marker::PhantomData;
use std::ops::Deref;

use log::{error, warn};
use serde::{Deserialize, Serialize};
use sled::Db;
use uuid::Uuid;

#[derive(Clone)]
pub struct DiskCache<K, V> {
    sled: Db,
    key_type: PhantomData<K>,
    value_type: PhantomData<V>,
}

impl<K: Deref<Target = String>, V: Serialize + Debug + for<'a> Deserialize<'a>> DiskCache<K, V> {
    pub fn new(name: &str, mode: Mode) -> Self {
        let path = match mode {
            Mode::Prod => format!("data/db/{}", name),
            Mode::Test => format!("target/data/{}/{}_db", Uuid::new_v4().to_string(), name),
        };
        Self {
            sled: sled::open(path).unwrap(),
            key_type: PhantomData,
            value_type: PhantomData,
        }
    }
    pub fn get(&self, key: K) -> Option<V> {
        self.sled
            .get(key.deref())
            .expect("Failed to read from disk")
            .map(|ivec| {
                let vec = ivec.to_vec();
                let res = serde_json::from_slice(vec.as_slice());
                if res.is_err() {
                    warn!("Failed to read from disk: {:?}", res);
                }

                res.ok()
            })
            .flatten()
    }

    pub fn insert(&self, key: K, value: V) {
        let _ = self
            .sled
            .insert(key.deref(), serde_json::to_vec(&value).unwrap());
    }

    pub fn values(&self) -> impl Iterator<Item = V> {
        self.sled
            .iter()
            .values()
            .flat_map(|value| {
                if let Err(ref e) = value {
                    error!("failed to read from sled: {:?}", e);
                }
                value.ok()
            })
            .map(|ivec| {
                let vec = ivec.to_vec();
                let res = serde_json::from_slice(vec.as_slice());
                if res.is_err() {
                    warn!("Failed to read from disk: {:?}", res);
                }

                res.ok()
            })
            .flatten()
    }
}

#[derive(Copy, Clone)]
pub enum Mode {
    Prod,
    Test,
}

#[cfg(test)]
mod tests {
    use std::sync::Arc;

    use uuid::Uuid;

    use crate::types::{Channel, ChannelId, ChannelName, PlaylistId};

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
        assert_eq!(cache3.get(id), Some(feed));
    }

    #[test]
    fn test_iterate_values() {
        let cache: DiskCache<ChannelName, String> = DiskCache::new("test4", Mode::Test);
        cache.insert(ChannelName("0".into()), "zero".into());
        cache.insert(ChannelName("1".into()), "one".into());
        cache.insert(ChannelName("2".into()), "two".into());

        let result = cache.values().map(|s| s.to_uppercase()).collect::<Vec<_>>();
        assert_eq!(
            result,
            vec!["ZERO".to_string(), "ONE".to_string(), "TWO".to_string()]
        );
    }
}
