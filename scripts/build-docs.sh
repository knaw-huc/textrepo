#!/usr/bin/env bash

# Run to preview docs

if [[ ! -d "./docs" ]] ; then echo "./docs not found, aborting."; exit; fi

sphinx-build -b html ./docs ./docs/target
