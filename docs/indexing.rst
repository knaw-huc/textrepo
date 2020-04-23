.. |tr| replace:: Text Repository

Indexing
========

The |tr| contains a number of default elasticsearch indexes which are populated by the |tr| itself. Users can read documents from the index, but cannot create, update or delete documents. All modifications of the index should be handled by the |tr| itself. When you want to edit a index document, edit the appropriate file instead using the |tr| REST-API.

Searching and updating
----------------------

ElasticSearch has extensively documentated its own API. Read all about it `here <https://www.elastic.co/guide/en/elastic-stack/current/index.html>`_.

Default index
-------------

By default the text repository contains a full text and a autocomplete index.

Custom index
------------

A |tr| installation can be configured to use any number of custom indexers to populate custom indexes. To add a custom index, make sure its 'indexer' is available.
An indexer is a remote service with a 'mapping' endpoint to request elasticsearch fields, and a 'fields' endpoint to convert a |tr| file into a elasticsearch index document.

- `GET Mapping` endpoints are used to create elasticsearch indexes.
- `POST Fields` endpoints are used to create elastcsearch index documents.
- Generated documents should match the mapping that the indexer mapping endpoint returns.

When a user adds a new (or initial) version to a file, the |tr| calls fields-endpoints of all indexers of indexes that support that specific file type.
A |tr| `POST fields` request contains the contents of new version and a `Content-Type` header with the mimetype.
|tr| uses the response of a custom indexer to create a new document in its index.

Custom indexer API
------------------
Custom indexers can be configured in `./textrepo-app/config.yml`: ::

  indexers:
  - mapping:    # string, url to GET mapping endpoint
    fields:
      url:      # string, url to POST fields endpoint
      type:     # string, 'multipart' or 'urlencoded'
    mimetypes:
      -         # list of strings, supported mimetypes
    elasticsearch:
      index:    # string, name
      hosts:
        -       # list of strings, host urls

