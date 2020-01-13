# Autocomplete indexer

Run: see `../README.md`

Get elasticsearch mapping:
```
url '{host}/autocomplete/mapping'
```

Convert file into es autocomplete fields:
```
url '{host}/autocomplete/fields' -F 'file=@{file};type={mimetype}'
```
