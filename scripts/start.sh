#!/usr/bin/env bash
set -x

source docker-compose.env
docker-compose up --build
