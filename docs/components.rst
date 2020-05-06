.. |tr| replace:: Text Repository

Components
==========

Endpoints
*********

- |tr| `API <http://localhost:8080/textrepo/swagger>`_
- |tr| `healtch checks <http://localhost:8081/healthcheck/>`_
- `Integration test results <https://demorepo.tt.di.huc.knaw.nl/concordion/nl/knaw/huc/textrepo/Textrepo.html>`_
- `Elasticsearch indexes <http://localhost:8080/index/_aliases?pretty>`_
- `Stub indexer <http://localhost:8080/custom-index/>`_

Services
********

The |tr| application is bundled with a couple of services:

- |tr|, core application
- `Postgres <https://www.postgresql.org/>`_ as database
- `Elasticsearch <https://www.elastic.co/elasticsearch/>`_ as search engine
- `Docker <https://www.docker.com/>`_ and `docker-compose <https://docs.docker.com/compose/>`_ for running and connecting services
- `Nginx <https://www.nginx.com/>`_ as a reverse proxy

|tr|
____
The |tr| is a java application build with `dropwizard <https://www.dropwizard.io/en/latest/>`_.
It contains a REST-API to create, retrieve, update and delete documents, files, versions and metadata.
It uses the postgres database to store its domain model.
It keeps the elastic search indexes in sync with the postgres database.

Postgres
--------
Postgres is used as a 'vault' which contains all the data or 'the golden standard', from which all indexes are generated.

Postgres data can be modified using the |tr| REST-API.

Elasticsearch
-------------
See the `indexing <indexing.html>`_ page.

Nginx
-----
As a reverse proxy it exposes the relevant parts of all the service endpoints. See exposed endpoints below.
