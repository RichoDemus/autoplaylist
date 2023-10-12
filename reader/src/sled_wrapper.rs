use crate::projections::feed_service::Feed;
use log::warn;
use serde::{Deserialize, Serialize};
use sled::Db;
use std::fmt::Debug;

pub struct DiskCache {
    sled: Db,
}

impl DiskCache {
    pub fn new() -> Self {
        Self {
            sled: sled::open("target/sled_db").unwrap(),
        }
    }
    pub fn get<K: AsRef<[u8]>, V: Debug + for<'a> Deserialize<'a>>(&self, key: K) -> Option<V> {
        self.sled
            .get(key)
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

    pub fn insert<K: AsRef<[u8]>, V: Serialize>(&self, key: K, value: V) {
        let _ = self.sled.insert(key, serde_json::to_vec(&value).unwrap());
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::types::{FeedId, FeedName};
    use uuid::Uuid;

    #[test]
    fn test_cache() {
        let cache = DiskCache::new();
        let id = FeedId(Uuid::new_v4().to_string());
        let name = FeedName(Uuid::new_v4().to_string());
        let feed = Feed {
            id: id.clone(),
            name,
        };

        let key: String = id.0.clone();
        let should_be_none: Option<Feed> = cache.get(key);
        assert!(should_be_none.is_none());
        cache.insert(id.clone().0, feed.clone());
        assert_eq!(cache.get::<String, Feed>(id.0), Some(feed));
    }
}
