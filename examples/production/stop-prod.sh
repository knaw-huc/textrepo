#!/usr/bin/env bash

# careful with "set -e", see below
# set -e
set -x
source docker-compose.env

# First stop textrepo-app, so it drops its connection to postgres
docker-compose -f docker-compose-prod.yml stop textrepo-app

# Then gracefully stop postgres, waiting until it is done.
docker-compose -f docker-compose-prod.yml exec -u postgres postgres pg_ctl stop --mode=smart --wait

# Note that pg_ctl exits with non-zero exit code 137, so if this script is run with "set -e"
# then it will end here (thinking there was an error), leaving the rest of the stack running.
# Finally bring the rest of the stack down
docker-compose -f docker-compose-prod.yml stop
