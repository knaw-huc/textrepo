#!/usr/bin/env bash
set -e

if [[ -z "$1" ]] || ([[ "$1" != 'prod' ]] && [[ "$1" != 'integration' ]]);
  then echo 'usage: ./log.sh (prod|integration) [<tail>]'; exit;
fi

source docker-compose.env

docker-compose -f "docker-compose-${1}.yml" logs -f --tail=${2:-all}
