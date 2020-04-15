# Text Repository

Repository to store texts, including their metadata and versions.

## Development

On first run:
```
docker build -t textrepo-builder -f Dockerfile.builder .
```

Build, start and stop:
```
./scripts/start.sh
./scripts/stop.sh
```

### Elasticsearch
If you run into this Elasticsearch warning:
```
max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]
```

you may have to
```
sysctl -w vm.max_map_count=262144
```

(see https://github.com/docker-library/elasticsearch/issues/111)

### Kubernetes
Run:
```
kubectl apply\
 -f kubernetes/01-postgres.yml\
 -f kubernetes/02-elasticsearch.yml\
 -f kubernetes/03-textrepo-app.yaml\
 -f kubernetes/04-nginx.yml\
 -f kubernetes/05-concordion.yml
```

## Documentation

### Local 

Text Repository REST-endpoint are documented with [Swagger](http://localhost:8080/textrepo/swagger).

Custom indexers, REST-endpoints and healtch checks are documented with [Concordion integration tests](http://localhost:8080/concordion/nl/knaw/huc/textrepo/Textrepo.html).

### Online
See: [readthedocs](http://textrepo.readthedocs.io/en/latest/) (WIP)

