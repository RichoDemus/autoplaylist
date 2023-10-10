use std::sync::Mutex;
use crate::projections::user_service2::UserService2;

pub struct Services {
    pub user_service: Mutex<UserService2>,
}

impl Default for Services {
    fn default() -> Self {
        Self {
            user_service: Mutex::new(UserService2::default()),
        }
    }
}




