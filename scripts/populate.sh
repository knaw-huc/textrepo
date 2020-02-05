#!/usr/bin/env bash
set -e

HOST=localhost:8080/textrepo/rest

# Create 'text' and 'xml' file types:
TYPE_ID=$(curl "$HOST/types" \
  -H 'content-type:application/json' \
  -d '{"name": "text", "mimetype": "text/plain"}' | jq '.id')

echo "Created text type with id: $TYPE_ID"

curl "$HOST/types" \
  -H 'content-type:application/json' \
  -d '{"name": "xml", "mimetype": "application/xml"}'

# Create document:
DOC_ID=$(curl "$HOST/documents" \
  -H 'content-type:application/json' \
  -d '{"externalId": "example-external-id"}' | jq '.id')

echo "Created document with id: $DOC_ID"

# Create file:
FILE_ID=$(curl "$HOST/files" \
  -H 'content-type:application/json' \
  -d "{\"docId\": $DOC_ID, \"typeId\": $TYPE_ID}" | jq '.id')

echo "Created file with id: $FILE_ID"

