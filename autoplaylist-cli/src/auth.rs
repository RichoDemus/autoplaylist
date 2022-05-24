use std::str::FromStr;
use std::sync::{Arc, Mutex};

use anyhow::{bail, Ok, Result};
use log::{error, info};
use serde::Deserialize;
use serde::Serialize;
use warp::http::Uri;
use warp::{Filter, Rejection};

const LOGIN_URL: &str = "https://accounts.spotify.com/authorize?response_type=code&client_id=df0732a2defe44ecabd30868fa57a2d5&scope=user-read-private&redirect_uri=http://localhost:8080/callback&state=hello";

#[derive(Debug, Deserialize, Serialize)]
struct CallbackParameters {
    code: String,
    state: String,
}

pub async fn get_new_refresh_token() -> Result<String> {
    let verifier = pkce::code_verifier(43);
    let challenge = pkce::code_challenge(&verifier);
    let verifier_str = String::from_utf8(verifier)?;
    // panic!("verifier: {:?} challgen: {:?}", verifier_str, challenge);
    let login_url = format!(
        "{}&code_challenge_method=S256&code_challenge={}",
        LOGIN_URL, challenge
    );
    let (new_refresh_token, listen_for_refresh_token) = tokio::sync::oneshot::channel();
    let new_refresh_token = Arc::new(Mutex::new(Some(new_refresh_token)));

    let (do_shutdown, listen_for_shutdown) = tokio::sync::oneshot::channel();
    let shutdown_hook = Arc::new(Mutex::new(Some(do_shutdown)));

    let callback = warp::get()
        .and(warp::path("callback"))
        .and(warp::query::<CallbackParameters>())
        .and_then(move |p: CallbackParameters| {
            let shutdown_hook = shutdown_hook.clone();
            let new_refresh_token = new_refresh_token.clone();
            let verifier_str = verifier_str.clone();
            async move {
                // let code = p.code;
                info!("callback: {:?}", p);
                let token = exchange_code_for_token(p.code.as_str(), verifier_str.as_str())
                    .await
                    .expect("todo anyhow <-> warp");
                info!("token: {:?}", token);
                // let token = exchange_code_for_token(code.as_str()).await;
                // Response::builder().body(format!("callback: {:?}", p))

                // test
                // let mut client = EveClient {
                //     token: token.clone(),
                //     ..Default::default()
                // };
                // client.ping().await;
                // let x = client.get_current_character_id().await;
                // let (price, wallet) = client.get_assets().await;
                // info!("Total: {} - {}", (price.buy + wallet).to_formatted_string(&Locale::en), (price.sell + wallet).to_formatted_string(&Locale::en));
                // core::result::Result::Ok(Response::builder().body(format!("price")))
                if let Some(tx) = shutdown_hook.clone().lock().unwrap().take() {
                    tx.send(()).unwrap();
                }
                if let Some(sender) = new_refresh_token.lock().unwrap().take() {
                    sender.send(token).unwrap();
                }
                std::result::Result::Ok::<_, Rejection>(warp::reply())
            }
        });

    let test = warp::path::end() // matches /
        .map(move || {
            warp::redirect::found(Uri::from_str(login_url.as_str()).expect("should work"))
        });

    info!("Please visit http://localhost:8080/");
    let (_addr, server) =
        warp::serve(test.or(callback)).bind_with_graceful_shutdown(([0, 0, 0, 0], 8080), async {
            listen_for_shutdown.await.ok();
        });
    server.await;

    let refresh_token = listen_for_refresh_token.await?;

    Ok(refresh_token)
}

#[derive(Debug, Serialize, Deserialize)]
pub struct TokenResponse {
    pub access_token: String,
    pub expires_in: i32,
    pub token_type: String,
    pub refresh_token: String,
}

pub async fn exchange_code_for_token(code: &str, code_verifier: &str) -> Result<String> {
    let response = reqwest::Client::new()
        .post("https://accounts.spotify.com/api/token")
        .form(&[
            ("grant_type", "authorization_code"),
            ("code", code),
            ("redirect_uri", "http://localhost:8080/callback"),
            ("code_verifier", code_verifier),
            ("client_id", "df0732a2defe44ecabd30868fa57a2d5"),
        ])
        .send()
        .await?;
    info!("Response status {}", response.status());
    if response.status().is_success() {
        let token: TokenResponse = response.json().await.unwrap();
        info!("Resp: {:?}", token);
        Ok(token.refresh_token)
    } else {
        let e = response.text().await?;
        error!("E: {:?}", e);
        bail!(format!("e: {:?}", e));
    }
}

pub async fn get_access_token(refresh_token: &str) -> Result<TokenResponse> {
    let response = reqwest::Client::new()
        .post("https://accounts.spotify.com/api/token")
        .form(&[
            ("grant_type", "refresh_token"),
            ("refresh_token", refresh_token),
            ("client_id", "df0732a2defe44ecabd30868fa57a2d5"),
        ])
        .send()
        .await?;
    info!("Response status {}", response.status());
    if response.status().is_success() {
        let token: TokenResponse = response.json().await.unwrap();
        info!("Resp: {:?}", token);
        Ok(token)
    } else {
        let e = response.text().await?;
        error!("E: {:?}", e);
        bail!(format!("e: {:?}", e));
    }
}
