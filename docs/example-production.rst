.. |tr| replace:: Text Repository

Production Setup Example
========================

To keep production data safe, create a production docker-compose file which:

- does not run the concordion tests
- saves database and index data into its own 'production'-volumes


See: ``./example/production/``.

Backing Up Data
---------------

Postgres is backed up by archiving its data directory.

Elasticsearch is a bit more complicated: We cannot simply copy the data from es data dirs, because it could result in corrupt indices and missing data.
Instead we'll be using the 'snapshot' api  of ES to create and restore backups.

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

  # check docs have bee restored:
  ./start-prod.sh && ./log.sh prod
  curl localhost:8080/textrepo/rest/documents
  curl localhost:8080/index/full-text/_search

Running Integration Tests
-------------------------

In production we want to show the integration test results from the concordiondata volume, but we want to prevent concordion to wipe/change our production data during the tests. This can be achieved by defining separate volumes for postgres and elasticsearch environments (prod vs integration), but using the same concordion volume.

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
