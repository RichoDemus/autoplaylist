use std::collections::{HashMap, HashSet};
use std::sync::Mutex;

use once_cell::sync::Lazy;

use crate::event::events::Event;
use crate::types::{FeedId, ItemId, UserId};

static WATCHED_ITEMS: Lazy<Mutex<HashMap<(UserId, FeedId), HashSet<ItemId>>>> =
    Lazy::new(|| Mutex::new(HashMap::new()));

pub fn process_event(event: &Event) {
    match event {
        Event::UserWatchedItem {
            id: _,
            timestamp: _,
            user_id,
            feed_id,
            item_id,
        } => {
            WATCHED_ITEMS
                .lock()
                .unwrap()
                .entry((user_id.clone(), feed_id.clone()))
                .or_insert(HashSet::new())
                .insert(item_id.clone());
        }
        Event::UserUnwatchedItem {
            id: _,
            timestamp: _,
            user_id,
            feed_id,
            item_id,
        } => {
            WATCHED_ITEMS
                .lock()
                .unwrap()
                .entry((user_id.clone(), feed_id.clone()))
                .or_insert(HashSet::new())
                .remove(item_id);
        }
        _ => {}
    }
}

pub fn get_watched_items(key: &(UserId, FeedId)) -> HashSet<ItemId> {
    WATCHED_ITEMS
        .lock()
        .unwrap()
        .get(key)
        .unwrap_or(&HashSet::new())
        .clone()
}
