.. |tr| replace:: Text Repository

Production Setup Example
========================

To keep production data safe, create a production docker-compose file which:

- does not run the concordion tests
- saves database and index data into its own 'production'-volumes


See: ``./example/production/``.

Backing Up Data
---------------

We can backup Postgres by archiving its data directory.

Elasticsearch is a bit more complicated: we cannot simply copy the data from its data dir. It would result in corrupt indices and missing data. Instead we will be using the 'snapshot' api  of ES to create and restore backups.

See also: `backing up elasticsearch, an example <example-backup-es.html>`_.

Example of backing up and restoring production data:

.. code-block:: bash

  cd ./examples/production

  # start production:
  ./start-prod.sh

  # show logging:
  ./log.sh prod

  # add some docs, files and versions:
  (cd ../../ && ./scripts/populate.sh)

  # check added documents exist:
  curl localhost:8080/textrepo/rest/documents
  curl localhost:8080/index/full-text/_search

  # we might want to backup our data?
  mkdir ~/backup
  ./stop-prod.sh
  ./backup-prod.sh
  ls ~/backup/

  # disaster strikes!
  source docker-compose.env
  docker-compose -f docker-compose-prod.yml down -v

  # nothing left!
  ./start-prod.sh && ./log.sh prod
  curl localhost:8080/textrepo/rest/documents
  curl localhost:8080/index/full-text/_search

  # restore volumes:
  docker-compose -f docker-compose-prod.yml down -v
  ./restore-prod.sh

  # check docs have been restored:
  ./start-prod.sh && ./log.sh prod
  curl localhost:8080/textrepo/rest/documents
  curl localhost:8080/index/full-text/_search

See: ``./backup-prod.sh`` and ``./restore-prod.sh`` in ``./examples/production/``.

Running Integration Tests
-------------------------

In production we want to show the integration test results from the concordiondata volume, but we do not want concordion to wipe out our production data while running its tests. This can be achieved by creating separate postgres and elasticsearch volumes for the production and integration environment. However, we only have one concordion data volume, in order to share the concordion test results of the integration setup with the production setup.

Example of running integration tests on production server (i.e. after upgrading) without interfering with production data:

.. code-block:: bash

  cd ./examples/production

  # for starting and populating production, see example above.

  # stop production:
  ./stop-prod.sh

  # run integration tests with different volumes:
  ./start-integration.sh && ./log.sh integration

  # check that no documents exist (because populate.sh was not run):
  curl localhost:8080/textrepo/rest/documents

  # when tests have run, stop integration test setup:
  ./stop-integration.sh

  # start production again...
  ./start-prod.sh && ./log.sh prod

  # check that documents created by ./scripts/populate.sh still exist:
  curl localhost:8080/textrepo/rest/documents
  curl localhost:8080/index/full-text/_search

  # check concordion results from integration setup are available through nginx:
  open http://localhost:8080

See: ``docker-compose-integration.yml`` and ``docker-compose-prod.yml`` in ``./examples/production/``.

When rerunning an example, make sure all previous data is gone:

.. code-block:: bash

  cd ./examples/production

  source docker-compose.env
  docker-compose -f docker-compose-prod.yml down -v
  docker-compose -f docker-compose-integration.yml down -v

