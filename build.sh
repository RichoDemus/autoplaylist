#!/usr/bin/env sh
(cd autoplaylist-backend && docker build -t richodemus/autoplaylist-backend:latest .)
(cd autoplaylist-frontend && docker build -t richodemus/autoplaylist-frontend:latest .)
