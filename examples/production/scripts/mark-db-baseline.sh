#!/usr/bin/env bash

# Mark current state of database as baseline
# to prevent a restart from clearing and resetting the database.
# Check repo:./textrepo-app/db for current migration versions

if [[ -z "$1" ]]; then
  echo 'usage: `./mark-db-baseline.sh <version>`'
  echo 'e.g: `./mark-db-baseline.sh 001` marks file starting with `V001__` as baseline'
  exit
fi

source docker-compose.env

docker run \
  --rm \
  --network textrepo_network \
  flyway/flyway:7.10 \
  -url=${TR_DATABASE_URL} \
  -user=${TR_DATABASE_USER} \
  -password=${TR_DATABASE_PASSWORD} \
  baseline -baselineVersion=${1}
