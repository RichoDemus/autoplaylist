use derive_newtype::NewType;
use serde::Deserialize;
use serde::Serialize;
use uuid::Uuid;

#[derive(NewType, Serialize, Deserialize, Default, Debug, Eq, PartialEq, Clone)]
pub struct EventId(pub Uuid);

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
pub struct Video {
    pub(crate) id: VideoId,
    pub(crate) title: String,
    pub(crate) description: String,
    #[serde(rename = "uploadDate")]
    pub(crate) upload_date: String,
    pub(crate) url: String,
    pub(crate) duration: String,
    pub(crate) views: u64,
}
