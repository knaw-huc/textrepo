#!/usr/bin/env bash
set -e

HOST=localhost:8080/textrepo/rest
OLD_CONTENT=./scripts/example.old.txt
NEW_CONTENT=./scripts/example.new.txt

# Check example files exist:
if [[ ! -f $OLD_CONTENT ]] ; then echo "File '$OLD_CONTENT' not found, aborting."; exit; fi
if [[ ! -f $NEW_CONTENT ]] ; then echo "File '$NEW_CONTENT' not found, aborting."; exit; fi


# Create text and xml file types:

TEXT_TYPE_ID=$(curl "$HOST/types" \
  -H 'content-type:application/json' \
  -d '{"name": "text", "mimetype": "text/plain"}' | jq -r '.id')
echo "Created text type with id: $TEXT_TYPE_ID"

XML_TYPE_ID=$(curl "$HOST/types" \
  -H 'content-type:application/json' \
  -d '{"name": "xml", "mimetype": "application/xml"}' | jq -r '.id')
echo "Created xml type with id: $XML_TYPE_ID"


# Create two documents:

DOC_ID=$(curl "$HOST/documents" \
  -H 'content-type:application/json' \
  -d '{"externalId": "example-external-id"}' | jq -r '.id')
echo "Created document with id: $DOC_ID"

curl "$HOST/documents" \
  -H 'content-type:application/json' \
  -d '{"externalId": "other-example-external-id"}'


# Add xml and text files to document with $DOC_ID:

TEXT_FILE_ID=$(curl "$HOST/files" \
  -H 'content-type:application/json' \
  -d "{\"docId\": \"$DOC_ID\", \"typeId\": \"$TEXT_TYPE_ID\"}" | jq -r '.id')
echo "Created text file with id: $TEXT_FILE_ID"

XML_FILE_ID=$(curl "$HOST/files" \
  -H 'content-type:application/json' \
  -d "{\"docId\": \"$DOC_ID\", \"typeId\": \"$XML_TYPE_ID\"}" | jq -r '.id')
echo "Created xml file with id: $XML_FILE_ID"


# Add two versions to text file:

OLD_VERSION_ID=$(curl "$HOST/versions" \
  -F "fileId=$TEXT_FILE_ID" \
  -F "contents=@$OLD_CONTENT" | jq -r '.id')
echo "Created old version with id: $OLD_VERSION_ID"

NEW_VERSION_ID=$(curl "$HOST/versions" \
  -F "fileId=$TEXT_FILE_ID" \
  -F "contents=@$NEW_CONTENT" | jq -r '.id')
echo "Created new version with id: $NEW_VERSION_ID"

