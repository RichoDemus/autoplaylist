use std::sync::{Arc, Mutex};

use anyhow::Result;
use chrono::Utc;
use dashmap::DashMap;
use uuid::Uuid;

use crate::endpoints::endpoint_types::Label;
use crate::event::event_store::EventStore;
use crate::event::events::Event::{FeedAddedToLabel, LabelCreated};
use crate::types::{ChannelId, LabelId, LabelName, UserId};

pub struct LabelService {
    event_store: Arc<Mutex<EventStore>>,
    labels: Arc<DashMap<UserId, Vec<Label>>>,
}

impl LabelService {
    pub fn new(event_store: Arc<Mutex<EventStore>>) -> Self {
        let labels: Arc<DashMap<UserId, Vec<Label>>> = Default::default();
        let labels_spawn = labels.clone();
        let mut receiver = event_store.lock().unwrap().receiver();
        actix_rt::spawn(async move {
            while let Some(event) = receiver.recv().await {
                match event {
                    LabelCreated {
                        id: _,
                        timestamp: _,
                        user_id,
                        label_id,
                        label_name,
                    } => labels_spawn.entry(user_id).or_default().push(Label {
                        id: label_id,
                        name: label_name,
                        feeds: vec![],
                    }),
                    FeedAddedToLabel {
                        id: _,
                        timestamp: _,
                        label_id,
                        feed_id,
                    } => {
                        for mut entry in labels_spawn.iter_mut() {
                            let labels = entry.value_mut();
                            for label in labels.iter_mut() {
                                if label.id == label_id {
                                    label.feeds.push(feed_id.clone());
                                }
                            }
                        }
                    }
                    _ => {}
                }
            }
        });
        Self {
            event_store,
            labels,
        }
    }

    pub async fn create_label(&mut self, user_id: UserId, label_name: LabelName) -> Result<()> {
        self.event_store
            .lock()
            .unwrap()
            .publish_event(LabelCreated {
                id: Default::default(),
                timestamp: Utc::now(),
                user_id,
                label_id: LabelId(Uuid::new_v4()), // todo impl default
                label_name,
            })
            .await
    }

    pub fn get_labels(&self, user_id: &UserId) -> Vec<Label> {
        self.labels
            .get(user_id)
            .map(|v| v.value().clone())
            .unwrap_or_default()
    }

    pub async fn add_channel_to_label(
        &mut self,
        feed_id: ChannelId,
        label_id: LabelId,
    ) -> Result<()> {
        self.event_store
            .lock()
            .unwrap()
            .publish_event(FeedAddedToLabel {
                id: Default::default(),
                timestamp: Utc::now(),
                label_id,
                feed_id,
            })
            .await
    }
}
