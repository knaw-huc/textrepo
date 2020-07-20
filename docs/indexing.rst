.. |tr| replace:: Text Repository

Indexing
========

The |tr| creates and updates ElasticSearch (ES) indexes as defined with ``$TR_INDEXERS`` in ``docker-compose.env``.

Documents are automatically created and updated when adding new versions to the appropriate |tr| files using the REST-API.

Searching in ES indexers is documented `here <https://www.elastic.co/guide/en/elastic-stack/current/index.html>`_.

Indexer
-------

Documents added to an ES index have to conform to the mapping of that ES index. To convert new file versions into a format that matches the ES mapping, the |tr| uses 'indexer' services.
An 'indexer' is a service with two endpoints:

- ``GET mapping`` returns json mapping used by the |tr| to create ES indexes.
- ``POST fields`` endpoint converts the contents of new file version into json document to create or update ES documents

More on ES mappings `here <https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping.html>`_.

Indexing workflow
-----------------

When a file version is added, the |tr| adds or updates all appropriate indexed documents:

- Retrieve file type of new file version
- For each index configured in |tr|: can it handle this file type?

  - No? Skip
  - Yes? Continue

- Convert file version contents to ES document using appropriate indexer ``fields`` endpoint.
- Is this the first version of a file?

  - Yes: create ES document
  - No: update ES document by file ID

- Use response of ``fields``-endpoint to create or update ES documents

Fields endpoint
---------------

The fields-endpoint of an indexer converts a file into a ES document. The |tr| can call this endpoint in two ways: this can be configured with ``indexers[].fields.type``.

A fields ``type`` can configured as ``multipart`` or ``original``:

- When using *multipart*, files will be send to the fields endpoint using ``Content-Type: multipart/form-data`` with a body part named ``file`` which contains the file contents and and a ``Content-Type`` header with the file mimetype.
- When using *original*, files will be send to the fields endpoint with a ``Content-Type`` header and a body containing the file contents.

Indexer configuration
---------------------

A |tr| installation can be configured to any number custom indexes populated by its indexers.
When adding a custom index, make sure its 'indexer' service can be reached by the |tr|.

Indexers can be configured in the ``config.yml`` of |tr| under ``indexers``.
See ``./docker-compose.env`` for some examples, and below an empty template: ::

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

