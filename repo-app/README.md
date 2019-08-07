# Text Repository
Build with:
- java 11
- dropwizard
- docker

## Development
Run:
```
docker build -t textrepobuilder:latest -f Docker.builderfile .
docker-compose build textrepo-app
docker-compose -p tr up
```
