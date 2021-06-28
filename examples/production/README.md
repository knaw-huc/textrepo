# Production docker-compose setup

To start production setup for the first time:
- Cd to`./examples/production`
- Add `wait-for-it.sh` to `./scripts`
- And run `./start-prod.sh` to download images and start containers

- Run `./stop-prod.sh` to stop containers
- Run `./start-integration.sh` to run integration tests without touching production volumes
- Run `./stop-integration.sh` to stop integration containers without losing integration test results
- Run `./backup-prod.sh` to gzip postgres data and elasticsearch snapshot
- Run `./restore-prod.sh` to populate postgres and elasticsearch with data from gzipped backups
- Use `./log.sh` to view logging.

See also: `./docs/example-production.rst`

## Upgrading existing databases
If the current database should not be erased, make sure to use `./mark-db-baseline.sh` when upgrading to this version.
