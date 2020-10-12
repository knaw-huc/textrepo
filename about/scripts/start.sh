#!/bin/sh

rm -rf ./target/
mkdir ./target/

envsubst < src/index.json > target/index.json
envsubst < src/index.html > target/index.html
