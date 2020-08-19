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

# start es container:
docker-compose -f docker-compose-prod.yml up -d elasticsearch
read -p "Press enter when elasticsearch has started (check with: ./log.sh prod)"

ES_URL=$(docker port $CONTAINER 9200)

# empty repository directory:
docker exec -ti tr_elasticsearch rm -rf $CONTAINER_DIR/*
docker exec -ti tr_elasticsearch ls -al $CONTAINER_DIR

# create snapshot repository:
curl -XPUT $ES_URL/_snapshot/backup \
  -d "{\"type\":\"fs\",\"settings\":{\"location\":\"$CONTAINER_DIR\"}}" \
  -H 'content-type:application/json'

# create snapshot:
curl -XPUT "$ES_URL/_snapshot/backup/snapshot_1?wait_for_completion=true" | jq

# stop elasticsearch with new snapshot:
docker-compose -f docker-compose-prod.yml stop elasticsearch

# create archive of snapshot:
backup_volume $BACKUP_DIR $CONTAINER_DIR $ARCHIVE_NAME $CONTAINER

