use std::collections::HashMap;
use std::sync::Mutex;

use actix_test::TestServer;
use actix_web::web::{Data, Query};
use actix_web::{get, App, HttpResponse};
use log::trace;
use serde::Deserialize;
use serde_json::json;

pub struct YoutubeMock {
    mock: TestServer,
}

impl Default for YoutubeMock {
    fn default() -> Self {
        let state = Data::new(State {
            first_download: Mutex::new(true),
        });
        Self {
            mock: actix_test::start(move || {
                App::new()
                    .app_data(state.clone())
                    .service(search)
                    .service(channels)
                    .service(playlist_items)
            }),
        }
    }
}

#[get("/youtube/v3/search/")]
async fn search(query: Query<HashMap<String, String>>) -> HttpResponse {
    if query.get("type").unwrap_or(&"".to_string()) != "channel" {
        return HttpResponse::BadRequest().into();
    }
    if query.get("key").unwrap_or(&"".to_string()) != "YT_KEY" {
        return HttpResponse::BadRequest().into();
    }
    if query.get("part").unwrap_or(&"".to_string()) != "snippet" {
        return HttpResponse::BadRequest().into();
    }
    if query.get("q").unwrap_or(&"".to_string()) != "https://www.youtube.com/user/richodemus" {
        return HttpResponse::BadRequest().into();
    }

    HttpResponse::Ok().json(json!({
        "items": [{
            "snippet": {
                "channelTitle": "richo-channel-name",
                "channelId": "richo-channel-id",
            }
        }]
    }))
}

#[get("/youtube/v3/channels/")]
async fn channels(query: Query<HashMap<String, String>>) -> HttpResponse {
    if query.get("key").unwrap_or(&"".to_string()) != "YT_KEY" {
        return HttpResponse::BadRequest().into();
    }
    if query.get("part").unwrap_or(&"".to_string()) != "snippet,contentDetails" {
        return HttpResponse::BadRequest().into();
    }
    if query.get("id").unwrap_or(&"".to_string()) != "richo-channel-id" {
        return HttpResponse::BadRequest().into();
    }

    HttpResponse::Ok().json(json!({
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
    }))
}

#[get("/youtube/v3/playlistItems/")]
async fn playlist_items(query: Query<QueryParams>, state: Data<State>) -> HttpResponse {
    if query.key != "YT_KEY" {
        return HttpResponse::BadRequest().into();
    }
    if query.part != "snippet" {
        return HttpResponse::BadRequest().into();
    }
    if query.playlist_id != "uploads-playlist-id" {
        return HttpResponse::BadRequest().into();
    }
    let first_download = state.first_download.lock().unwrap().clone();
    trace!("playlist items: first {first_download} query: {query:?}");
    HttpResponse::Ok().json(match query.page_token.as_deref() {
        None if first_download => {
            *state.first_download.lock().unwrap() = false;
            json!({
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
             "nextPageToken": "first-dl-page-two"
            })
        }
        None if !first_download => {
            json!({
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
                "nextPageToken": "second-dl-page-two"
            })
        }
        Some("first-dl-page-two") => json!({
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
        }),
        Some("second-dl-page-two") => json!({
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
                "nextPageToken": "second-dl-page-three",
        }),
        Some("second-dl-page-three") => json!({
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
        }),
        _ => todo!("not implemented"),
    })
}

impl YoutubeMock {
    pub fn base_url(&self) -> String {
        format!("http://localhost:{}", self.mock.addr().port())
    }
}

#[derive(Deserialize, Debug)]
struct QueryParams {
    page_token: Option<String>,
    key: String,
    part: String,
    #[serde(rename = "playlistId")]
    playlist_id: String,
}

struct State {
    first_download: Mutex<bool>,
}
