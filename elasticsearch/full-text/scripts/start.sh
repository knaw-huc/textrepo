#!/bin/sh
envsubst < /indexer/config-template.yml > /indexer/config.yml

java -jar full-text.jar server config.yml
