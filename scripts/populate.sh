#!/usr/bin/env bash
set -e

HOST=localhost:8080/textrepo
TXT=example.txt
XML=example.xml

# Check example files exist:
if [[ ! -f $TXT ]] ; then echo "File '$TXT' not found, aborting."; exit; fi
if [[ ! -f $XML ]] ; then echo "File '$XML' not found, aborting."; exit; fi

# Add 'text' and 'xml' file types:
curl "$HOST/types" -H 'Content-Type:application/json' -d '{"name": "text", "mimetype": "text/plain"}'
curl "$HOST/types" -H 'Content-Type:application/json' -d '{"name": "xml", "mimetype": "application/xml"}'

# Add text and xml file to same document:
curl -sv "$HOST/documents" \
  -F "contents=@$TXT;filename=example.file" \
  -F type=text \
  -F externalId=exampleFile
curl -sv "$HOST/documents" \
  -F "contents=@$XML;filename=example.file" \
  -F type=xml \
  -F externalId=exampleFile
