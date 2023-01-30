use std::collections::HashMap;
use std::sync::Mutex;

use anyhow::{bail, Result};
use log::warn;
use once_cell::sync::Lazy;
use uuid::Uuid;

use crate::event::event_store;
use crate::event::events::Event;
use crate::types::{EventId, Password, UserId, Username};

static USERS: Lazy<Mutex<HashMap<Username, (UserId, Password)>>> =
    Lazy::new(|| Mutex::new(HashMap::new()));

pub fn process_event(event: &Event) {
    if let Event::UserCreated {
        id: _,
        timestamp: _,
        user_id,
        username,
        password,
    } = event
    {
        let mut subs = USERS.lock().unwrap();
        if subs.contains_key(username) {
            warn!("User {:?} already exists, skipping...", username);
            return;
        }
        subs.insert(username.clone(), (user_id.clone(), password.clone()));
    }
}

pub async fn create_user(username: Username, password: Password) -> Result<UserId> {
    if USERS.lock().unwrap().contains_key(&username) {
        bail!("User {:?} already exists", username);
    }

    let user_id = UserId(Uuid::new_v4()); // todo make sure it's unique
    let e = Event::UserCreated {
        id: EventId(Uuid::new_v4()),
        timestamp: Default::default(),
        user_id: user_id.clone(),
        username,
        password,
    };
    event_store::publish_event(e).await?;
    Ok(user_id)
}

pub fn is_password_valid(username: &Username, password_input: &Password) -> bool {
    if let Some((_id, password)) = USERS.lock().unwrap().get(username) {
        password == password_input
    } else {
        false
    }
}
