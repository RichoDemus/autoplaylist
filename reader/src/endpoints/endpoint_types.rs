use crate::types::{ChannelId, ChannelWithoutVideos, LabelId, LabelName};
use serde::Deserialize;

pub struct Label {
    pub id: LabelId,
    pub name: LabelName,
    pub feeds: Vec<ChannelId>,
}

pub struct AllFeedsAndLabelsResponse {
    feeds: Vec<ChannelWithoutVideos>,
    labels: Vec<Label>,
}

#[derive(Deserialize, Debug)]
pub enum Operation {
    MARK_READ,
    MARK_UNREAD,
}
