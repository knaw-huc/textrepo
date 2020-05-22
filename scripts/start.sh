#!/usr/bin/env bash
set -x

source ./scripts/set-env.sh

docker-compose -f docker-compose-subst.yml up --build
