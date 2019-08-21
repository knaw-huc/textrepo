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
```
# Create document from file:
curl -v -X POST 'localhost:8080/textrepo/documents' -F file=@{file}

# Get version from `Location` header:
curl -X GET 'localhost:8080/textrepo/document/{uuid}'

# Get file from sha:
curl -X GET 'localhost:8080/textrepo/files/{fileSha}'

# Search for latest document version of in elasticsearch index:
curl -X GET 'localhost:8080/index/documents/_search?q=content:{term}'
```
