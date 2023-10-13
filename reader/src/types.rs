use derive_newtype::NewType;
use serde::Deserialize;
use serde::Serialize;
use std::time::Duration;
use uuid::Uuid;

#[derive(NewType, Serialize, Deserialize, Default, Debug, Eq, PartialEq, Clone)]
pub struct EventId(pub Uuid);

#[derive(Hash, NewType, Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct UserId(pub Uuid);

#[derive(Hash, NewType, Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct FeedId(pub String);

#[derive(Hash, NewType, Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct FeedUrl(pub String);

#[derive(Hash, NewType, Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct FeedName(pub String);

#[derive(Hash, NewType, Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct ItemId(pub String);

#[derive(Hash, NewType, Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct Username(pub String);

#[derive(NewType, Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct Password(pub String);

#[derive(NewType, Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct LabelId(pub Uuid);

#[derive(NewType, Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct LabelName(pub String);

#[derive(Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct FeedWithoutItem {
    pub(crate) id: FeedId,
    pub(crate) name: FeedName,
    #[serde(rename = "numberOfAvailableItems")]
    pub(crate) number_of_available_items: usize,
}

#[derive(Serialize, Deserialize)]
pub struct UserSession {
    user_id: UserId,
    username: Username,
}
#[derive(Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct Feed {
    pub(crate) id: FeedId,
    pub(crate) name: FeedName,
    pub(crate) items: Vec<Item>,
}
#[derive(Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct Item {
    pub(crate) id: ItemId,
    pub(crate) title: String,
    pub(crate) description: String,
    pub(crate) upload_date: String,
    pub(crate) url: String,
    pub(crate) duration: Duration,
    pub(crate) views: u64,
}
