.. |tr| replace:: Text Repository

Backing Up Elasticsearch Example
================================

We cannot simply copy the data from es data dirs, because it could result in corrupt indices and missing data.
Instead we'll be using the 'snapshot' api  of ES to create and restore backups (i.e. snapshots).

More on: `elasticsearch snapshot api <https://www.elastic.co/guide/en/elasticsearch/reference/7.6/snapshot-restore.html>`_.

Testing ES Snapshot
-----------------------

See also the `production example <example-production.html>`_, ``backup-prod.sh`` and ``restore-prod.sh``.

.. code-block:: bash

  ES_URL=$(docker port tr_elasticsearch 9200)

  # register a snapshot repository
  curl -XPUT $ES_URL/_snapshot/backup \
    -H 'content-type:application/json' \
    -d '{"type":"fs","settings":{"location":"/backup"}}'

  # create a snapshot:
  curl -XPUT "$ES_URL/_snapshot/backup/snapshot_1?wait_for_completion=true" | jq

  # when starting with fresh es install, first restore snapshot repository:
  curl -XPUT $ES_URL/_snapshot/backup \
    -d '{"type":"fs","settings":{"location":"/backup"}}' \
    -H 'content-type:application/json'

  # and restore snapshot:
  curl -XPOST "$ES_URL/_snapshot/backup/snapshot_1/_restore" \
    -H 'content-type:application/json' \
    -d '{}'

