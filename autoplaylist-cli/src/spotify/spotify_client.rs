use std::collections::HashMap;
use std::fmt::{Display, Formatter};

use anyhow::{Context, Ok, Result};
use log::info;
use reqwest::Client;
use serde::Deserialize;
use serde::Serialize;
use serde_json::Value;

pub struct SpotifyClient {
    access_token: String,
    client: Client,
    base_url: String,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct Track {
    pub album_name: String,
    pub artist_name: String,
    pub release_date: String,
    pub track_name: String,
    pub album_id: String,
    pub artist_id: String,
    pub track_id: String,
}

impl Display for Track {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "{} - {}", self.album_name, self.track_name)
    }
}

impl SpotifyClient {
    pub fn new(access_token: String, base_url: Option<String>) -> Self {
        Self {
            access_token,
            client: Client::new(),
            base_url: base_url.unwrap_or_else(|| "https://api.spotify.com".to_string()),
        }
    }

    pub async fn artist(&self, artist: &str) -> Result<Vec<Track>> {
        let base_url = &self.base_url;
        let json: Value = self
            .client
            .get(format!("{base_url}/v1/search"))
            .header("Authorization", format!("Bearer {}", self.access_token))
            .query(&[("q", artist), ("type", "artist"), ("market", "SE")])
            .send()
            .await
            .context("search")?
            .json()
            .await
            .context("parse search response")?;

        let first_match_artist = &json["artists"]["items"][0];
        let artist_name = first_match_artist["name"]
            .as_str()
            .context("Getting artist name")?;
        let artist_id = first_match_artist["id"]
            .as_str()
            .context("Getting artist id")?;
        // info!("Artist: {}, id: {}", name, id);

        let json: Value = self
            .client
            .get(format!("{base_url}/v1/artists/{artist_id}/albums"))
            .header("Authorization", format!("Bearer {}", self.access_token))
            .query(&[
                ("market", "SE"),
                ("limit", "50"),
                ("include_groups", "album,single"),
            ])
            .send()
            .await
            .context("get albums")?
            .json()
            .await
            .context("parse albums json")?;

        // info!("raw result: {:#?}", json);
        let album_names = &json["items"]
            .as_array()
            .context("iterate albums")?
            .iter()
            .filter_map(|value| {
                match (
                    value["name"].as_str().map(std::string::ToString::to_string),
                    value["id"].as_str().map(std::string::ToString::to_string),
                ) {
                    (Some(name), Some(id)) => Some((name, id)),
                    _ => None,
                }
            })
            .collect::<Vec<_>>();
        assert!(
            album_names.len() < 50,
            "we got 50 albums, we probably don't handle that"
        );

        let mut print = String::new();
        let mut tracks = vec![];
        for (album_name, album_id) in album_names {
            let json: Value = self
                .client
                .get(format!("{base_url}/v1/albums/{album_id}"))
                .header("Authorization", format!("Bearer {}", self.access_token))
                .query(&[("market", "SE")])
                .send()
                .await
                .context("Get album")?
                .json()
                .await
                .context("parse album json")?;
            let release_date = json["release_date"].as_str();

            let json: Value = self
                .client
                .get(format!("{base_url}/v1/albums/{album_id}/tracks"))
                .header("Authorization", format!("Bearer {}", self.access_token))
                .query(&[("market", "SE")])
                .send()
                .await?
                .json()
                .await?;
            // info!("album {name}: {:#?}",json);
            let track_names = &json["items"]
                .as_array()
                .context("iterate tracks")?
                .iter()
                .filter_map(|value| {
                    match (
                        value["name"].as_str().map(std::string::ToString::to_string),
                        value["id"].as_str().map(std::string::ToString::to_string),
                    ) {
                        (Some(name), Some(id)) => Some((name, id)),
                        _ => None,
                    }
                })
                .collect::<Vec<_>>();
            // info!("tracks: {:#?}", track_names);
            print.push_str(album_name);
            print.push('\n');
            for (track_name, track_id) in track_names {
                print.push('\t');
                print.push_str(track_name);
                print.push('\n');
                tracks.push(Track {
                    album_name: album_name.clone(),
                    artist_name: artist_name.to_string(),
                    release_date: release_date
                        .expect("album didn't have a release date")
                        .to_string(),
                    track_name: track_name.clone(),
                    album_id: album_id.clone(),
                    artist_id: artist_id.to_string(),
                    track_id: track_id.clone(),
                });
            }
            print.push('\n');
        }
        info!("Result:\n{print}");
        Ok(tracks)
    }

    pub(crate) async fn create_or_update_playlist(
        &self,
        playlist_name: &str,
        tracks: Vec<Track>,
    ) -> Result<()> {
        let playlists = self.get_playlists().await?;

        let maybe_exists = playlists
            .into_iter()
            .find(|playlist| playlist.name == playlist_name);
        info!("Playlist found?: {}", maybe_exists.is_some());
        let playlist_id = match maybe_exists {
            None => self.create_playlist(playlist_name).await?,
            Some(playlist) => playlist.id,
        };

        info!("playlist id: {}", playlist_id);

        self.set_playlist_content(playlist_id, tracks).await
    }

