use crate::event::event_store::EventStore;
use crate::projections::feed_service::FeedService;
use crate::projections::subscriptions::SubscriptionsService;
use crate::projections::user_service2::UserService2;
use crate::youtube::youtube_client::YoutubeClient;
use std::sync::{Arc, Mutex};

pub struct Services {
    pub user_service: Arc<Mutex<UserService2>>,
    pub subscriptions_service: Arc<Mutex<SubscriptionsService>>,
    pub feed_service: Arc<Mutex<FeedService>>,
}

impl Default for Services {
    fn default() -> Self {
        let event_store = Arc::new(EventStore::new());
        Self {
            user_service: Arc::new(Mutex::new(UserService2::new(event_store.clone()))),
            subscriptions_service: Arc::new(Mutex::new(SubscriptionsService::new(
                event_store.clone(),
            ))),
            feed_service: Arc::new(Mutex::new(FeedService::new(
                event_store.clone(),
                YoutubeClient::new(),
            ))),
        }
    }
}
