#!/usr/bin/env bash
set -e
set -x

source docker-compose.env

restore_volume() {

  # name of volume:
  local volume=${1}

  # location of archive on host machine:
  local backup_dir=${2}

  # filename of archive:
  local archive_name=${3}

  # must not run:
  local container=${4}

  if [[ ! -f "$backup_dir/$archive_name" ]] ; then echo "backup [$backup_dir/$archive_name] not found, aborting."; exit; fi
  if [[ "$(docker ps -q -f name=$container)" ]] ; then echo "$container is running, aborting."; exit; fi

  # create named but still empty volume:
  docker volume create $volume

  # create dummy alpine container with volume, and unpack archive in it (using gnu instead of busybox tar):
  docker run --rm \
    -v $volume:/recover \
    -v $backup_dir:/backup \
    alpine \
    /bin/sh -c "apk add --no-cache tar && cd /recover && tar xvf /backup/$archive_name"
}

restore_es_snapshot() {

  # must not run:
  local container=${1}

  # directory containing snapshot repository:
  local container_dir=${2}

  # container as named in docker-compose:
  local service=${3}

  if [[ "$(docker ps -q -f name=$container)" ]] ; then echo "$container is running, aborting."; exit; fi


  # start only es:
  docker-compose -f docker-compose-prod.yml up -d $service

  read -p "Press enter when elasticsearch has started (check with: docker logs $container -f)"

  local es_url=$(docker port $container 9200)

  # restore snapshot repository:
  curl -XPUT $es_url/_snapshot/backup \
    -d "{\"type\":\"fs\",\"settings\":{\"location\":\"$container_dir\"}}" \
    -H 'content-type:application/json'

  # restore snapshot:
  curl -XPOST "$es_url/_snapshot/backup/snapshot_1/_restore?wait_for_completion=true" \
    -H 'content-type:application/json' \
    -d '{}'

  # stop elasticsearch with restored backup/snapshot_1:
  docker-compose -f docker-compose-prod.yml stop $service
}

# Restoring postgres:

BACKUP_DIR=~/backup
PREFIX=$(basename $(pwd))
VOLUME=${PREFIX}_postgresdata-prod
ARCHIVE_NAME=postgresdata-prod-volume.tar.gz
CONTAINER=tr_postgres

restore_volume $VOLUME $BACKUP_DIR $ARCHIVE_NAME $CONTAINER


# Restoring elasticsearch:

BACKUP_DIR=~/backup
PREFIX=$(basename $(pwd))
VOLUME=${PREFIX}_essnapshotdata-prod
ARCHIVE_NAME=esdata-prod-volume.tar.gz
CONTAINER=tr_elasticsearch
CONTAINER_DIR=/snapshot-repo
SERVICE=elasticsearch

restore_volume $VOLUME $BACKUP_DIR $ARCHIVE_NAME $CONTAINER
restore_es_snapshot $CONTAINER $CONTAINER_DIR $SERVICE
