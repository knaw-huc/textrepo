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

Test:
```
curl -X POST 'localhost:8080/textrepo/files' -F file=@{file}
curl -X GET 'localhost:8080/textrepo/files/{sha224}'
curl -X GET 'localhost:8080/index/files/_search?q=content:{term}'
```
