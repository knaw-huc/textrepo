# Full-text indexer

Run: see `../../README.md`

Get supported mimetypes:
```
curl '<host>/full-text/types'
```

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

## Mimetypes and subtypes
The full-text indexer knows about a number mimetypes.

Besides those mimetypes the indexer has the concept of "mimetype subtypes", a configurable list of mimetypes that the full-text indexer will handle just like their "super" mimetype. E.g. pagexml or 'application/vnd.prima.page+xml' can be configured to be indexed just like ordinary xml or 'application/xml'.

Check `<host>/full-text/types` for a list of mimetypes and their subtypes.
