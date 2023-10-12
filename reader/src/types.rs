use derive_newtype::NewType;
use serde::Deserialize;
use serde::Serialize;
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
    pub(crate) number_of_available_items: i32,
}

#[derive(Serialize, Deserialize)]
pub struct UserSession {
    user_id: UserId,
    username: Username,
}
