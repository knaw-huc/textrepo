#!/bin/sh
envsubst < /indexer/config-template.yml > /indexer/config.yml

java -jar /indexer/autocomplete.jar server config.yml
