use std::collections::HashMap;
use std::sync::{Arc, Mutex};

use anyhow::Result;
use bcrypt::{hash, verify, DEFAULT_COST};
use chrono::Utc;
use log::info;
use uuid::Uuid;

use crate::event::event_store::EventStore;
use crate::event::events::Event;
use crate::types::{EventId, HashedPassword, Password, UserId, Username};

pub struct UserService {
    event_store: Arc<Mutex<EventStore>>,
    users: Arc<Mutex<HashMap<Username, (UserId, HashedPassword)>>>,
}

impl UserService {
    pub fn new(event_store: Arc<Mutex<EventStore>>) -> Self {
        let users: Arc<Mutex<HashMap<Username, (UserId, HashedPassword)>>> = Default::default();
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
        Self { event_store, users }
    }
    pub async fn create_user(&mut self, username: Username, password: Password) -> Result<()> {
        let user_id = UserId(Uuid::new_v4()); // todo make sure it's unique

        // self.users.insert(username.clone(), (user_id.clone(), password.clone()));
        let password = HashedPassword(hash(password.0, DEFAULT_COST).unwrap());
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
            verify(password_input.0.as_str(), password).unwrap()
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
