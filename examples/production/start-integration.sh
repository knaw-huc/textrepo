#!/usr/bin/env bash
set -e

source docker-compose.env
docker-compose -f docker-compose-integration.yml down -v
docker-compose -f docker-compose-integration.yml up --no-build -d

