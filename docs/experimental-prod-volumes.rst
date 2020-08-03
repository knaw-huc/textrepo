.. |tr| replace:: Text Repository

Production Volumes
==================

To keep production data safe, create a production docker-compose file which:

- does not run the concordion tests
- saves database and index data into its own 'production'-volumes

ES data is already stored in a seperate volume, postgres data must be moved to its own volume in this branch, called ``postgresdata`` and ``prostgresdata-prod``.

See: ``docker-compose-prod.yml``.

Testing production setup
------------------------

.. code-block:: bash

  # start production:
  ./scripts/start-docker-compose-prod.sh

  # add some docs, files and versions:
  ./scripts/populate.sh

  # check added documents exist:
  curl localhost:8080/textrepo/rest/documents

  # we might want to backup our data first?
  # see: https://docs.docker.com/storage/volumes/#backup-restore-or-migrate-data-volumes

  # stop production setup:
  ./scripts/stop-docker-compose-prod.sh

  # run integration tests with different volumes:
  ./scripts/start-docker-compose.sh

  # check that no documents exist (because ./scripts/populate.sh was not run):
  curl localhost:8080/textrepo/rest/documents

  # stop integration test setup:
  ./scripts/stop-docker-compose.sh

  # start production again...
  ./scripts/start-docker-compose-prod.sh

  # check that documents created by ./scripts/populate.sh still exist:
  curl localhost:8080/textrepo/rest/documents

  # check concordion results from integration setup are available through nginx:
  open http://localhost:8080/concordion/nl/knaw/huc/textrepo/TextRepo.html

  # when rerunning this test, make sure everything is really really gone:
  source docker-compose.env
  docker-compose -f docker-compose.yml down -v
  docker-compose -f docker-compose-dev.yml down -v
  docker-compose -f docker-compose-prod.yml down -v
  docker rm tr_postgres
  docker rmi knawhuc/textrepo-postgres:latest
  docker rmi postgres:11-alpine

