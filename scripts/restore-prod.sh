#!/usr/bin/env bash
set -e
set -x

restore_volume() {
  local prefix=${1}
  local volume=${2}
  local backup_dir=${3}
  local backup_archive=${4}
  local container_dir=${5}

  if [[ ! -f "$backup_dir/$backup_archive" ]] ; then echo "backup [$backup_dir/$backup_archive] not found, aborting."; exit; fi

  # create named but still empty volume:
  docker volume create $volume

  # create dummy alpine container with volume and extract archive in it:
  docker run --rm -v $volume:/recover -v $backup_dir:/backup alpine /bin/sh -c "cd /recover && tar xvf /backup/$backup_archive"
}

PREFIX=$(basename $(pwd))

VOLUME=${PREFIX}_esdata-prod
BACKUP_DIR=~/workspace/test/backup
BACKUP_ARCHIVE=esdata-prod-volume.tar
CONTAINER_DIR=/usr/share/elasticsearch/data
restore_volume $PREFIX $VOLUME $BACKUP_DIR $BACKUP_ARCHIVE $CONTAINER_DIR $CONTAINER

VOLUME=${PREFIX}_postgresdata-prod
BACKUP_DIR=~/workspace/test/backup
BACKUP_ARCHIVE=postgresdata-prod-volume.tar
CONTAINER_DIR=/var/lib/textrepo/data
restore_volume $PREFIX $VOLUME $BACKUP_DIR $BACKUP_ARCHIVE $CONTAINER_DIR $CONTAINER
