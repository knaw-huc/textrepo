#!/usr/bin/env bash

source docker-compose.env
docker-compose -f docker-compose-prod.yml up --no-build -d

