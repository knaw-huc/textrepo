# Development docker-compose setup

To use development setup:
- Move files to root.
- Run `./start.sh` to build images and start containers
- Run `./stop.sh` to remove all containers, volumes, data and networks (using `down -v`) 
- When altering `Dockerfile.builder`, update your local image by running:
```
docker build -f Dockerfile.builder -t knawhuc/textrepo-builder:latest .
```
