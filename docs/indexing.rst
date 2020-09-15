.. |tr| replace:: Text Repository

Indexing
========

The |tr| creates and updates ElasticSearch (ES) indexes as defined in ``docker-compose.env``.

Elasticsearch documents are automatically created and updated when new file versions are added to the |TR|.

More on: `searching in ES <https://www.elastic.co/guide/en/elastic-stack/current/index.html>`_.

Indexer
-------

To convert new file versions into a format that ES understands, the |tr| uses 'indexer' services.
An 'indexer' is a service with two endpoints:

- ``GET mapping`` returns json mapping used by the |tr| to create an ES index.
- ``POST fields`` endpoint converts the contents of new file version into a json document that matches the json mapping

More on: `ES mappings <https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping.html>`_.

Indexing workflow
-----------------

When a new file version is added, the |tr| has to determine which ES document in which index needs to be created or updated. The current workflow consists of the following steps:

- Retrieve file type of new file version
- For each indexer: does it handle this file mimetype?

  - No? Skip
  - Yes? Continue

- Convert file version contents to ES document using appropriate indexer ``fields`` endpoint.
- Is this file version the first version?

  - Yes: create new ES document
  - No: update ES document by file ID

- Use response of ``fields``-endpoint to create or update ES documents

Fields endpoint
---------------

The fields-endpoint of an indexer converts a file into an ES document. The |tr| can call this endpoint in two ways: as ``multipart`` or ``original``.

- When using **multipart**, files will be send to the fields endpoint using ``Content-Type: multipart/form-data`` with a body part named ``file`` which contains the file contents and and a ``Content-Type`` header with the file mimetype.
- When using **original**, files will be send to the fields endpoint with a ``Content-Type`` header and a body containing the file contents.

Indexer configuration
---------------------

Indexers and their ES indexes can be configured with ``$TR_INDEXERS`` in  ``docker-compose.env``. An (empty) indexer configuration consists of the following elements: ::

  indexers:
  - name:       # string, name of indexer
    mapping:    # string, url of GET mapping endpoint
    fields:
      url:      # string, url of POST fields endpoint
      type:     # string, 'multipart' or 'original'
    mimetypes:
      -         # list of strings, supported mimetypes
    elasticsearch:
      index:    # string, name of index
      hosts:
        -       # list of strings, host urls

