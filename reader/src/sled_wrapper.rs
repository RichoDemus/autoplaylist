use log::warn;
use serde::{Deserialize, Serialize};
use sled::Db;
use std::fmt::Debug;
use std::ops::Deref;

pub struct DiskCache {
    sled: Db,
}

impl DiskCache {
    pub fn new(name: &str) -> Self {
        Self {
            sled: sled::open(format!("target/{}_db", name)).unwrap(),
        }
    }
    pub fn get<K: Deref<Target = String>, V: Debug + for<'a> Deserialize<'a>>(
        &self,
        key: K,
    ) -> Option<V> {
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

    pub fn insert<K: Deref<Target = String>, V: Serialize>(&self, key: K, value: V) {
        let _ = self
            .sled
            .insert(key.deref(), serde_json::to_vec(&value).unwrap());
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::types::{Channel, ChannelId, ChannelName};
    use uuid::Uuid;

    #[test]
    fn test_cache() {
        let cache = DiskCache::new("test");
        let id = ChannelId(Uuid::new_v4().to_string());
        let name = ChannelName(Uuid::new_v4().to_string());
        let feed = Channel {
            id: id.clone(),
            name,
            items: vec![],
        };

        assert!(cache.get::<ChannelId, Channel>(id.clone()).is_none());
        cache.insert(id.clone(), feed.clone());
        assert_eq!(cache.get::<ChannelId, Channel>(id), Some(feed));
    }
}
