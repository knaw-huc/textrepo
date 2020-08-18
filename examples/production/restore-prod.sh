#!/usr/bin/env bash
set -e

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

PREFIX=$(basename $(pwd))
VOLUME=${PREFIX}_postgresdata-prod
BACKUP_DIR=~/backup
ARCHIVE_NAME=postgresdata-prod-volume.tar
CONTAINER=tr_postgres
restore_volume $PREFIX $VOLUME $BACKUP_DIR $ARCHIVE_NAME $CONTAINER
