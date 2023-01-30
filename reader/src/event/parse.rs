use anyhow::{Context, Result};

use crate::event::events::Event;

#[allow(clippy::missing_errors_doc)]
pub fn parse(text: &str) -> Result<Event> {
    serde_json::from_str(text)
        .with_context(|| format!("Unable to parse event {}", parse_type(text)))
}

#[allow(clippy::missing_errors_doc)]
pub fn to_data(event: &Event) -> Result<String> {
    serde_json::to_string(event).context("to_data")
}

fn parse_type(str: &str) -> String {
    let start = str.find("type\":\"").unwrap();
    let (_, str) = str.split_at(start);
    let str = &str[7..];
    let end = str.find('"').unwrap();
    let (str, _) = str.split_at(end);
    str.to_string()
}

#[cfg(test)]
mod tests {
    use uuid::Uuid;

    use crate::event::events::Event;
    use crate::event::parse::{parse, to_data};
    use crate::types::{EventId, UserId};

    #[test]
    fn test_parse() {
        let event = Event::UserWatchedItem {
            id: EventId(Uuid::nil()),
            timestamp: Default::default(),
            user_id: UserId(Uuid::nil()),
            feed_id: "feed".to_string().into(),
            item_id: "item".to_string().into(),
        };

        let raw = to_data(&event).unwrap();
        assert_eq!(
            raw.as_str(),
            r#"{"type":"USER_WATCHED_ITEM","id":"00000000-0000-0000-0000-000000000000","timestamp":"1970-01-01T00:00:00Z","userId":"00000000-0000-0000-0000-000000000000","feedId":"feed","itemId":"item"}"#
        );
        let result = parse(raw.as_str()).unwrap();

        assert_eq!(event, result);
    }
}