    async fn create_playlist(&self, playlist_name: &str) -> Result<PlaylistId> {
        let base_url = &self.base_url;
        let mut map = HashMap::new();
        map.insert("name", playlist_name);
        map.insert("public", "false");
        map.insert("collaborative", "false");
        map.insert("description", "Created by autoplaylist-cli");
        let response = self
            .client
            .post(format!("{base_url}/v1/me/playlists"))
            .header("Authorization", format!("Bearer {}", self.access_token))
            .json(&map)
            .send()
            .await
            .context("Create playlist")?;
        let json: Value = response
            .json()
            .await
            .context("Parse create playlist json")?;
        let playlist_id = json["id"]
            .as_str()
            .context("Unpack id from create playlist")?
            .to_string();
        Ok(playlist_id)
    }

    async fn set_playlist_content(
        &self,
        playlist_id: PlaylistId,
        tracks: Vec<Track>,
    ) -> Result<()> {
        assert!(!tracks.is_empty(), "tracks cant be empty");
        let base_url = &self.base_url;
        for (i, chunk) in tracks.chunks(100).enumerate() {
            //test with 100
            info!("Adding {i}th chunk");
            if i == 0 {
                let body = create_body(chunk)?;
                let response = self
                    .client
                    .put(format!("{base_url}/v1/playlists/{playlist_id}/tracks"))
                    .header("Authorization", format!("Bearer {}", self.access_token))
                    .body(body)
                    .send()
                    .await
                    .context("Add tracks to playlist")?;

                info!("put tracks resp: {:?}", response);
                info!("text: {:?}", response.text().await);
                continue;
            }
            let body = create_body(chunk)?;
            let response = self
                .client
                .post(format!("{base_url}/v1/playlists/{playlist_id}/tracks"))
                .header("Authorization", format!("Bearer {}", self.access_token))
                .header("Accept", "application/json")
                .body(body)
                .send()
                .await
                .context("Add tracks to playlist")?;

            info!("put tracks resp: {:?}", response);
            info!("text: {:?}", response.text().await);
        }
        // let tracks = tracks[0..99].to_vec();
        // assert!(tracks.len() < 100, "don't currently support more than 100 tracks");

        // let track_ids = tracks.into_iter().map(|track|track.track_id).collect::<Vec<_>>();
        // let track_ids = serde_json::to_string(&track_ids)?;
        // info!("tracks: {}", track_ids);
        // info!("playlist id: {}", playlist_id);
        // info!("access token: {}", self.access_token);
        // // let mut body = HashMap::new();
        // // body.insert("uris", track_ids);
        // let body = create_body(tracks)?;
        // let response = self.client
        //     .put(format!("{BASE_URL}/v1/playlists/{playlist_id}/tracks"))
        //     .header("Authorization", format!("Bearer {}", self.access_token))
        //     .header("Accept","application/json")
        //     .body(body)
        //     .send().await.context("Add tracks to playlist")?;
        //
        // info!("put tracks resp: {:?}", response);
        // info!("text: {:?}", response.text().await);
        Ok(())
    }

    async fn get_playlists(&self) -> Result<Vec<Playlist>> {
        let base_url = &self.base_url;
        let mut offset = 0;
        let mut playlists = vec![];
        loop {
            let response = self
                .client
                .get(format!("{base_url}/v1/me/playlists"))
                .header("Authorization", format!("Bearer {}", self.access_token))
                .query(&[("limit", "50"), ("offset", offset.to_string().as_str())])
                .send()
                .await
                .context("get playlists")?;
            if !response.status().is_success() {
                break;
            }
            let json: Value = response.json().await.context("parse get playlists")?;
            let next = &json["next"].as_str();
            info!("{next:?}");

            let items = &json["items"].as_array().context("get items")?;
            for value in &**items {
                let name = value["name"]
                    .as_str()
                    .context("get playlist name")?
                    .to_string();
                let id = value["id"].as_str().context("get playlist id")?.to_string();
                playlists.push(Playlist { id, name });
            }
            // todo parse next query string and use offset instead of incrementing
            offset += 50;
            if next.is_none() {
                break;
            }
        }
        Ok(playlists)
    }
}

type PlaylistId = String;

struct Playlist {
    id: PlaylistId,
    name: String,
}

#[derive(Serialize)]
struct Body {
    uris: Vec<String>,
}

fn create_body(tracks: &[Track]) -> Result<String> {
    let uris = tracks
        .iter()
        .map(|track| format!("spotify:track:{}", track.track_id))
        .collect::<Vec<_>>();
    serde_json::to_string(&Body { uris }).context("serialize body")
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn it_works() {
        let expected = r#"{"uris":["spotify:track:70aIp3AQtpSePVE7eIFTxi","spotify:track:2BxmDUvrfdW5GrNIGTROgk"]}"#;
        let result = create_body(&[
            Track {
                album_name: "".to_string(),
                artist_name: "".to_string(),
                release_date: "".to_string(),
                track_name: "".to_string(),
                album_id: "".to_string(),
                artist_id: "".to_string(),
                track_id: "70aIp3AQtpSePVE7eIFTxi".to_string(),
            },
            Track {
                album_name: "".to_string(),
                artist_name: "".to_string(),
                release_date: "".to_string(),
                track_name: "".to_string(),
                album_id: "".to_string(),
                artist_id: "".to_string(),
                track_id: "2BxmDUvrfdW5GrNIGTROgk".to_string(),
            },
        ])
        .unwrap();

        assert_eq!(expected, result);
    }
}
