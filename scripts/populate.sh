#!/usr/bin/env bash
set -e

HOST=localhost:8080/textrepo/rest


# Create text and xml file types:

TEXT_TYPE_ID=$(curl "$HOST/types" \
  -H 'content-type:application/json' \
  -d '{"name": "text", "mimetype": "text/plain"}' | jq '.id')
echo "Created text type with id: $TEXT_TYPE_ID"

XML_TYPE_ID=$(curl "$HOST/types" \
  -H 'content-type:application/json' \
  -d '{"name": "xml", "mimetype": "application/xml"}' | jq '.id')
echo "Created xml type with id: $XML_TYPE_ID"


# Create two documents:

DOC_ID=$(curl "$HOST/documents" \
  -H 'content-type:application/json' \
  -d '{"externalId": "example-external-id"}' | jq '.id')
echo "Created document with id: $DOC_ID"

curl "$HOST/documents" \
  -H 'content-type:application/json' \
  -d '{"externalId": "other-example-external-id"}'


# Add xml and text files to document with $DOC_ID:

TEXT_FILE_ID=$(curl "$HOST/files" \
  -H 'content-type:application/json' \
  -d "{\"docId\": $DOC_ID, \"typeId\": $TEXT_TYPE_ID}" | jq '.id')
echo "Created text file with id: $TEXT_FILE_ID"

XML_FILE_ID=$(curl "$HOST/files" \
  -H 'content-type:application/json' \
  -d "{\"docId\": $DOC_ID, \"typeId\": $XML_TYPE_ID}" | jq '.id')
echo "Created xml file with id: $XML_FILE_ID"
