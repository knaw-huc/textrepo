.. |tr| replace:: Text Repository

Indexing
========

The |tr| creates and updates elasticsearch indexes as defined in `config.yml`.

Changing the index should be done by creating adding new versions to the appropriate |tr| files using the REST-API.

Searching
---------

Read all about searching in elasticsearch `here <https://www.elastic.co/guide/en/elastic-stack/current/index.html>`_.

Default index
-------------

By default the text repository contains a full text and a autocomplete index.

Custom index
------------

A |tr| installation can be configured to any number custom indexes populated by its indexers.
When adding a custom index, make sure its 'indexer' service can be reached by the |tr|.

An indexer is a remote service with a mapping-endpoint to request `elasticsearch mappings <https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping.html>`_, and a fields-endpoint to convert a |tr| file into a document that can be indexed.

- `GET mapping` returns json mapping used by the |tr| to create elasticsearch indexes
- `POST fields` endpoint converts the contents of new file version into json document to create or update elastcsearch documents
- Generated documents should match the requirements of the mapping
- Each |tr| file results in a index document

When a file version is added, the |tr| adds or updates all appropriate indexed documents. Workflow:

- |tr| retrieves file type of new file version
- For each index: can it handle this file type?
- No? Skip.
- Yes? `POST` the contents of new version to `fields`-endpoint. `Content-Type` header contains mimetype of file.
- Response of `fields`-endpoint is used by |tr| to create a new or update the corresponding index document

Fields endpoint
-----------

The fields-endpoint of an indexer converts a file into a elasticsearch document. The |tr| can call this endpoint in two ways: this can be configured with `indexers[].fields.type`.

A fields `type` can configured as 'multipart' or 'original':

- When using *multipart*, files will be send to the fields endpoint using `Content-Type: multipart/form-data` with a body part named `file` which contains the file contents and and a `Content-Type` header with the file mimetype.
- When using *original*, files will be send to the fields endpoint with a `Content-Type` header and a body contained the file contents.


Configuration
-------------
Custom indexers can be configured in the `config.yml` of |tr| under `indexers`.
One empty index example: ::

  indexers:
  - mapping:    # string, url of GET mapping endpoint
    fields:
      url:      # string, url of POST fields endpoint
      type:     # string, 'multipart' or 'original'
    mimetypes:
      -         # list of strings, supported mimetypes
    elasticsearch:
      index:    # string, name
      hosts:
        -       # list of strings, host urls

