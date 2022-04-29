use anyhow::*;
use log::info;
use reqwest::Client;
use serde::Deserialize;
use serde::Serialize;
use serde_json::Value;
use std::fmt::{Display, Formatter};

pub struct SpotifyClient {
    access_token: String,
    client: Client,
}

#[derive(Debug, Serialize, Deserialize)]
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
    pub fn new(access_token: String) -> Self {
        Self {
            access_token,
            client: Client::new(),
        }
    }

    pub async fn artist(&self) -> Result<Vec<Track>> {
        let json: Value = self
            .client
            .get(" 	https://api.spotify.com/v1/search")
            .header("Authorization", format!("Bearer {}", self.access_token))
            .query(&[("q", "powerwolf"), ("type", "artist"), ("market", "SE")])
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
            .get(format!(
                "https://api.spotify.com/v1/artists/{artist_id}/albums"
            ))
            .header("Authorization", format!("Bearer {}", self.access_token))
            .query(&[("market", "SE")])
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
            .into_iter()
            .flat_map(|value| {
                match (
                    value["name"].as_str().map(|s| s.to_string()),
                    value["id"].as_str().map(|s| s.to_string()),
                ) {
                    (Some(name), Some(id)) => Some((name, id)),
                    _ => None,
                }
            })
            .collect::<Vec<_>>();

        // info!("albums: {:#?}", album_names);

        let mut print = String::new();
        let mut tracks = vec![];
        for (album_name, album_id) in album_names {
            let json: Value = self
                .client
                .get(format!("https://api.spotify.com/v1/albums/{album_id}"))
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
                .get(format!(
                    "https://api.spotify.com/v1/albums/{album_id}/tracks"
                ))
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
                .into_iter()
                .flat_map(|value| {
                    match (
                        value["name"].as_str().map(|s| s.to_string()),
                        value["id"].as_str().map(|s| s.to_string()),
                    ) {
                        (Some(name), Some(id)) => Some((name, id)),
                        _ => None,
                    }
                })
                .collect::<Vec<_>>();
            // info!("tracks: {:#?}", track_names);
            print.push_str(album_name);
            print.push_str("\n");
            for (track_name, track_id) in track_names {
                print.push_str("\t");
                print.push_str(track_name);
                print.push_str("\n");
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
            print.push_str("\n");
        }
        info!("Result:\n{print}");
        Ok(tracks)
    }
}
