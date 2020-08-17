#!/usr/bin/env bash
set -e
set -x

backup_volume() {
  local backup_dir=${1}
  local container_dir=${2}
  local container=${3}
  local archive_name=${4}

  if [[ ! -d $backup_dir ]] ; then echo "backup dir [$backup_dir] not found, aborting."; exit; fi

  # stop containers using volume:
  docker stop $container

  # create tar in backup directory:
  docker run --rm --volumes-from $container -v $backup_dir:/backup alpine /bin/sh -c "cd $container_dir && tar cvf /backup/$archive_name ."
}

BACKUP_DIR=~/workspace/test/backup

CONTAINER_DIR=/usr/share/elasticsearch/data
CONTAINER=tr_elasticsearch
ARCHIVE_NAME=esdata-prod-volume.tar
backup_volume $BACKUP_DIR $CONTAINER_DIR $CONTAINER $ARCHIVE_NAME

CONTAINER_DIR=/var/lib/textrepo/data
CONTAINER=tr_postgres
ARCHIVE_NAME=postgresdata-prod-volume.tar
backup_volume $BACKUP_DIR $CONTAINER_DIR $CONTAINER $ARCHIVE_NAME
