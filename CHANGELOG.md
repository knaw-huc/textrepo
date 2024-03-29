# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Do not forget to include:
 - Changes in the environment variables
 - Changes to the database

## [branch: txt_anchor / xml_view]

### Changed
- Environment variable NGINX_LOCATION no longer has ends in a trailing '/'

## [1.18.0]

### Changed
- Indexers should have a types endpoint.
- RESTful deletes result in a `204 no content`

### Added
Indices are now created, updated and deleted when versions and files are created, updated and deleted. 

## [1.17.0]

### Changed
Introduced database migrations using flyway. 
If the current database should not be erased, make sure to use `./mark-db-baseline.sh` when upgrading to this version.

Current custom postgres image is replaced with a generic postgres alpine image.

### Added
- Environment variables: TR_FLYWAY_CLEAN_DISABLED, TR_FLYWAY_LOCATIONS

## [1.16.2]

### Changed
Link to elasticsearch service using container name `tr_elasticsearch:9200`

### Added
- Environment variables: NGINX_ES_HOST

## [1.14.0]

### Changed
- Autocomplete and full-text indexers now index "subtypes" of a mimetype as if they where that mimetype. E.g. pagexml of 'application/vnd.prima.page+xml' is indexed as ordinary xml or 'application/xml'. See `Added` for new env vars.

### Added
- Environment variables: AUTOCOMPLETE_XML_SUBTYPES, AUTOCOMPLETE_TXT_SUBTYPES, FULL_TEXT_XML_SUBTYPES, FULL_TEXT_TXT_SUBTYPES

## [1.13.1]

## Fixed
- Continue indexing by indexer when bumping into a file without versions
- Autocomplete and full-text indexers can handle empty files now 

### Changed
- Set `shm_size` in docker-compose setup to increase default 64mb limit 

## [1.13.0]

### Added
- Version metadata, see: `./postgres/initdb/04-add-version-metadata-table.sql`

[v1.12.1]
### Fixed
- Show commit and tag name in textrepo-app and about images using docker build hooks

## [v1.12]
- Fixed various build issues to get things going on Docker Hub

## [v1.11]

### Added
- Show 'about' page `index.html` (with link to `index.json`) on nginx landing page, with version info and links to services, documentation and source code.
- Show version and configuration info at root of textrepo API
- Resources now send 'Content-Encoding: gzip' when sending gzip'd contents
- File indexer, more info in readme: `./elasticsearch/file`
- Origin Link header to indexer fields request

### Changed
- Renamed env var `TR_VERSION` to `DOCKER_TAG` 
 (as it is named in docker builds: https://docs.docker.com/docker-hub/builds/advanced)
- Renamed env var `NGINX_ROOT_REDIRECT` to `CONCORDION_ROOT_REDIRECT`
- Database queries using 'count' now use 'long' as their Java counterpart (was: int)

## [v1.10]
- [TT-631] When yielding contents, return gzip'd content when 'Accept-Encoding: gzip'
- [TT-645] Resources now send 'Content-Encoding: gzip' when sending gzip'd contents
- [TT-574] Database queries using 'count' now use 'long' as their Java counterpart (was: int)

### Fixed
- Use `tar.gz` instead of `tar` as backup extension

## [v1.9]
### Added
- 'contentDecompressionLimit' (kB) configures plain/gzip output cutoff in HTTP results
- docker-compose.yml and docker-compose.env need TR\_CONTENT\_DECOMPRESSION\_LIMIT stanzas
### Changed
- Use timestamp without time zone in versions and documents tables
- Use (gzip) compressed contents in contents table (migration code available)
- Use content-type text/plain instead of application/json when putting metadata
