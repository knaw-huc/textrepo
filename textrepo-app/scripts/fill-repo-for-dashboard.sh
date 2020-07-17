#!/usr/bin/env bash

set -x

tr=localhost:8080/textrepo

doc1=$(curl -s -X POST $tr/rest/documents -H 'Content-type: application/json' -d '{"externalId": "doc1"}' | jq -r .id)
doc2=$(curl -s -X POST $tr/rest/documents -H 'Content-type: application/json' -d '{"externalId": "doc2"}' | jq -r .id)
doc3=$(curl -s -X POST $tr/rest/documents -H 'Content-type: application/json' -d '{"externalId": "doc3"}' | jq -r .id)
doc4=$(curl -s -X POST $tr/rest/documents -H 'Content-type: application/json' -d '{"externalId": "doc4"}' | jq -r .id)
doc5=$(curl -s -X POST $tr/rest/documents -H 'Content-type: application/json' -d '{"externalId": "doc5"}' | jq -r .id)
doc6=$(curl -s -X POST $tr/rest/documents -H 'Content-type: application/json' -d '{"externalId": "doc6"}' | jq -r .id)

curl -sX POST $tr/rest/types -H 'Content-type: application/json' -d '{"name": "txt", "mimetype": "text/plain"}'

curl -X POST $tr/task/import/documents/doc1/txt -F "contents=@Dockerfile.builder"
curl -X POST $tr/task/import/documents/doc2/txt -F "contents=@Dockerfile.builder"

curl -XPUT -H 'Content-type: application/json' $tr/rest/documents/$doc1/metadata/aap -d noot
curl -XPUT -H 'Content-type: application/json' $tr/rest/documents/$doc3/metadata/aap -d mies
curl -XPUT -H 'Content-type: application/json' $tr/rest/documents/$doc4/metadata/wim -d zus
curl -XPUT -H 'Content-type: application/json' $tr/rest/documents/$doc4/metadata/jet -d teun
curl -XPUT -H 'Content-type: application/json' $tr/rest/documents/$doc5/metadata/aap -d noot
