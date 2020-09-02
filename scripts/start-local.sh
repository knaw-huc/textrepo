#!/usr/bin/env bash
set -e

source docker-compose.env
docker-compose -f docker-compose-local.yml up --no-build -d \
  postgres \
  elasticsearch \
  nginx \
  autocomplete-indexer \
  full-text-indexer \
  textrepo-app

echo "================================================================================"
echo "         TAILING LOGS. Hit ^C to abort, leaving services running"
echo "================================================================================"
docker-compose -f docker-compose-local.yml logs -f
