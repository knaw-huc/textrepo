#!/usr/bin/env bash
set -e
set -x

HOST=localhost:8080/textrepo

# Add 'text' and 'xml' file types:
curl "$HOST/types" -H 'Content-Type:application/json' -d '{"name": "text", "mimetype": "text/plain"}'
curl "$HOST/types" -H 'Content-Type:application/json' -d '{"name": "xml", "mimetype": "application/xml"}'

## Add text and xml file to same document:
curl -sv "$HOST/documents?type=text" -F "contents=@example.txt;filename=example.file" -F type=text
curl -sv "$HOST/documents?type=xml&byFile=true" -F "contents=@example.xml;filename=example.file" -F type=xml
