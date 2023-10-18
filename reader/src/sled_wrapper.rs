use log::warn;
use serde::{Deserialize, Serialize};
use sled::Db;
use std::fmt::Debug;
use std::marker::PhantomData;
use std::ops::Deref;

pub struct DiskCache<K, V> {
    sled: Db,
    key_type: PhantomData<K>,
    value_type: PhantomData<V>,
}

impl<K: Deref<Target = String>, V: Serialize + Debug + for<'a> Deserialize<'a>> DiskCache<K, V> {
    pub fn new(name: &str) -> Self {
        Self {
            sled: sled::open(format!("target/{}_db", name)).unwrap(),
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
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::types::{Channel, ChannelId, ChannelName, PlaylistId};
    use uuid::Uuid;

    #[test]
    fn test_cache() {
        let cache: DiskCache<ChannelId, Channel> = DiskCache::new("test");
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
}
