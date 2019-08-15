# Text Repository

Repository to store texts, including their metadata and versions

Build with:
- java 11
- dropwizard
- docker

## Development

First run:
```
cd textrepo-app && docker build -t textrepo-builder -f Dockerfile.builder .
```

Build all and start:
```
docker-compose -p tr up --build -d
```

Test:
```
curl -X POST 'localhost:8080/files' -F file=@{file}
curl -X GET 'localhost:8080/files/{sha224}'
```
