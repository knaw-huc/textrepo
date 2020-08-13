#!/usr/bin/env bash
set -e
set -x

restore_volume() {
  local prefix=${1}
  local volume=${2}
  local backup_dir=${3}
  local backup_archive=${4}
  local container_dir=${5}
  local container=${6}

  if [[ ! -f "$backup_dir/$backup_archive" ]] ; then echo "backup [$backup_dir/$backup_archive] not found, aborting."; exit; fi

  docker volume create $volume
  docker run --rm -v $volume:/recover -v $backup_dir:/backup alpine /bin/sh -c "cd /recover && tar xvf /backup/$backup_archive"
  docker-compose up --no-build -d $container
}

PREFIX=$(basename $(pwd))
VOLUME=${PREFIX}_esdata
BACKUP_DIR=~/workspace/test/backup
BACKUP_ARCHIVE=esdata-volume.tar
CONTAINER_DIR=/usr/share/elasticsearch/data
CONTAINER=elasticsearch

restore_volume $PREFIX $VOLUME $BACKUP_DIR $BACKUP_ARCHIVE $CONTAINER_DIR $CONTAINER
