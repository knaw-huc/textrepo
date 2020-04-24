.. |tr| replace:: Text Repository

Indexing
========

The |tr| contains a number of default elasticsearch indexes which are populated by the |tr| itself.
Users can search documents in the index, but should create, update or delete documents.
All modifications of the index should be handled by the |tr| itself.
When you want to edit a index document, edit the appropriate file instead using the |tr| REST-API.

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

When a file version is added, the |tr| adds or updates all appropriate indexed documents:

- |tr| retrieves file type of new file version
- For each index: can it handle this file type?
- No? Skip
- Yes? `POST` contents of new vrsion to `fields`-endpoint. `Content-Type` header contains mimetype of file.
- Response of `fields`-endpoint is used by |tr| to create or update index document

Config.yml
----------
Custom indexers can be configured in the `config.yml` of |tr| under `indexers`.
One empty index example: ::

  indexers:
  - mapping:    # string, url of GET mapping endpoint
    fields:
      url:      # string, url of POST fields endpoint
      type:     # string, 'multipart' or 'urlencoded'
    mimetypes:
      -         # list of strings, supported mimetypes
    elasticsearch:
      index:    # string, name
      hosts:
        -       # list of strings, host urls

