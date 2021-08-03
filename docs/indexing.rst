.. |tr| replace:: Text Repository

Indexing
========

The |tr| creates and updates ElasticSearch (ES) indices as defined in ``docker-compose.env``.

Elasticsearch documents are automatically created and updated when new file versions are added to the |TR|.

More on: `searching in ES <https://www.elastic.co/guide/en/elastic-stack/current/index.html>`_.

Indexer
-------

To convert new file versions into a format that ES understands, the |tr| uses 'indexer' services.
An indexer is a service with three endpoints:

- ``GET mapping`` returns json mapping used by the |tr| to create an ES index.
- ``POST fields`` converts the contents of new file version into a json document that matches the json mapping
- ``GET types`` returns mimetypes that the indexer accepts, including the 'subtypes' of a mimetype, which will handled in the same way as their parent mimetype.

  - For example ``application/xml`` could have as a subtype ``application/vnd.prima.page+xml``, meaning that the indexer handles page xml just like ordinary xml.
  - An indexer that returns no types (status ``204 No Content``) is assumed to handle all file types

More on: `ES mappings <https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping.html>`_.

Indexing moments
----------------

When are changes made to the ES indices?

- ``POST /rest/files``      -> Creating a new file resource will create ES docs with empty body
- ``PUT  /rest/files``      -> Updating a file resource will update ES docs with latest version contents or an empty body when no latest version contents available
- ``DELETE /rest/files``    -> Deleting a file will delete the corresponding ES docs
- ``POST /rest/versions``   -> Updating a version resource will update ES docs with latest version contents
- ``DELETE /rest/versions`` -> Deleting the latest version of a file will update ES docs with the pre-latest version contents

Which tasks change indices?

- ``POST /task/index``      -> Multiple endpoints for reindexing a subset of files
- ``POST /task/import/documents/{externalId}/{typeName}`` -> index the imported file (optional, default)
- ``DELETE /task/delete/documents/{externalId}`` -> delete files of document (optional, default)
- ``DELETE /task/index/deleted-files`` -> new, delete all ES-docs of deletes files, meaning: delete all ES doc IDs not present in |tr| database

Note: when calling any other endpoints (e.g. mutating metadata or external IDs), reindexing should done by calling one of the indexing tasks.

Indexing workflow
-----------------

When a new file or file version is added, the |tr| has to determine which ES document in which index needs to be created or updated. The current workflow consists of the following steps:

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

Indexers and their ES indices can be configured with ``$TR_INDEXERS`` in  ``docker-compose.env``. An (empty) indexer configuration consists of the following elements: ::

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


Default indexers
----------------

The textrepo contains a number of default indexers:

- `Full-text indexer <https://github.com/knaw-huc/textrepo/tree/master/elasticsearch/full-text>`_: for basic full-text search queries
- `Autocomplete indexer <https://github.com/knaw-huc/textrepo/tree/master/elasticsearch/autocomplete>`_: for autocomplete suggestions
- `File indexer <https://github.com/knaw-huc/textrepo/tree/master/elasticsearch/file>`_: for searching the metadata of documents, files and versions
