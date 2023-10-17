extern crate core;

use crate::endpoints::admin::download;
use crate::endpoints::feeds::{add_feed, get_all_feeds, get_videos};
use crate::endpoints::serve_assets::static_fie;
use actix_cors::Cors;
use actix_session::storage::CookieSessionStore;
use actix_session::SessionMiddleware;
use actix_web::cookie::Key;
use actix_web::middleware::Logger;
use actix_web::{cookie, web, App, HttpServer};
use log::LevelFilter;

use crate::endpoints::user::{create_user, login};
use crate::event::event_store;
use crate::service::Services;

pub mod endpoints;
pub mod event;
mod gcs;
pub mod projections;
pub mod service;
pub mod sled_wrapper;
#[cfg(test)]
pub mod test;
pub mod types;
pub mod youtube;

// #[get("/")]
// async fn index(_session: Session) -> impl Responder {
//     "Hello, World!"
// }

#[actix_web::main]
async fn main() -> anyhow::Result<()> {
    let _ = env_logger::builder()
        .filter_module("reader", LevelFilter::Info)
        .try_init();

    // event_store::init().await?;
    let secret_key = Key::from(&[0; 64]); // todo use proper key
    let state = web::Data::new(Services::default());
    HttpServer::new(move || {
        App::new()
            .app_data(state.clone())
            .wrap(Logger::default())
            .wrap(
                SessionMiddleware::builder(CookieSessionStore::default(), secret_key.clone())
                    .cookie_secure(false)
                    .session_lifecycle(
                        actix_session::config::PersistentSession::default()
                            .session_ttl(cookie::time::Duration::days(365)),
                    )
                    .build(),
            )
            .wrap(
                Cors::default()
                    .supports_credentials()
                    .allow_any_origin()
                    .allow_any_method()
                    .allow_any_header(),
            )
            .service(create_user)
            .service(login)
            .service(get_all_feeds)
            .service(get_videos)
            .service(add_feed)
            .service(download)
            .route("/{filename:.*}", web::get().to(static_fie))
    })
    .bind(("0.0.0.0", 8080))?
    .run()
    .await?;

    // if true {
    //     return Ok(());
    // }
    //
    // // snippet-start:[s3.rust.client-client]
    // let region_provider = RegionProviderChain::default_provider().or_else("us-east-1");
    // let config = aws_config::from_env().region(region_provider).load().await;
    // let client = Client::new(&config);
    // // snippet-end:[s3.rust.client-client]
    //
    // let bucket_name = {
    //     let resp = client.list_buckets().send().await?;
    //     let buckets = resp.buckets().unwrap();
    //
    //     let bucket_name = buckets
    //         .iter()
    //         .map(|b| b.name().unwrap().to_string())
    //         .find(|b| b == "richo-reader")
    //         .unwrap();
    //     bucket_name
    // };
    //
    // let mut ids = vec![];
    // let objects = client
    //     .list_objects_v2()
    //     .bucket(&bucket_name)
    //     .max_keys(10_000)
    //     .send()
    //     .await?;
    // let mut cont_token = objects.next_continuation_token().map(ToString::to_string);
    // let mut new_ids = get_ids(&objects);
    // ids.append(&mut new_ids);
    //
    // loop {
    //     match cont_token {
    //         None => {
    //             break;
    //         }
    //         Some(token) => {
    //             let objects = client
    //                 .list_objects_v2()
    //                 .bucket(&bucket_name)
    //                 .max_keys(10_000)
    //                 .continuation_token(token)
    //                 .send()
    //                 .await?;
    //             cont_token = objects.next_continuation_token().map(ToString::to_string);
    //             let mut new_ids = get_ids(&objects);
    //             ids.append(&mut new_ids);
    //         }
    //     }
    // }
    //
    // println!("{bucket_name}: {}", ids.len());
    // let mut ids_num = ids
    //     .iter()
    //     .filter(|id| id.starts_with("events/v2"))
    //     .filter_map(|id| id[10..].parse::<i32>().ok())
    //     .collect::<Vec<_>>();
    // ids_num.sort_unstable();
    // let mut last_num = -1;
    // for num in &ids_num {
    //     if last_num + 1 == *num {
    //         last_num = *num;
    //     } else {
    //         panic!("Missing event {}", last_num + 1)
    //     }
    // }
    // // println!("{:?}", ids_num);
    //
    // let tot_events = ids_num.len();
    // let finished_events_counter = Arc::new(AtomicUsize::new(0));
    // let fe = finished_events_counter.clone();
    //
    // tokio::spawn(async move {
    //     let mut v = VecDeque::new();
    //     let mut last_events = 0;
    //
    //     loop {
    //         let events = fe.load(Ordering::SeqCst);
    //         while v.len() > 10 {
    //             v.pop_front();
    //         }
    //         v.push_back(events - last_events);
    //
    //         let events_per_second: usize = v.iter().sum::<usize>() / v.len();
    //
    //         let events_left = tot_events - events;
    //         let seconds_left = chrono::Duration::seconds(
    //             events_left
    //                 .checked_div(events_per_second)
    //                 .unwrap_or_default() as i64,
    //         );
    //
    //         println!(
    //             "Events: {events}/{tot_events} {events_per_second} events/s. {}:{}",
    //             seconds_left.num_minutes(),
    //             seconds_left.num_seconds() % 60
    //         );
    //         if events >= tot_events {
    //             break;
    //         }
    //         last_events = events;
    //         tokio::time::sleep(Duration::from_secs(1)).await;
    //     }
    //     println!("Downloaded {} events", fe.load(Ordering::SeqCst));
    // });

    // let mut events = vec![];
    // for id in ids_num {
    //     let key = format!("events/v2/{id}");
    //     let raw = client
    //         .get_object()
    //         .bucket(&bucket_name)
    //         .key(key)
    //         .send();
    //     events.push(async {
    //         let res = raw.await;
    //         finished_events_counter.fetch_add(1, Ordering::SeqCst);
    //         res
    //     });
    // }
    //
    // let events = join_all(events).await;
    //
    // let mut finished_events = vec![];
    // for event in events {
    //     let data = event?.body.collect().await.unwrap();
    //     let v = data.to_vec();
    //
    //     let s = String::from_utf8(v).unwrap();
    //     let (_id, event) = s.split_once(',').unwrap();
    //
    //     // println!("{id} - {event}");
    //     let event: Event = parse::parse(event)?;
    //     finished_events.push(event);
    // }

    // let mut events = vec![];
    // for id in ids_num {
    //     let key = format!("events/v2/{id}");
    //     let raw = client
    //         .get_object()
    //         .bucket(&bucket_name)
    //         .key(key)
    //         .send()
    //         .await?;
    //     let data = raw.body.collect().await.unwrap();
    //     let v = data.to_vec();
    //
    //     let s = String::from_utf8(v).unwrap();
    //     let (_id, event) = s.split_once(',').unwrap();
    //
    //     // println!("{id} - {event}");
    //     let event: Event = parse::parse(event)?;
    //     events.push(event);
    //     finished_events_counter.store(id as usize, Ordering::SeqCst);
    // }
    //
    // // for event in events {
    // //     println!("{event:?}");
    // // }
    //
    // println!("done");

    Ok(())
}

// fn get_ids(objects: &ListObjectsV2Output) -> Vec<String> {
//     objects
//         .contents()
//         .context("contents")
//         .unwrap()
//         .iter()
//         .map(|o| o.key().context("Getting key").unwrap().to_string())
//         .collect::<Vec<_>>()
// }
