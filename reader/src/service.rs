use crate::event::event_store::EventStore;
use crate::projections::feed_service::FeedService;
use crate::projections::label_service::LabelService;
use crate::projections::subscriptions::SubscriptionsService;
use crate::projections::user_service2::UserService2;
use crate::projections::watched_items::WatchedVideosService;
use crate::sled_wrapper::DiskCache;
use crate::youtube::youtube_client::YoutubeClient;
use std::sync::{Arc, Mutex};
use uuid::Uuid;

pub struct Services {
    pub user_service: Arc<Mutex<UserService2>>,
    pub subscriptions_service: Arc<Mutex<SubscriptionsService>>,
    pub feed_service: Arc<Mutex<FeedService>>,
    pub watched_videos_service: Arc<Mutex<WatchedVideosService>>,
    pub label_service: Arc<Mutex<LabelService>>,
}

impl Services {
    pub(crate) fn new(youtube_base_url: Option<String>) -> Self {
        let event_store = Arc::new(EventStore::new());
        Self {
            user_service: Arc::new(Mutex::new(UserService2::new(event_store.clone()))),
            subscriptions_service: Arc::new(Mutex::new(SubscriptionsService::new(
                event_store.clone(),
            ))),
            watched_videos_service: Arc::new(Mutex::new(WatchedVideosService::new(
                event_store.clone(),
            ))),
            feed_service: Arc::new(Mutex::new(FeedService::new(
                event_store.clone(),
                YoutubeClient::new(youtube_base_url, "YT_KEY".to_string()),
                DiskCache::new(format!("{}/channels", Uuid::new_v4().to_string()).as_str()),
                DiskCache::new(format!("{}/videos", Uuid::new_v4().to_string()).as_str()),
            ))),
            label_service: Arc::new(Mutex::new(LabelService::new(event_store.clone()))),
        }
    }
}
