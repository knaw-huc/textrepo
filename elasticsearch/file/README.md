# Full-text indexer

Run: see `../../README.md`

FileIndexer supports all file types.

Get elasticsearch mapping:
```
curl '<host>/full-text/mapping'
```

Convert file into elasticsearch full-text fields:
```
curl '<host>/full-text/fields' -F 'file=@<file>;type=<mimetype>'
```

Search in elasticsearch index by `<term>`:
```
curl -X POST '<index-host>/_search' -H 'Content-Type:application/json' \
  -d '{"suggest":{"keyword-suggest":{"prefix":"<term>", "completion":{"field":"suggest","skip_duplicates":true,"size":5}}},"_source":false}'
```

# Structure of file-index:
Each file-index-document contains the following information of a single textrepo-file:  

- **$.file** containers file fields id and type
- **$.file.type** contains fields id, name and mimetype
- **$.file.metadata** contains metadata as key-value pairs
- **$.doc** contains document fields id, externalId
- **$.doc.metadata** contains metadata as key-value pairs
- **$.versions** contains versions, ordered from newest to oldest
- **$.versions[*].contentsModified**: marks if the version contents differ from the contents of the previous version
