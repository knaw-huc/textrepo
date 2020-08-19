.. |tr| replace:: Text Repository

Backing Up Elasticsearch
=====================

We cannot simply copy the data from es data dirs, because it could result in corrupt indices and missing data.
Instead we'll be using the 'snapshot' api  of ES to create and restore backups.

See: ``example/production``.

Testing ES Snapshot api
-----------------------

.. code-block:: bash
  # start production setup and wait until finished:
  cd ./examples/production
  source docker-compose.env
  ./start-prod.sh && ./log.sh prod

  ES_URL=$(docker port tr_elasticsearch 9200)

  # check es backup dir is empty:
  docker exec tr_elasticsearch ls -al /backup

  # register and check a snapshot repository
  curl -XPUT $ES_URL/_snapshot/backup -d "@es-repository.json" -H 'content-type:application/json'
  curl $ES_URL/_snapshot/backup

  # add some docs, files and versions:
  (cd ../../ && ./scripts/populate.sh)

  # backup dir is still empty:
  docker exec tr_elasticsearch ls -al /backup

  # check added documents exist:
  curl $ES_URL/full-text/_search | jq

  # create es snapshot:
  curl -XPUT "$ES_URL/_snapshot/backup/snapshot_1?wait_for_completion=true" | jq

  # check backup dir contains data:
  docker exec tr_elasticsearch ls -al /backup

  # disaster strikes!
  # remove es container with its data, but keep its backup volume:
  docker-compose -f docker-compose-prod.yml down

  # start only es:
  docker-compose -f docker-compose-prod.yml up -d elasticsearch &&  ./log.sh prod
  ES_URL=$(docker port tr_elasticsearch 9200)

  # check backup dir contains data:
  docker exec tr_elasticsearch ls -al /backup

  # check index nor documents exist:
  curl $ES_URL/full-text/_search | jq

  # recreate snapshot repository:
  curl -XPUT $ES_URL/_snapshot/backup -d "@es-repository.json" -H 'content-type:application/json'

  # restore snapshot:
  curl -XPOST "$ES_URL/_snapshot/backup/snapshot_1/_restore" -d '{}' -H 'content-type:application/json'

  # check documents exist:
  curl $ES_URL/full-text/_search | jq

  # stop elastic now data is restored:
  docker-compose -f docker-compose-prod.yml stop elasticsearch

  # start fresh production setup:
  ./start-prod.sh &&  ./log.sh prod
  ES_URL=$(docker port tr_elasticsearch 9200)

  # check documents exist:
  curl $ES_URL/full-text/_search | jq

  # when rerunning this test, make sure everything is really really gone:
  docker-compose -f docker-compose-prod.yml down -v

