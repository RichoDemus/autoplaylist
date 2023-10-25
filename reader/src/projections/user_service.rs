use std::collections::HashMap;
use std::env;
use std::sync::{Arc, Mutex};

use anyhow::Result;
use chrono::Utc;
use log::info;
use uuid::Uuid;

use crate::event::event_store::EventStore;
use crate::event::events::Event;
use crate::types::{EventId, Password, UserId, Username};

pub struct UserService {
    event_store: Arc<Mutex<EventStore>>,
    users: Arc<Mutex<HashMap<Username, (UserId, Password)>>>,
    password_override: Option<String>,
}

impl UserService {
    pub fn new(event_store: Arc<Mutex<EventStore>>, password_override: Option<String>) -> Self {
        let users: Arc<Mutex<HashMap<Username, (UserId, Password)>>> = Default::default();
        let users_spawn = users.clone();
        let mut receiver = event_store.lock().unwrap().receiver();
        actix_rt::spawn(async move {
            while let Some(event) = receiver.recv().await {
                if let Event::UserCreated {
                    id: _,
                    timestamp: _,
                    user_id,
                    username,
                    password,
                } = event
                {
                    let username = Username(username.to_lowercase());
                    users_spawn
                        .lock()
                        .unwrap()
                        .insert(username, (user_id, password));
                }
            }
        });
        Self {
            event_store,
            users,
            password_override,
        }
    }
    pub async fn create_user(&mut self, username: Username, password: Password) -> Result<()> {
        let user_id = UserId(Uuid::new_v4()); // todo make sure it's unique
                                              // self.users.insert(username.clone(), (user_id.clone(), password.clone()));
        self.event_store
            .lock()
            .unwrap()
            .publish_event(Event::UserCreated {
                id: EventId::default(),
                timestamp: Utc::now(),
                user_id,
                username,
                password,
            })
            .await
    }

    pub fn is_password_valid(&self, username: &Username, password_input: &Password) -> bool {
        let username = Username(username.to_lowercase());
        if let Some((_userid, password)) = self.users.lock().unwrap().get(&username) {
            if let Some(override_password) = self.password_override.as_ref() {
                return *override_password == **password_input;
            }
            password == password_input
        } else {
            info!("User Not found: {:?}", username);
            false
        }
    }

    pub fn get_user_id(&self, username: &Username) -> Option<UserId> {
        let username = Username(username.to_lowercase());
        self.users
            .lock()
            .unwrap()
            .get(&username)
            .map(|(id, _pass)| id.clone())
    }
}
