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
