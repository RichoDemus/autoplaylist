use crate::types::{VideoDuration, VideoId, ViewCount};
use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize, Debug, Eq, PartialEq, Clone)]
pub struct Video {
    pub(crate) id: VideoId,
    pub(crate) title: String,
    pub(crate) description: String,
    pub(crate) upload_date: DateTime<Utc>,
    pub(crate) last_updated: DateTime<Utc>,
    pub(crate) duration: VideoDuration,
    pub(crate) views: ViewCount,
}
