#!/bin/sh

envsubst < config.yml > config-subst.yml
java -jar textrepo.jar server config-subst.yml
