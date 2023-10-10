use std::collections::HashMap;
use crate::types::{Password, UserId, Username};
use anyhow::Result;
use log::info;
use uuid::Uuid;

#[derive(Default)]
pub struct UserService2 {
    users: HashMap<Username, (UserId, Password)>,
}

impl UserService2 {
    pub fn create_user(&mut self, username: Username, password: Password) -> Result<()> {
        let user_id = UserId(Uuid::new_v4()); // todo make sure it's unique
        self.users.insert(username, (user_id, password));
        info!("Persisted users: {:?}", self.users);
        Ok(())
    }

    pub fn is_password_valid(&self, username: &Username, password_input: &Password) -> bool {
        if let Some((_userid, password)) = self.users.get(&username) {
            password == password_input
        } else {
            false
        }
    }

    pub fn get_user_id(&self, username: &Username) -> Option<UserId> {
        self.users.get(username).map(|(id, _pass)|id.clone())
    }
}