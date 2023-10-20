use crate::test::test_client::MainPage;
use crate::types::{ChannelId, Video, VideoId};
use actix_rt::time::sleep;
use std::time::{Duration, Instant};

pub async fn await_videos(main_page: &MainPage, id: ChannelId, count: usize) {
    let deadline = Instant::now() + Duration::from_secs(1);
    while Instant::now() < deadline {
        if main_page.get_videos(id.clone()).await.unwrap().len() == count {
            return;
        }
        sleep(Duration::from_millis(10)).await;
    }
    panic!("Timeout exceeded getting videos");
}

pub fn expected_videos() -> Vec<Video> {
    vec![
        Video {
            id: VideoId("video1-id".into()),
            title: "video1-title".into(),
            description: "video1-desc".into(),
            upload_date: "2023-10-15T00:59:50Z".into(),
            url: "https://www.youtube.com/watch?v=video1-id".into(),
        },
        Video {
            id: VideoId("video2-id".into()),
            title: "video2-title".into(),
            description: "video2-desc".into(),
            upload_date: "2022-10-15T00:59:50Z".into(),
            url: "https://www.youtube.com/watch?v=video2-id".into(),
        },
        Video {
            id: VideoId("video3-id".into()),
            title: "video3-title".into(),
            description: "video3-desc".into(),
            upload_date: "2013-10-15T00:59:50Z".into(),
            url: "https://www.youtube.com/watch?v=video3-id".into(),
        },
        Video {
            id: VideoId("video4-id".into()),
            title: "video4-title".into(),
            description: "video4-desc".into(),
            upload_date: "2012-10-15T00:59:50Z".into(),
            url: "https://www.youtube.com/watch?v=video4-id".into(),
        },
    ]
}

pub fn expected_videos2() -> Vec<Video> {
    vec![
        Video {
            id: VideoId("video1-id".into()),
            title: "video1-title".into(),
            description: "video1-desc".into(),
            upload_date: "2023-10-15T00:59:50Z".into(),
            url: "https://www.youtube.com/watch?v=video1-id".into(),
        },
        Video {
            id: VideoId("video2-id".into()),
            title: "video2-title".into(),
            description: "video2-desc".into(),
            upload_date: "2022-10-15T00:59:50Z".into(),
            url: "https://www.youtube.com/watch?v=video2-id".into(),
        },
        Video {
            id: VideoId("video3-id".into()),
            title: "video3-title".into(),
            description: "video3-desc".into(),
            upload_date: "2013-10-15T00:59:50Z".into(),
            url: "https://www.youtube.com/watch?v=video3-id".into(),
        },
        Video {
            id: VideoId("video4-id".into()),
            title: "video4-title".into(),
            description: "video4-desc".into(),
            upload_date: "2012-10-15T00:59:50Z".into(),
            url: "https://www.youtube.com/watch?v=video4-id".into(),
        },
        Video {
            id: VideoId("video5-id".into()),
            title: "video5-title".into(),
            description: "video5-desc".into(),
            upload_date: "2012-09-15T00:59:50Z".into(),
            url: "https://www.youtube.com/watch?v=video5-id".into(),
        },
    ]
}
