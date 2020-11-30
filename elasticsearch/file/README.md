# File indexer

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

# Structure of file index:
Each file-index-document contains the following information of a single textrepo-file:  

- **$.file**: object with id and:
- **$.file.type**: object with id, name mimetype
- **$.file.metadata[]**: array of nested objects, contains metadata as key-value pairs
- **$.doc**: object with id, externalId and:
- **$.doc.metadata[]**: array of nested objects, contains metadata as key-value pairs
- **$.contentsLastModified** object with sha of latest contents, id of first version with latest sha, and its date-time of creation
- **$.versions[]**: array of nested objects, ordered from newest to oldest, with id, sha, createdAt, and:
- **$.versions[*].contentsModified**: marks if the version contents differ from the contents of the previous version
