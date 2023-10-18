use crate::types::{ChannelId, ChannelWithoutVideos, LabelId, LabelName};

pub struct Label {
    pub id: LabelId,
    pub name: LabelName,
    pub feeds: Vec<ChannelId>,
}

pub struct AllFeedsAndLabelsResponse {
    feeds: Vec<ChannelWithoutVideos>,
    labels: Vec<Label>,
}
