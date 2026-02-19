use std::collections::HashSet;
use std::fmt::Debug;
use std::fs;
use std::marker::PhantomData;
use std::ops::Deref;
use std::path::PathBuf;
use std::sync::Arc;

use log::warn;
use serde::{Deserialize, Serialize};
use uuid::Uuid;

use crate::types::{Channel, ChannelId, ChannelName, PlaylistId};

#[derive(Clone)]
pub struct DiskCache<K, V> {
    base_path: PathBuf,
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
            Mode::Prod => format!("data/cache/{}", name),
            Mode::Test => format!("target/data/cache-{}/{}", Uuid::new_v4(), name),
        };
        let base_path = PathBuf::from(&path_str);

        if let Err(e) = fs::create_dir_all(&base_path) {
            warn!("Failed to create cache directory {:?}: {:?}", base_path, e);
        }

        Self {
            base_path,
            key_type: PhantomData,
            value_type: PhantomData,
        }
    }

    pub fn get(&self, key: K) -> Option<V> {
        let filename = PathBuf::from(format!("{}.json.zst", key.deref()));
        let path = self.base_path.join(&filename);

        if !path.exists() {
            return None;
        }

        match fs::read(&path) {
            Ok(bytes) => match zstd::stream::decode_all(bytes.as_slice()) {
                Ok(json_bytes) => match serde_json::from_slice(&json_bytes) {
                    Ok(v) => Some(v),
                    Err(e) => {
                        warn!("Failed to deserialize value from {:?}: {:?}", path, e);
                        None
                    }
                },
                Err(e) => {
                    warn!("Failed to decompress value from {:?}: {:?}", path, e);
                    None
                }
            },
            Err(e) => {
                warn!("Failed to read file {:?}: {:?}", path, e);
                None
            }
        }
    }

    pub fn insert(&self, key: K, value: V) {
        let filename = PathBuf::from(format!("{}.json.zst", key.deref()));
        let path = self.base_path.join(filename);

        // Ensure directory exists (lazy creation in case it was deleted)
        if !self.base_path.exists() {
            let _ = fs::create_dir_all(&self.base_path);
        }

        match serde_json::to_vec(&value) {
            Ok(json_bytes) => match zstd::stream::encode_all(&json_bytes[..], 0) {
                Ok(bytes) => {
                    if let Err(e) = fs::write(&path, bytes) {
                        warn!("Failed to write to file {:?}: {:?}", path, e);
                    }
                }
                Err(e) => {
                    warn!("Failed to compress value for {:?}: {:?}", path, e);
                }
            },
            Err(e) => {
                warn!("Failed to serialize value for {:?}: {:?}", path, e);
            }
        }
    }

    pub fn keys(&self) -> impl Iterator<Item = K> {
        let mut keys = Vec::new();
        if let Ok(entries) = fs::read_dir(&self.base_path) {
            for entry in entries.flatten() {
                if let Ok(file_type) = entry.file_type() {
                    if file_type.is_file() {
                        if let Some(filename) = entry.file_name().to_str() {
                            if filename.ends_with(".json.zst") {
                                let key = filename[0..filename.len() - 9].to_string();
                                keys.push(key.into());
                            }
                        }
                    }
                }
            }
        }
        keys.into_iter()
    }

    pub fn values(&self) -> impl Iterator<Item = V> {
        let mut values = Vec::new();
        if let Ok(entries) = fs::read_dir(&self.base_path) {
            for entry in entries.flatten() {
                if let Ok(file_type) = entry.file_type() {
                    if file_type.is_file() {
                        let path = entry.path();
                        if path.to_str().map_or(false, |s| s.ends_with(".json.zst")) {
                            if let Ok(bytes) = fs::read(&path) {
                                if let Ok(json_bytes) = zstd::stream::decode_all(&bytes[..]) {
                                    if let Ok(v) = serde_json::from_slice(&json_bytes) {
                                        values.push(v);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        values.into_iter()
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
