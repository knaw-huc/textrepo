#!/usr/bin/env bash

set -x

tr=localhost:8080/textrepo

doc1=$(curl -s -X POST $tr/rest/documents -H 'Content-type: application/json' -d '{"externalId": "doc1"}' | jq -r .id)
doc2=$(curl -s -X POST $tr/rest/documents -H 'Content-type: application/json' -d '{"externalId": "doc2"}' | jq -r .id)
doc3=$(curl -s -X POST $tr/rest/documents -H 'Content-type: application/json' -d '{"externalId": "doc3"}' | jq -r .id)
doc4=$(curl -s -X POST $tr/rest/documents -H 'Content-type: application/json' -d '{"externalId": "doc4"}' | jq -r .id)

curl -sX POST $tr/rest/types -H 'content-type: application/json' -d '{"name": "txt", "mimetype": "text/plain"}'

curl -X POST $tr/task/import/documents/doc1/txt -F "contents=@Dockerfile.builder"
curl -X POST $tr/task/import/documents/doc2/txt -F "contents=@Dockerfile.builder"
curl -XPUT -H 'content-type: application/json' $tr/rest/documents/$doc1/metadata/aap -d noot
curl -XPUT -H 'content-type: application/json' $tr/rest/documents/$doc3/metadata/mies -d wim
