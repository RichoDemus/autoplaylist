use actix_session::{Session, SessionGetError};
use anyhow::anyhow;

use crate::types::UserId;

impl TryFrom<Session> for UserId {
    type Error = SessionGetError;

    fn try_from(value: Session) -> Result<Self, Self::Error> {
        value
            .get::<UserId>("user_id")
            .and_then(|id| id.ok_or(anyhow!("no user id").into()))
    }
}
