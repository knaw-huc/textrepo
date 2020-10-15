envsubst < config.yml > config-subst.yml
java -jar file.jar server config-subst.yml
