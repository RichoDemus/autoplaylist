use std::sync::{Arc, Mutex};

use crate::event::event_store::EventStore;
use crate::projections::feed_service::feed_service::FeedService;
use crate::projections::label_service::LabelService;
use crate::projections::subscriptions::SubscriptionsService;
use crate::projections::user_service::UserService;
use crate::projections::watched_items::WatchedVideosService;
use crate::sled_wrapper::{DiskCache, Mode};
use crate::youtube::youtube_client::YoutubeClient;

pub struct Services {
    pub user_service: Arc<Mutex<UserService>>,
    pub subscriptions_service: Arc<Mutex<SubscriptionsService>>,
    pub feed_service: Arc<Mutex<FeedService>>,
    pub watched_videos_service: Arc<Mutex<WatchedVideosService>>,
    pub label_service: Arc<Mutex<LabelService>>,
}

impl Services {
    pub(crate) fn new(
        youtube_base_url: Option<String>,
        youtube_key: String,
        mode: Mode,
        use_gcs: bool,
    ) -> Self {
        let event_store = Arc::new(Mutex::new(EventStore::new(use_gcs)));
        Self {
            user_service: Arc::new(Mutex::new(UserService::new(event_store.clone()))),
            subscriptions_service: Arc::new(Mutex::new(SubscriptionsService::new(
                event_store.clone(),
            ))),
            watched_videos_service: Arc::new(Mutex::new(WatchedVideosService::new(
                event_store.clone(),
            ))),
            feed_service: Arc::new(Mutex::new(FeedService::new(
                event_store.clone(),
                YoutubeClient::new(youtube_base_url, youtube_key),
                DiskCache::new("channels", mode),
                DiskCache::new("videos", mode),
            ))),
            label_service: Arc::new(Mutex::new(LabelService::new(event_store.clone()))),
        }
    }
}
