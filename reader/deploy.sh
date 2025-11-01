#!/bin/bash
set -e

if [ -z "$1" ]; then
  echo "Usage: $0 <youtube_api_key>"
  exit 1
fi

YOUTUBE_API_KEY=$1
INSTALL_DIR="/usr/local/bin"
SERVICE_NAME="reader"
BINARY_NAME="reader"

echo "Pulling latest changes..."
git pull

echo "Building release binary..."
cargo build --release

echo "Installing binary..."
sudo cp target/release/$BINARY_NAME $INSTALL_DIR

echo "Creating systemd service file..."
sudo bash -c "cat > /etc/systemd/system/$SERVICE_NAME.service" << EOL
[Unit]
Description=Reader Service
After=network.target

[Service]
User=$(whoami)
Group=$(id -gn)
WorkingDirectory=$(pwd)
ExecStart=$INSTALL_DIR/$BINARY_NAME --youtube-api-key $YOUTUBE_API_KEY
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOL

echo "Reloading systemd, enabling and starting service..."
sudo systemctl daemon-reload
sudo systemctl enable $SERVICE_NAME
sudo systemctl start $SERVICE_NAME

echo "Deployment complete!"
