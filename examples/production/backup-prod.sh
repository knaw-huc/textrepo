#!/usr/bin/env bash
set -e
set -x

source docker-compose.env

backup_volume() {

  # location of archive on host machine:
  local backup_dir=${1}

  # location of mounted volume in container:
  local container_dir=${2}

  # filename of new archive:
  local archive_name=${3}

  # must not run:
  local container=${4}


  if [[ ! -d $backup_dir ]] ; then echo "backup dir [$backup_dir] not found, aborting."; exit; fi
  if [[ "$(docker ps -q -f name=$container)" ]] ; then echo "$container is running, aborting."; exit; fi

  # create archive in backup directory:
  docker run --rm --volumes-from $container -v $backup_dir:/backup alpine /bin/sh -c "cd $container_dir && tar czvf /backup/$archive_name ."
  ls -alh $backup_dir
}

create_es_snapshot() {

  # location of mounted volume in container:
  local container_dir=${1}

  # must not run:
  local container=${2}

  # container as named in docker-compose:
  local service=${3}

  if [[ "$(docker ps -q -f name=$container)" ]] ; then echo "$container is running, aborting."; exit; fi

  # start es container:
  docker-compose -f docker-compose-prod.yml up -d $service
  read -p "Press enter when elasticsearch status is GREEN or YELLOW"

  local es_url=$(docker port $container 9200)

  # empty repository directory:
  curl -XDELETE $es_url/_snapshot/backup/snapshot_1
  curl -XDELETE $es_url/_snapshot/backup
  docker exec -ti $container rm -rf $container_dir/*
  docker exec -ti $container ls -al $container_dir

  # create snapshot repository:
  curl -XPUT $es_url/_snapshot/backup \
    -d "{\"type\":\"fs\",\"settings\":{\"location\":\"$container_dir\"}}" \
    -H 'content-type:application/json'

  # create snapshot:
  curl -XPUT "$es_url/_snapshot/backup/snapshot_1?wait_for_completion=true"

  # stop elasticsearch with new snapshot:
  docker-compose -f docker-compose-prod.yml stop $service
}

# Backing up postgres:

BACKUP_DIR=~/backup
CONTAINER_DIR=/var/lib/textrepo/data
ARCHIVE_NAME=postgresdata-prod-volume.tar
CONTAINER=tr_postgres
backup_volume $BACKUP_DIR $CONTAINER_DIR $ARCHIVE_NAME $CONTAINER


# Backing up elasticsearch:

BACKUP_DIR=~/backup
CONTAINER_DIR=/snapshot-repo
ARCHIVE_NAME=esdata-prod-volume.tar
CONTAINER=tr_elasticsearch
SERVICE=elasticsearch

create_es_snapshot $CONTAINER_DIR $CONTAINER $SERVICE
backup_volume $BACKUP_DIR $CONTAINER_DIR $ARCHIVE_NAME $CONTAINER

