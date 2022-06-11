#[cfg(test)]
mod tests {
    use crate::spotify::autoplaylist_core;
    use anyhow::*;
    use httpmock::prelude::*;
    use httpmock::MockServer;
    use log::LevelFilter;
    use serde_json::json;

    #[tokio::test]
    async fn new_style() -> Result<()> {
        let _ = env_logger::builder()
            .filter_module("autoplaylist_cli", LevelFilter::Info)
            .try_init();

        let mock = MockServer::start();

        let _get_playlists = mock.mock(|when, then| {
            when.method(GET)
                .path("/v1/me/playlists")
                .header("Authorization", "Bearer access_token")
                .query_param("limit", "50")
                .query_param("offset", "0");
            then.status(200)
                .header("content-type", "application/json")
                .json_body(json!({"items":[{"name":"playlist-name","id":"playlist-id", "description":"AP:Powerwolf"}]}));
        });

        let _search_artist = mock.mock(|when, then| {
            when.method(GET)
                .path("/v1/search")
                .header("Authorization", "Bearer access_token")
                .query_param("q", "Powerwolf")
                .query_param("type", "artist")
                .query_param("market", "SE");
            then.status(200)
                .header("content-type", "application/json")
                .json_body(json!({"artists": {"items":[{"name":"Powerwolf","id":"pw-id"}]}}));
        });

        let _get_albums = mock.mock(|when, then| {
            when.method(GET)
                .path("/v1/artists/pw-id/albums")
                .header("Authorization", "Bearer access_token")
                .query_param("market", "SE")
                .query_param("limit", "50")
                .query_param("include_groups", "album,single");
            then.status(200)
                .header("content-type", "application/json")
                .json_body(json!({"items":[{"name":"album-name","id":"album-id"}]}));
        });

        let _get_album_details = mock.mock(|when, then| {
            when.method(GET)
                .path("/v1/albums/album-id")
                .header("Authorization", "Bearer access_token")
                .query_param("market", "SE");
            then.status(200)
                .header("content-type", "application/json")
                .json_body(json!({"release_date":"1900-01-01"}));
        });

        let _get_tracks = mock.mock(|when, then| {
            when.method(GET)
                .path("/v1/albums/album-id/tracks")
                .header("Authorization", "Bearer access_token")
                .query_param("market", "SE")
            // .query_param("limit", "50") //todo should probably set limit here as well
            ;
            then.status(200)
                .header("content-type", "application/json")
                .json_body(json!({"items":[{"name":"track-name","id":"track-id"}]}));
        });

        let add_tracks_to_playlist = mock.mock(|when, then| {
            when.method(PUT)
                .path("/v1/playlists/playlist-id/tracks")
                .header("Authorization", "Bearer access_token")
                .json_body(json!({"uris":["spotify:track:track-id"]}));
            then.status(200)
                .header("content-type", "application/json")
                .json_body(json!({}));
        });

        let resp =
            autoplaylist_core::do_stuff2("access_token".to_string(), Some(mock.base_url())).await;
        println!("{:?}", resp);

        add_tracks_to_playlist.assert();

        Ok(())
    }

    #[tokio::test]
    async fn old_style() -> Result<()> {
        let mock = MockServer::start();

        let _search_artist = mock.mock(|when, then| {
            when.method(GET)
                .path("/v1/search")
                .header("Authorization", "Bearer access_token")
                .query_param("q", "Powerwolf")
                .query_param("type", "artist")
                .query_param("market", "SE");
            then.status(200)
                .header("content-type", "application/json")
                .json_body(json!({"artists": {"items":[{"name":"Powerwolf","id":"pw-id"}]}}));
        });

        let _get_albums = mock.mock(|when, then| {
            when.method(GET)
                .path("/v1/artists/pw-id/albums")
                .header("Authorization", "Bearer access_token")
                .query_param("market", "SE")
                .query_param("limit", "50")
                .query_param("include_groups", "album,single");
            then.status(200)
                .header("content-type", "application/json")
                .json_body(json!({"items":[{"name":"album-name","id":"album-id"}]}));
        });

        let _get_album_details = mock.mock(|when, then| {
            when.method(GET)
                .path("/v1/albums/album-id")
                .header("Authorization", "Bearer access_token")
                .query_param("market", "SE");
            then.status(200)
                .header("content-type", "application/json")
                .json_body(json!({"release_date":"1900-01-01"}));
        });

        let _get_tracks = mock.mock(|when, then| {
            when.method(GET)
                .path("/v1/albums/album-id/tracks")
                .header("Authorization", "Bearer access_token")
                .query_param("market", "SE")
            // .query_param("limit", "50") //todo should probably set limit here as well
            ;
            then.status(200)
                .header("content-type", "application/json")
                .json_body(json!({"items":[{"name":"track-name","id":"track-id"}]}));
        });

        let create_playlist = mock.mock(|when, then| {
            when.method(POST)
                .path("/v1/me/playlists")
                .header("Authorization", "Bearer access_token")
                .json_body(json!({"name":"Powerwolf - gen", "public":"false", "collaborative":"false", "description": "Created by autoplaylist-cli"}))
            ;
            then.status(200)
                .header("content-type", "application/json")
                .json_body(json!({"id":"playlist-id"}));
        });

        let add_tracks_to_playlist = mock.mock(|when, then| {
            when.method(PUT)
                .path("/v1/playlists/playlist-id/tracks")
                .header("Authorization", "Bearer access_token")
                .json_body(json!({"uris":["spotify:track:track-id"]}));
            then.status(200)
                .header("content-type", "application/json")
                .json_body(json!({}));
        });

        let resp = autoplaylist_core::do_stuff(
            "Powerwolf",
            "access_token".to_string(),
            Some(mock.base_url()),
        )
        .await;
        println!("{:?}", resp);

        create_playlist.assert();
        add_tracks_to_playlist.assert();

        Ok(())
    }
}
