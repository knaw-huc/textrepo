# Spacy NER Indexer

Text repository indexer that recognizes named entities using spacy, flask and docker.

### Adding service to Text Repository

- Add your spacy model to `./elasticsearch/spacy-ner/model`.

- Add indexer service to `./docker-compose.yml`:
```
  spacy-ner-indexer:
    container_name: tr_spacy-ner
    image: knawhuc/textrepo-spacy-ner-indexer:${DOCKER_TAG}
    ports:
      - 8080
    networks:
      - textrepo_network
    volumes:
      - ./scripts/wait-for-it.sh:/tagger/wait-for-it.sh
    command: [
        "./wait-for-it.sh", "elasticsearch:9200", "--timeout=0", "--",
        "python", "/tagger/py/main.py"
    ]
    volumes:
      - ./elasticsearch/spacy-ner/model:/tagger/model

```

- Add to `TR_INDEXERS` in `./docker-compose.env`:
```
- name: spacy-ner
  mapping: http://spacy-ner-indexer:8080/spacy-ner/mapping
  fields:
    url: http://spacy-ner-indexer:8080/spacy-ner/fields
    type: multipart
  mimetypes:
    - text/plain
  elasticsearch:
    index: spacy-ner
    hosts:
      - elasticsearch:9200
```

- Add some text versions to the text repository suitable for your model.

- Query elasticsearch index:
```shell script
curl -X GET localhost:8080/index/spacy-ner/_search \
  -H 'Content-Type: application/json' \
  -d @test-query.json
```
