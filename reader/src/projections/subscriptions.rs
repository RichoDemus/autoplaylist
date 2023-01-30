use std::collections::{HashMap, HashSet};
use std::sync::Mutex;

use once_cell::sync::Lazy;

use crate::event::events::Event;
use crate::types::{FeedId, UserId};

static SUBSCRIPTIONS: Lazy<Mutex<HashMap<UserId, HashSet<FeedId>>>> =
    Lazy::new(|| Mutex::new(HashMap::new()));

pub fn process_event(event: &Event) {
    if let Event::UserSubscribedToFeed {
        id: _,
        timestamp: _,
        user_id,
        feed_id,
    } = event
    {
        let u: UserId = user_id.clone();
        let f: FeedId = feed_id.clone();
        SUBSCRIPTIONS
            .lock()
            .unwrap()
            .entry(u)
            .or_default()
            .insert(f);
    }
}

pub fn get_subscribed_feeds(user_id: &UserId) -> HashSet<FeedId> {
    SUBSCRIPTIONS
        .lock()
        .unwrap()
        .get(user_id)
        .unwrap_or(&HashSet::new())
        .clone()
}
