use serde::{Deserialize, Serialize};

use crate::types::{ChannelId, ChannelWithoutVideos, LabelId, LabelName};

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct Label {
    pub id: LabelId,
    pub name: LabelName,
    pub feeds: Vec<ChannelId>,
}

#[derive(Serialize, Deserialize, Debug)]
pub struct AllFeedsAndLabelsResponse {
    pub feeds: Vec<ChannelWithoutVideos>,
    pub labels: Vec<Label>,
}

#[derive(Deserialize, Debug)]
pub enum Operation {
    MARK_READ,
    MARK_UNREAD,
}
