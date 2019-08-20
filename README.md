# Text Repository

Repository to store texts, including their metadata and versions.

Build with:
- java 11
- dropwizard
- docker

## Development

On first run:
```
cd textrepo-app && docker build -t textrepo-builder -f Dockerfile.builder .
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

Test:
```
curl -X POST 'localhost:8080/textrepo/files' -F file=@{file}
curl -X GET 'localhost:8080/textrepo/files/{sha224}'
curl -X GET 'localhost:8080/index/files/_search?q=content:{term}'
```
