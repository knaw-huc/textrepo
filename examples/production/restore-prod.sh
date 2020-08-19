#!/usr/bin/env bash
set -e
set -x

restore_volume() {

  # prefix of volume (=parent dir):
  local prefix=${1}

  # name of volume:
  local volume=${2}

  # location of archive on host machine:
  local backup_dir=${3}

  # filename of archive:
  local archive_name=${4}

  # must not run:
  local container=${5}

  if [[ ! -f "$backup_dir/$archive_name" ]] ; then echo "backup [$backup_dir/$archive_name] not found, aborting."; exit; fi
  if [[ "$(docker ps -q -f name=$container)" ]] ; then echo "$container is running, aborting."; exit; fi

  # create named but still empty volume:
  docker volume create $volume

  # create dummy alpine container with volume, and unpack archive in it:
  docker run --rm -v $volume:/recover -v $backup_dir:/backup alpine /bin/sh -c "cd /recover && tar xvf /backup/$archive_name"
}


# Restoring postgres:

PREFIX=$(basename $(pwd))
BACKUP_DIR=~/backup
VOLUME=${PREFIX}_postgresdata-prod
ARCHIVE_NAME=postgresdata-prod-volume.tar
CONTAINER=tr_postgres
restore_volume $PREFIX $VOLUME $BACKUP_DIR $ARCHIVE_NAME $CONTAINER


# Restoring elasticsearch:

CONTAINER_DIR=/snapshot-repo
PREFIX=$(basename $(pwd))
BACKUP_DIR=~/backup
VOLUME=${PREFIX}_essnapshotdata-prod
ARCHIVE_NAME=esdata-prod-volume.tar
CONTAINER=tr_elasticsearch

restore_volume $PREFIX $VOLUME $BACKUP_DIR $ARCHIVE_NAME $CONTAINER

# start only es:
docker-compose -f docker-compose-prod.yml up -d elasticsearch

read -p "Press enter when elasticsearch has started (check with: ./log.sh prod)"

ES_URL=$(docker port $CONTAINER 9200)

# restore snapshot repository:
curl -XPUT $ES_URL/_snapshot/backup \
  -d "{\"type\":\"fs\",\"settings\":{\"location\":\"$CONTAINER_DIR\"}}" \
  -H 'content-type:application/json'

# restore snapshot:
curl -XPOST "$ES_URL/_snapshot/backup/snapshot_1/_restore" \
  -H 'content-type:application/json' \
  -d '{}'

# stop elasticsearch with restored backup/snapshot_1:
docker-compose -f docker-compose-prod.yml stop elasticsearch

