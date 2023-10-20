use std::env;
use std::sync::atomic::AtomicBool;
use std::sync::atomic::Ordering::SeqCst;
use std::sync::Mutex;

use crate::test::test_utis::MoreVidsContainer;
use httpmock::prelude::*;
use log::trace;
use once_cell::sync::Lazy;
use serde_json::json;

// static MORE_VIDEOS: Lazy<AtomicBool> = Lazy::new(|| AtomicBool::new(false));
static MORE_VIDEOS: Lazy<Mutex<MoreVidsContainer>> =
    Lazy::new(|| Mutex::new(MoreVidsContainer::new()));

pub struct YoutubeMock {
    mock_server: MockServer,
}

impl Default for YoutubeMock {
    fn default() -> Self {
        Self {
            mock_server: MockServer::start(),
        }
    }
}

impl YoutubeMock {
    pub fn init(&self) {
        self.setup_youtube_mock();
    }

    pub fn base_url(&self) -> String {
        self.mock_server.base_url()
    }

    pub fn setup_youtube_mock(&self) {
        MORE_VIDEOS.lock().unwrap().fals("setup");
        // todo add proper checks to mocks, like query params
        self.mock_server.mock(|when, then| {
            when.method(GET)
                .path("/youtube/v3/search/")
                .query_param("key", "YT_KEY")
                .query_param("part", "snippet")
                .query_param("type", "channel")
                .query_param("q", "https://www.youtube.com/user/richodemus");
            then.status(200).json_body(json!({
                "items": [{
                    "snippet": {
                        "channelTitle": "richo-channel-name",
                        "channelId": "richo-channel-id",
                    }
                }]
            }));
        });

        self.mock_server.mock(|when, then| {
            when.method(GET)
                .path("/youtube/v3/channels/")
                .query_param("key", "YT_KEY")
                .query_param("part", "snippet,contentDetails")
                .query_param("id", "richo-channel-id");
            then.status(200).json_body(json!({
                "items": [{
                    "snippet": {
                        "title": "richo-channel-name",
                    },
                    "contentDetails": {
                        "relatedPlaylists": {
                            "uploads" : "uploads-playlist-id",
                        }
                    }
                }]
            }));
        });

        self.mock_server.mock(|when, then| {
            when.method(GET)
                .path("/youtube/v3/playlistItems/")
                .query_param("key", "YT_KEY")
                .query_param("part", "snippet")
                .query_param("playlistId", "uploads-playlist-id")
                .matches(|m| {
                    let more_videos = MORE_VIDEOS.lock().unwrap().get();
                    trace!(
                        "old endpoint evaluating: {:?}, more vids: {}",
                        m.query_params,
                        more_videos
                    );
                    let b = !m
                        .query_params
                        .clone()
                        .unwrap_or_default()
                        .iter()
                        .any(|(h, _v)| h == "page_token")
                        && !more_videos;
                    trace!("Using old endpoint: {b}");
                    b
                });
            then.status(200).json_body(json!({
             "items": [{
                 "snippet": {
                     "resourceId": {
                         "videoId": "video4-id"
                     },
                     "title": "video4-title",
                     "description": "video4-desc",
                     "publishedAt": "2004-01-01T00:00:00Z"
                 }
             },{
                 "snippet": {
                     "resourceId": {
                         "videoId": "video3-id"
                     },
                     "title": "video3-title",
                     "description": "video3-desc",
                     "publishedAt": "2003-01-01T00:00:00Z"
                 }
             }],
                "nextPageToken": "page-two",
            }));
        });

        self.mock_server.mock(|when, then| {
            when.method(GET)
                .path("/youtube/v3/playlistItems/")
                .query_param("key", "YT_KEY")
                .query_param("part", "snippet")
                .query_param("playlistId", "uploads-playlist-id")
                .query_param("page_token", "page-two")
                .matches(|_| !MORE_VIDEOS.lock().unwrap().get());
            then.status(200).json_body(json!({
                "items": [{
                    "snippet": {
                        "resourceId": {
                            "videoId": "video2-id"
                        },
                        "title": "video2-title",
                        "description": "video2-desc",
                        "publishedAt": "2002-01-01T00:00:00Z"
                    }
                },{
                    "snippet": {
                        "resourceId": {
                            "videoId": "video1-id"
                        },
                        "title": "video1-title",
                        "description": "video1-desc",
                        "publishedAt": "2001-01-01T00:00:00Z"
                    }
                }],
            }));
        });

        //more videos

        self.mock_server.mock(|when, then| {
            when.method(GET)
                .path("/youtube/v3/playlistItems/")
                .query_param("key", "YT_KEY")
                .query_param("part", "snippet")
                .query_param("playlistId", "uploads-playlist-id")
                .matches(|m| {
                    let more_videos = MORE_VIDEOS.lock().unwrap().get();
                    trace!(
                        "new endpoint evaluating: {:?}, more vids: {}",
                        m.query_params,
                        more_videos
                    );
                    let b = !m
                        .query_params
                        .clone()
                        .unwrap_or_default()
                        .iter()
                        .any(|(h, _v)| h == "page_token")
                        && more_videos;
                    trace!("Using new endpoint: {b}");
                    b
                });
            then.status(200).json_body(json!({
             "items": [{
                 "snippet": {
                     "resourceId": {
                         "videoId": "video5-id"
                     },
                     "title": "video5-title",
                     "description": "video5-desc",
                     "publishedAt": "2005-01-01T00:00:00Z"
                 }
             }],
                "nextPageToken": "page-two",
            }));
        });

        self.mock_server.mock(|when, then| {
            when.method(GET)
                .path("/youtube/v3/playlistItems/")
                .query_param("key", "YT_KEY")
                .query_param("part", "snippet")
                .query_param("playlistId", "uploads-playlist-id")
                .query_param("page_token", "page-two")
                .matches(|_| MORE_VIDEOS.lock().unwrap().get());
            then.status(200).json_body(json!({
             "items": [{
                 "snippet": {
                     "resourceId": {
                         "videoId": "video4-id"
                     },
                     "title": "video4-title",
                     "description": "video4-desc",
                     "publishedAt": "2004-01-01T00:00:00Z"
                 }
             },{
                 "snippet": {
                     "resourceId": {
                         "videoId": "video3-id"
                     },
                     "title": "video3-title",
                     "description": "video3-desc",
                     "publishedAt": "2003-01-01T00:00:00Z"
                 }
             }],
                "nextPageToken": "page-three",
            }));
        });

        self.mock_server.mock(|when, then| {
            when.method(GET)
                .path("/youtube/v3/playlistItems/")
                .query_param("key", "YT_KEY")
                .query_param("part", "snippet")
                .query_param("playlistId", "uploads-playlist-id")
                .query_param("page_token", "page-three")
                .matches(|_| MORE_VIDEOS.lock().unwrap().get());
            then.status(200).json_body(json!({
                "items": [{
                    "snippet": {
                        "resourceId": {
                            "videoId": "video2-id"
                        },
                        "title": "video2-title",
                        "description": "video2-desc",
                        "publishedAt": "2002-01-01T00:00:00Z"
                    }
                },{
                    "snippet": {
                        "resourceId": {
                            "videoId": "video1-id"
                        },
                        "title": "video1-title",
                        "description": "video1-desc",
                        "publishedAt": "2001-01-01T00:00:00Z"
                    }
                }],
            }));
        });
    }

    pub fn setup_youtube_mock_additional_videos(&mut self) {
        MORE_VIDEOS.lock().unwrap().tru();
        trace!(
            "Setting up yt mock for more videos, more vids bool: {}",
            MORE_VIDEOS.lock().unwrap().get()
        );
    }
}
