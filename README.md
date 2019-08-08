# Text Repository
Build with:
- java 11
- dropwizard
- docker

## Development
Run:
```
(cd textrepo-app && docker build -t textrepo-builder:latest -f Dockerfile.builder .)
docker-compose build textrepo-app
docker-compose -p tr up
```

