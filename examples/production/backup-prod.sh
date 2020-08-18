#!/usr/bin/env bash
set -e

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

BACKUP_DIR=~/backup
CONTAINER_DIR=/var/lib/textrepo/data
ARCHIVE_NAME=postgresdata-prod-volume.tar
CONTAINER=tr_postgres
backup_volume $BACKUP_DIR $CONTAINER_DIR $ARCHIVE_NAME $CONTAINER
