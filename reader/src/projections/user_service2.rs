use crate::event::event_store::EventStore;
use crate::event::events::Event;
use crate::types::{EventId, Password, UserId, Username};
use anyhow::Result;
use log::{error, info};
use std::collections::HashMap;
use std::env;
use std::sync::{Arc, Mutex};
use tokio::sync::broadcast::error::RecvError;
use uuid::Uuid;

pub struct UserService2 {
    event_store: Arc<Mutex<EventStore>>,
    users: Arc<Mutex<HashMap<Username, (UserId, Password)>>>,
}

impl UserService2 {
    pub fn new(event_store: Arc<Mutex<EventStore>>) -> Self {
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
        Self { event_store, users }
    }
    pub async fn create_user(&mut self, username: Username, password: Password) -> Result<()> {
        let user_id = UserId(Uuid::new_v4()); // todo make sure it's unique
                                              // self.users.insert(username.clone(), (user_id.clone(), password.clone()));
        self.event_store
            .lock()
            .unwrap()
            .publish_event(Event::UserCreated {
                id: EventId::default(),
                timestamp: Default::default(),
                user_id,
                username,
                password,
            })
            .await
    }

    pub fn is_password_valid(&self, username: &Username, password_input: &Password) -> bool {
        let username = Username(username.to_lowercase());
        if let Some((_userid, password)) = self.users.lock().unwrap().get(&username) {
            if let Ok(override_password) = env::var("PASSWORD_OVERRIDE") {
                return override_password == **password_input;
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
