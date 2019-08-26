# Text Repository

Repository to store texts, including their metadata and versions.

Build with:
- java 11
- dropwizard
- docker

## Development

On first run:
```
docker build -t textrepo-builder -f textrepo-app/Dockerfile.builder textrepo-app
```

Build and start:
```
docker-compose up --build
```

If you run into this Elasticsearch warning:
```
max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]
```

you may have to
```
sysctl -w vm.max_map_count=262144
```

(see https://github.com/docker-library/elasticsearch/issues/111)

## Demo

Create document from file:
```
curl -v 'localhost:8080/textrepo/documents' -F file=@{file}
```

Create multiple documents from zip file:
```
curl 'localhost:8080/textrepo/documents' -F 'file=@{file};type=application/zip'
```

Get document by `Location` header or json response:
```
curl 'localhost:8080/textrepo/documents/{uuid}'
curl 'localhost:8080/textrepo/documents/{uuid}/metadata'
curl 'localhost:8080/textrepo/documents/{uuid}/versions'
```

Get file from sha:
```
curl 'localhost:8080/textrepo/files/{sha}'
```

Search for latest document version of in elasticsearch index:
```
curl 'localhost:8080/index/documents/_search?q=content:{term}'
```
