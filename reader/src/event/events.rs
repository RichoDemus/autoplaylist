use chrono::{DateTime, Utc};
use serde::Deserialize;
use serde::Serialize;

use crate::types::{ChannelId, EventId, LabelId, LabelName, Password, UserId, Username, VideoId};

// todo normalize field names

#[derive(Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
#[serde(deny_unknown_fields)]
#[serde(tag = "type")]
pub enum Event {
    #[serde(rename = "USER_CREATED")]
    UserCreated {
        id: EventId,
        timestamp: DateTime<Utc>,
        #[serde(rename = "userId")] // wat
        user_id: UserId,
        username: Username,
        password: Password,
    },
    #[serde(rename = "USER_SUBSCRIBED_TO_FEED")]
    UserSubscribedToFeed {
        id: EventId,
        timestamp: DateTime<Utc>,
        #[serde(rename = "userId")] // wat
        user_id: UserId,
        #[serde(rename = "feedId")]
        feed_id: ChannelId,
    },
    #[serde(rename = "USER_WATCHED_ITEM")]
    UserWatchedItem {
        id: EventId,
        timestamp: DateTime<Utc>,
        #[serde(rename = "userId")]
        user_id: UserId,
        #[serde(rename = "feedId")]
        feed_id: ChannelId,
        #[serde(rename = "itemId")]
        item_id: VideoId,
    },
    #[serde(rename = "USER_UNWATCHED_ITEM")]
    UserUnwatchedItem {
        id: EventId,
        timestamp: DateTime<Utc>,
        #[serde(rename = "userId")]
        user_id: UserId,
        #[serde(rename = "feedId")]
        feed_id: ChannelId,
        #[serde(rename = "itemId")]
        item_id: VideoId,
    },
    #[serde(rename = "LABEL_CREATED")]
    LabelCreated {
        id: EventId,
        timestamp: DateTime<Utc>,
        #[serde(rename = "userId")] // wat
        user_id: UserId,
        #[serde(rename = "labelId")]
        label_id: LabelId,
        #[serde(rename = "labelName")]
        label_name: LabelName,
    },
    #[serde(rename = "FEED_ADDED_TO_LABEL")]
    FeedAddedToLabel {
        id: EventId,
        timestamp: DateTime<Utc>,
        #[serde(rename = "labelId")]
        label_id: LabelId,
        #[serde(rename = "feedId")]
        feed_id: ChannelId,
    },
}

impl Event {
    pub fn id(&self) -> EventId {
        match self {
            Event::UserCreated { id, .. } => id.clone(),
            Event::UserSubscribedToFeed { id, .. } => id.clone(),
            Event::UserWatchedItem { id, .. } => id.clone(),
            Event::UserUnwatchedItem { id, .. } => id.clone(),
            Event::LabelCreated { id, .. } => id.clone(),
            Event::FeedAddedToLabel { id, .. } => id.clone(),
        }
    }
    pub fn timestamp(&self) -> DateTime<Utc> {
        match self {
            Event::UserCreated { timestamp, .. } => *timestamp,
            Event::UserSubscribedToFeed { timestamp, .. } => *timestamp,
            Event::UserWatchedItem { timestamp, .. } => *timestamp,
            Event::UserUnwatchedItem { timestamp, .. } => *timestamp,
            Event::LabelCreated { timestamp, .. } => *timestamp,
            Event::FeedAddedToLabel { timestamp, .. } => *timestamp,
        }
    }
}
