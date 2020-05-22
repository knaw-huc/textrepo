#!/usr/bin/env bash
set -x

source docker-compose.env
envsubst < docker-compose.yml "$SUBSTITUTE_IN_COMPOSE_FILE" > docker-compose-subst.yml
