use std::collections::HashMap;

use chrono::{DateTime, Datelike, Utc};
use log::{error, info};

use crate::projections::feed_service::feed_service_types::Video;
use crate::sled_wrapper::DiskCache;
use crate::types::{ChannelId, VideoId};
use crate::youtube::youtube_client::YoutubeClient;

struct VideoDates {
    id: VideoId,
    upload_date: DateTime<Utc>,
    last_updated: DateTime<Utc>,
}

pub async fn update_statistics(client: YoutubeClient, videos: &DiskCache<ChannelId, Vec<Video>>) {
    let vids = videos
        .values()
        .flatten()
        .map(|v| VideoDates {
            id: v.id.clone(),
            upload_date: v.upload_date.clone(),
            last_updated: v.last_updated.clone(),
        })
        .collect::<Vec<_>>();

    let prioritized_ids = prioritize_videos_to_update(vids);

    let mut updates = HashMap::new();
    for ids in prioritized_ids.chunks(50) {
        match client.statistics(ids.to_vec()).await {
            Err(e) => {
                error!("Failed to get statistics: {e:?}");
            }
            Ok(stats) => {
                updates.extend(stats);
            }
        }
    }

    for id in videos.keys() {
        if let Some(mut vids) = videos.get(id.clone()) {
            for vid in &mut vids {
                if let Some((views, duration)) = updates.get(&vid.id) {
                    vid.last_updated = Utc::now();
                    vid.views = views.clone();
                    vid.duration = duration.clone();
                }
            }
            videos.insert(id, vids);
        }
    }
    info!("Updated statistics for {} videos", updates.len());
}

fn prioritize_videos_to_update(vids: Vec<VideoDates>) -> Vec<VideoId> {
    let now = Utc::now();
    let (new_things, older_things): (Vec<VideoDates>, Vec<VideoDates>) = vids
        .into_iter()
        .partition(|it| it.upload_date > (now - chrono::Duration::days(7)));

    let (mut more_than_two_months_since_update, less_than_two_months_since_update): (
        Vec<VideoDates>,
        Vec<VideoDates>,
    ) = older_things
        .into_iter()
        .partition(|it| it.last_updated < (now - chrono::Duration::days(60)));

    let (upload_day_same_as_current_day, _rest_of_things): (Vec<VideoDates>, Vec<VideoDates>) =
        less_than_two_months_since_update
            .into_iter()
            .partition(|it| it.upload_date.day() == now.day());

    if more_than_two_months_since_update.len() > 1000 {
        more_than_two_months_since_update.truncate(1000);
    }

    let all_things: Vec<VideoDates> = new_things
        .into_iter()
        .chain(more_than_two_months_since_update.into_iter())
        .chain(upload_day_same_as_current_day.into_iter())
        .collect();

    let all_thing_ids: Vec<VideoId> = all_things.iter().map(|it| it.id.clone()).collect();

    all_thing_ids
}
