#!/usr/bin/env bash
docker run --name autoplaylist-backends -it --rm -p 8080:8080 \
-e CLIENT_ID=id_goes_here \
-e CLIENT_SECRET=secret_goes_here \
-e REDIRECT_URL=http://localhost:8080/callback \
richodemus/autoplaylists-backend:latest
