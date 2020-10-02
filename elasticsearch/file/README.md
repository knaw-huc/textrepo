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
