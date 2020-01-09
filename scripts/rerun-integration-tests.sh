#!/usr/bin/env bash
set -e
set -x
docker exec tr_concordion rm -rf /concordion/src
docker cp ./concordion/src tr_concordion:/concordion/src
docker cp ./concordion/pom.xml tr_concordion:/concordion
docker exec tr_concordion mvn clean test
