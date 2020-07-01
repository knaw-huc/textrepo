#!/usr/bin/env bash
set -x

source docker-compose.env
docker-compose -f docker-compose-dev.yml up --build
