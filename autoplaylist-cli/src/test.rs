#[cfg(test)]
mod tests {
    use anyhow::*;
    use httpmock::MockServer;
    use httpmock::prelude::*;
    use serde_json::json;
    use warp::test::request;
    use crate::spotify::autoplaylist_core;

    #[tokio::test]
    async fn it_works() -> Result<()> {
        let mock = MockServer::start();

        let search_artist = mock.mock(|when, then| {
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

        let get_albums = mock.mock(|when, then| {
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

        let get_album_details = mock.mock(|when, then| {
            when.method(GET)
                .path("/v1/albums/album-id")
                .header("Authorization", "Bearer access_token")
                .query_param("market", "SE");
            then.status(200)
                .header("content-type", "application/json")
                .json_body(json!({"release_date":"1900-01-01"}));
        });

        let get_tracks = mock.mock(|when, then| {
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


        autoplaylist_core::do_stuff("Powerwolf","access_token".to_string(), Some(mock.base_url())).await?;


        Ok(())
    }
}
