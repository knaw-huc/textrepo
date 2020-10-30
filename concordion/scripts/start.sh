#!/bin/sh

mvn clean test

# keep container up:
tail -f /dev/null
