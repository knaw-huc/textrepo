#!/usr/bin/env bash

# use for development purposes only

set -e

if [[ ! -d "./textrepo-app" ]] ; then echo "./textrepo-app not found, aborting."; exit; fi

# create new jar
(cd textrepo-app && mvn clean install)

# copy jar into running container
docker cp ./textrepo-app/target/textrepo-1.0-SNAPSHOT.jar tr_textrepo:/textrepo-app/textrepo.jar
docker cp ./textrepo-app/config.yml tr_textrepo:/textrepo-app/config.yml

# save container with new jar as new image
docker commit tr_textrepo knawhuc/textrepo-app:latest

# restart container with new image
docker-compose stop textrepo-app
docker-compose up -d --no-deps textrepo-app
