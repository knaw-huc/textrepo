.. |tr| replace:: Text Repository

Production Setup
==================

To keep production data safe, create a production docker-compose file which:

- does not run the concordion tests
- saves database and index data into its own 'production'-volumes

ES data is already stored in a seperate volume, postgres data must be moved to its own volume in this branch, called ``postgresdata`` and ``prostgresdata-prod``.

See: ``./example/production/``.

Testing production setup
------------------------

.. code-block:: bash

  cd ./examples/production

  # start production:
  ./start-prod.sh

  # add some docs, files and versions:
  (cd ../../ && ./scripts/populate.sh)

  # check added documents exist:
  curl localhost:8080/textrepo/rest/documents

  # we might want to backup our data volumes first?
  ./backup-prod.sh

  # disaster strikes!
  docker-compose -f docker-compose-prod.yml down -v

  # restore volumes:
  ./restore-prod.sh

  # stop production setup:
  ./stop-prod.sh

  # run integration tests with different volumes:
  ./start-integration.sh

  # check that no documents exist (because populate.sh was not run):
  curl localhost:8080/textrepo/rest/documents

  # when tests have run, stop integration test setup:
  ./stop-integration.sh

  # start production again...
  ./start-prod.sh

  # check that documents created by ./scripts/populate.sh still exist:
  curl localhost:8080/textrepo/rest/documents
  curl localhost:8080/index/full-text/_search

  # check concordion results from integration setup are available through nginx:
  open http://localhost:8080/concordion/nl/knaw/huc/textrepo/TextRepo.html

