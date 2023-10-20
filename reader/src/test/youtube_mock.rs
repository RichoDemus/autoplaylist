use std::env;

use httpmock::prelude::*;
use serde_json::json;

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

    pub fn setup_youtube_mock(&self) {
        env::set_var("YOUTUBE_API_KEY", "YT_KEY".to_string());
        env::set_var("YOUTUBE_BASE_DIR", self.mock_server.base_url());

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
                    !m.query_params
                        .clone()
                        .unwrap_or_default()
                        .iter()
                        .any(|(h, _v)| h == "page_token")
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
                .query_param("page_token", "page-two");
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
        self.mock_server.mock(|when, then| {
            when.method(GET)
                .path("/youtube/v3/playlistItems/")
                .query_param("key", "YT_KEY")
                .query_param("part", "snippet")
                .query_param("playlistId", "uploads-playlist-id")
                .query_param("page_token", "page-three");
            then.status(200).json_body(json!({
                "items": [{
                    "snippet": {
                        "resourceId": {
                            "videoId": "video1-id"
                        },
                        "title": "video1-title",
                        "description": "video1-desc",
                        "publishedAt": "2023-10-15T00:59:50Z"
                    }
                },{
                    "snippet": {
                        "resourceId": {
                            "videoId": "video2-id"
                        },
                        "title": "video2-title",
                        "description": "video2-desc",
                        "publishedAt": "2022-10-15T00:59:50Z"
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
                .query_param("page_token", "page-two");
            then.status(200).json_body(json!({
                "items": [{
                    "snippet": {
                        "resourceId": {
                            "videoId": "video3-id"
                        },
                        "title": "video3-title",
                        "description": "video3-desc",
                        "publishedAt": "2013-10-15T00:59:50Z"
                    }
                },{
                    "snippet": {
                        "resourceId": {
                            "videoId": "video4-id"
                        },
                        "title": "video4-title",
                        "description": "video4-desc",
                        "publishedAt": "2012-10-15T00:59:50Z"
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
                .query_param("playlistId", "uploads-playlist-id");
            then.status(200).json_body(json!({
                "items": [{
                    "snippet": {
                        "resourceId": {
                            "videoId": "video5-id"
                        },
                        "title": "video5-title",
                        "description": "video5-desc",
                        "publishedAt": "2012-09-15T00:59:50Z"
                    }
                }],
                "nextPageToken": "page-two",
            }));
        });
    }
}
