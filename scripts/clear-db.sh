#!/usr/bin/env bash
set -e
set -x
PGPASSWORD=textrepo psql -h localhost -p 5432 -U textrepo -d textrepo -c "select truncate_tables_by_owner('textrepo');"
