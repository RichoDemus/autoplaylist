use chrono::{DateTime, Utc};
use derive_newtype::NewType;
use serde::Deserialize;
use serde::Serialize;
use uuid::Uuid;

#[derive(NewType, Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct EventId(pub Uuid);
impl Default for EventId {
    fn default() -> Self {
        Self(Uuid::new_v4())
    }
}

#[derive(Hash, NewType, Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct UserId(pub Uuid);

#[derive(Hash, NewType, Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct ChannelId(pub String);

#[derive(Hash, NewType, Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct PlaylistId(pub String);

#[derive(Hash, NewType, Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct YoutubeChannelUrl(pub String);

#[derive(Hash, NewType, Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct ChannelName(pub String);

#[derive(Hash, NewType, Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct VideoId(pub String);

#[derive(Hash, NewType, Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct Username(pub String);

#[derive(NewType, Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct Password(pub String);

#[derive(NewType, Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct LabelId(pub Uuid);

#[derive(NewType, Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct LabelName(pub String);

#[derive(Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct ChannelWithoutVideos {
    pub(crate) id: ChannelId,
    pub(crate) name: ChannelName,
    #[serde(rename = "numberOfAvailableItems")]
    pub(crate) number_of_available_items: usize,
}

#[derive(Serialize, Deserialize)]
pub struct UserSession {
    user_id: UserId,
    username: Username,
}

#[derive(Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct Channel {
    pub(crate) id: ChannelId,
    pub(crate) name: ChannelName,
    pub(crate) playlist: PlaylistId,
    // pub(crate) items: Vec<Video>,
}

#[derive(Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct ViewCount(pub u64);

#[derive(Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct VideoDuration(pub String);

impl From<&str> for VideoDuration {
    fn from(value: &str) -> Self {
        let duration = value
            .parse::<iso8601_duration::Duration>()
            .unwrap()
            .to_std()
            .unwrap();
        let seconds = duration.as_secs() % 60;
        let minutes = (duration.as_secs() / 60) % 60;
        let hours = (duration.as_secs() / 60) / 60;
        Self(format!("{:0>2}:{:0>2}:{:0>2}", hours, minutes, seconds))
    }
}

#[derive(Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct Video {
    pub(crate) id: VideoId,
    pub(crate) title: String,
    pub(crate) description: String,
    #[serde(rename = "uploadDate")]
    pub(crate) upload_date: String,
    #[serde(rename = "lastUpdated")]
    pub(crate) last_updated: DateTime<Utc>,
    pub(crate) url: String,
    pub(crate) duration: VideoDuration,
    pub(crate) views: ViewCount,
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn parse_video_duration() {
        assert_eq!(VideoDuration("00:00:22".to_string()), "PT22S".into());
        assert_eq!(VideoDuration("11:54:58".to_string()), "PT11H54M58S".into());
    }
}
