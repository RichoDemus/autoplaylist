use std::fs;
use std::fs::File;
use std::io::{Read, Write};
use std::path::Path;

use actix_web::web;
use anyhow::{Context, Result};

pub async fn read_file(filename: String) -> Option<Vec<u8>> {
    web::block(move || {
        let mut file = File::open(format!("data/{}", filename)).ok()?;

        let mut buffer = vec![];
        file.read_to_end(&mut buffer).ok()?;

        Some(buffer)
    })
    .await
    .ok()?
}

pub async fn write_file(filename: String, bytes: Vec<u8>) -> Result<()> {
    web::block(move || {
        if !Path::new("data/events/v2/").exists() {
            fs::create_dir_all("data/events/v2")?;
        }
        let mut file = File::create(format!("data/{}", filename))
            .context("Failed to open event disk file handler")?;

        file.write_all(bytes.as_slice())
            .context("Failed to write event to disk")?;
        Ok(())
    })
    .await?
}

// #[cfg(test)]
// mod tests {
// use super::*;

// #[actix_web::test]
// async fn test() {
//     assert!(read_file("test.txt".to_string()).await.is_none() );
//
//     assert!(write_file("test.txt".to_string(), "hello".as_bytes().to_vec()).await.is_ok());
//
//     assert_eq!(read_file("test.txt".to_string()).await, Some("hello".as_bytes().to_vec()));
// }
// }
