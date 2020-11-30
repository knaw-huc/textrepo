# Autocomplete indexer

Run: see `../README.md`

Get elasticsearch mapping:
```
curl '<host>/autocomplete/mapping'
```

Convert file into elasticsearch autocomplete fields:
```
curl '<host>/autocomplete/fields' -F 'file=@<file>;type=<mimetype>'
```

Search in elasticsearch index by `<term>`:
```
curl -X POST '<index-host>/_search' -H 'Content-Type:application/json' \
  -d '{"suggest":{"keyword-suggest":{"prefix":"<term>", "completion":{"field":"suggest","skip_duplicates":true,"size":5}}},"_source":false}'
```

## Mimetypes and subtypes
The autocomplete indexer knows about a number mimetypes.

Besides those mimetypes the indexer has the concept of "mimetype subtypes", a configurable list of mimetypes that the autocomplete indexer will handle just like their "super" mimetype. E.g. pagexml or 'application/vnd.prima.page+xml' can be configured to be indexed just like ordinary xml or 'application/xml'.

Check `<host>/autocomplete/types` for a list of mimetypes and their subtypes.
