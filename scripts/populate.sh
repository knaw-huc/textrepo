#!/usr/bin/env bash
set -e

HOST=localhost:8080/textrepo/rest
OLD_CONTENT=./scripts/example.old.txt
NEW_CONTENT=./scripts/example.new.txt
XML_CONTENT=./scripts/example.xml

# Check example files exist:
if [[ ! -f $OLD_CONTENT ]] ; then echo "File '$OLD_CONTENT' not found, aborting."; exit; fi
if [[ ! -f $NEW_CONTENT ]] ; then echo "File '$NEW_CONTENT' not found, aborting."; exit; fi
if [[ ! -f $XML_CONTENT ]] ; then echo "File '$XML_CONTENT' not found, aborting."; exit; fi


# Create text and xml file types:

TEXT_TYPE_ID=$(curl -s "$HOST/types" \
  -H 'content-type:application/json' \
  -d '{"name": "text", "mimetype": "text/plain"}' | jq -r '.id')
echo "Created text type with id: $TEXT_TYPE_ID"

XML_TYPE_ID=$(curl -s "$HOST/types" \
  -H 'content-type:application/json' \
  -d '{"name": "xml", "mimetype": "application/xml"}' | jq -r '.id')
echo "Created xml type with id: $XML_TYPE_ID"


# Create two documents:

DOC_ID=$(curl -s "$HOST/documents" \
  -H 'content-type:application/json' \
  -d '{"externalId": "example-external-id"}' | jq -r '.id')
echo "Created document with id: $DOC_ID"

SECOND_DOC_ID=$(curl -s "$HOST/documents" \
  -H 'content-type:application/json' \
  -d '{"externalId": "other-example-external-id"}' | jq -r '.id')
echo "Created second document with id: $DOC_ID"


# Add metadata to document:

DOC_METADATA_KEY='test-doc-key'
curl -s -X PUT \
  "$HOST/documents/$DOC_ID/metadata/$DOC_METADATA_KEY" \
  -H 'Content-Type: text/plain' \
  -d 'test-value' \
  > /dev/null
echo "Created document metadata with key: $DOC_METADATA_KEY"


# Add xml and text files to document with $DOC_ID:

TEXT_FILE_ID=$(curl -s "$HOST/files" \
  -H 'content-type:application/json' \
  -d "{\"docId\": \"$DOC_ID\", \"typeId\": \"$TEXT_TYPE_ID\"}" | jq -r '.id')
echo "Created text file with id: $TEXT_FILE_ID"

FILE_METADATA_KEY='test-file-key'
curl -s -X PUT \
  "localhost:8080/textrepo/rest/files/$TEXT_FILE_ID/metadata/$FILE_METADATA_KEY" \
  -d 'test-value' \
  -H 'Content-Type: text/plain' \
  > /dev/null
echo "Created file metadata with key: $FILE_METADATA_KEY"

XML_FILE_ID=$(curl -s "$HOST/files" \
  -H 'content-type:application/json' \
  -d "{\"docId\": \"$DOC_ID\", \"typeId\": \"$XML_TYPE_ID\"}" | jq -r '.id')
echo "Created xml file with id: $XML_FILE_ID"


# Add two versions to text file:

OLD_VERSION_ID=$(curl -s "$HOST/versions" \
  -F "fileId=$TEXT_FILE_ID" \
  -F "contents=@$OLD_CONTENT" | jq -r '.id')
echo "Created old version with id: $OLD_VERSION_ID"

sleep 2s

NEW_VERSION_ID=$(curl -s "$HOST/versions" \
  -F "fileId=$TEXT_FILE_ID" \
  -F "contents=@$NEW_CONTENT" | jq -r '.id')
echo "Created new version with id: $NEW_VERSION_ID"

# Add one xml file:

XML_VERSION_ID=$(curl -s "$HOST/versions" \
  -F "fileId=$XML_FILE_ID" \
  -F "contents=@$XML_CONTENT" | jq -r '.id')
echo "Created xml version with id: $XML_VERSION_ID"

# Add metadata to new text file version:
VERSION_METADATA_KEY='test-version-key'
curl -s -X PUT \
  "localhost:8080/textrepo/rest/versions/$NEW_VERSION_ID/metadata/$VERSION_METADATA_KEY" \
  -d 'test-value' \
  -H 'Content-Type: text/plain' \
  > /dev/null
echo "Created version metadata with key: $VERSION_METADATA_KEY"
