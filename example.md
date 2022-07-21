# TextRepo 101 (`Hello World`)
In this example, we will import a plain text file to the TextRepo, verify its contents in the repo as well as verify that it has been indexed in at least the `full-text` index of Elasticsearch.

* All TextRepo documents have an `externalId`. In this example, we use a fixed `externalId` of `doc_1`.
* We will register a new type `txt` to represent `text/plain`
* We will upload a plain text document of type `txt`, so it will be (automatically) indexed in the `full-text` index.

## Setup env var (QOL)

```bash
export tr=http://localhost:8080/textrepo
```

## Verify no types are registered yet

```bash
curl -s $tr/rest/types | jq .
```

```json
[]
```

## Register `text/plain` type named `txt`

```bash
curl -s -XPOST $tr/rest/types \
	-H 'Content-Type: application/json' \
	-d '{"name": "txt", "mimetype": "text/plain"}' \
| jq .
```

```json
{
	"id": 254,
	"name": "txt",
	"mimetype": "text/plain"
}
```
Note that as `.id` is internally assigned, it may be different in your case.

## Import a document

### Prepare file `hw.txt`
```bash
echo "Hello, world!" > hw.txt
```
```bash
cat hw.txt
```
```txt
Hello, world!
```
### Send it
* Use endpoint `task/import/documents/...`
* We import the file to `{externalId}/{fileType}`, in this case `doc_1/txt`
* Because document `doc_1` does not yet exist, we tell TextRepo it is OK to create a new document during this import, using the query parameter `allowNewDocument=true`.

```bash
curl -s -XPOST $tr/task/import/documents/doc_1/txt?allowNewDocument=true \
	-H "accept: application/json" \
	-F "contents=@hw.txt" \
| jq .
```

```json
{
	"indexed": true,
	"newVersion": true,
	"versionId": "0f7a74b2-3b1f-4462-918e-2d7d8b1a53b9",
	"contentsSha": "f12b74807e1e998def3a2080b5c237a1912c9be5df8ee80ea8ad028d",
	"fileId": "a46dd361-bdf3-4cf6-a050-7434524a75b2",
	"typeId": 254,
	"documentId": "e3f41426-2cd2-4e19-af1d-da027eb4ea9c"
}
```
* `indexed` tells us that the file was sent to Elasticsearch for indexing
* `newVersion` tells us that this is a new version of the `txt` file for document `doc_1`
* note that `versionId`, `fileId` and `documentId` are internally generated UUIDs which will be different in your case.
* however, as `contentsSha` is a hash based on the contents of `hw.txt`, it *should* be the same.
* `typeId` *may* be different, but *should* still match the `id` from when type `txt` was registered above.

## Check that `doc_1` has expected contents for type `txt`

```bash
curl -s $tr/task/find/doc_1/file/contents?type=txt
```
```txt
Hello, world
```

## Check that `doc_1` is indexed in `full-text` indexer

### 1. Via `http://localhost:8080/index`
The TextRepo stack has a perimeter `nginx` proxy which passes `/index` to the ElasticSearch indexes. So to pull everything from the `full-text` index, we can:

```bash
curl -s http://localhost:8080/index/full-text/_search \
	-H 'Content-type: application/json' \
	-d '{"query": {"match_all": {}}}' \
| jq .hits.hits
```

Notice how we directly visit `http://localhost:8080` and don't use our shortcut ENV var `$tr` here, as `$tr` points to `http://localhost:8080/textrepo` and is thus proxied to `textrepo` instead of `elasticsearch` which we need here.

```json
[
  {
	"_index": "full-text",
	"_type": "_doc",
	"_id": "a46dd361-bdf3-4cf6-a050-7434524a75b2",
	"_score": 1,
		"_source": {
			"contents": "Hello, world"
		}
	}
]
```
Note that `_id` is equal to the `fileId` we saw [earlier](#import-a-document)

### 2. From inside the `elasticsearch` container
Alternatively, and assuming `elasticsearch:9200` is not exposed via `docker-compose.yml`, we may want to run a query inside the `elasticsearch` container and pull out everything from the `full-text` index:

```bash
docker-compose -f docker-compose-dev.yml exec elasticsearch \
	curl -s localhost:9200/full-text/_search \
		-H 'Content-type: application/json' \
		-d '{"query": {"match_all": {}}}' \
| jq .hits.hits
```

```json
[
  {
	"_index": "full-text",
	"_type": "_doc",
	"_id": "a46dd361-bdf3-4cf6-a050-7434524a75b2",
	"_score": 1,
		"_source": {
			"contents": "Hello, world"
		}
	}
]
```

If, instead, you get a host of errors like:
```sh
WARN[0000] The "POSTGRES_PASSWORD" variable is not set. Defaulting to a blank string.
WARN[0000] The "POSTGRES_DB" variable is not set. Defaulting to a blank string.
WARN[0000] The "POSTGRES_USER" variable is not set. Defaulting to a blank string.
WARN[0000] The "POSTGRES_PORT" variable is not set. Defaulting to a blank string.
WARN[0000] The "DOCKER_TAG" variable is not set. Defaulting to a blank string.
WARN[0000] The "FULL_TEXT_XML_SUBTYPES" variable is not set. Defaulting to a blank string.
WARN[0000] The "FULL_TEXT_TXT_SUBTYPES" variable is not set. Defaulting to a blank string.
[...]
```

you will probably need to do one of the following, depending on how recent your `docker-compose` is:
1. either `source docker-compose.env`  so that your current shell has values for all the missing ENV vars
1. or use a `docker-compose` (or `docker compose`) version which supports `--env-file` and then pass `--env-file docker-compose.env`