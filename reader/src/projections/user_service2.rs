use crate::event::event_store::EventStore;
use crate::event::events::Event;
use crate::types::{EventId, Password, UserId, Username};
use anyhow::Result;
use log::{error, info};
use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use tokio::sync::broadcast::error::RecvError;
use uuid::Uuid;

pub struct UserService2 {
    event_store: Arc<EventStore>,
    users: Arc<Mutex<HashMap<Username, (UserId, Password)>>>,
}

impl UserService2 {
    pub fn new(event_store: Arc<EventStore>) -> Self {
        let users: Arc<Mutex<HashMap<Username, (UserId, Password)>>> = Default::default();
        let users_spawn = users.clone();
        let mut receiver = event_store.receiver();
        actix_rt::spawn(async move {
            loop {
                match receiver.recv().await {
                    Ok(event) => {
                        info!("Received event {event:?}");
                        if let Event::UserCreated {
                            id,
                            timestamp,
                            user_id,
                            username,
                            password,
                        } = event
                        {
                            users_spawn
                                .lock()
                                .unwrap()
                                .insert(username, (user_id, password));
                        }
                    }

                    Err(RecvError::Closed) => {
                        info!("closed");
                        break;
                    }
                    Err(RecvError::Lagged(x)) => {
                        error!("lagged {x}, very bad!");
                    }
                }
            }
        });
        Self { event_store, users }
    }
    pub fn create_user(&mut self, username: Username, password: Password) -> Result<()> {
        let user_id = UserId(Uuid::new_v4()); // todo make sure it's unique
                                              // self.users.insert(username.clone(), (user_id.clone(), password.clone()));
        self.event_store.publish_event(Event::UserCreated {
            id: EventId::default(),
            timestamp: Default::default(),
            user_id,
            username,
            password,
        })?;
        Ok(())
    }

    pub fn is_password_valid(&self, username: &Username, password_input: &Password) -> bool {
        if let Some((_userid, password)) = self.users.lock().unwrap().get(&username) {
            password == password_input
        } else {
            false
        }
    }

    pub fn get_user_id(&self, username: &Username) -> Option<UserId> {
        self.users
            .lock()
            .unwrap()
            .get(username)
            .map(|(id, _pass)| id.clone())
    }
}
