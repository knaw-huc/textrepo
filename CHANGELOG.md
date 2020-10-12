# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Show 'about' page `index.html` (with link to `index.json`) on nginx landing page, with version info and links to services, documentation and source code.

### Changed
- Renamed env var `NGINX_ROOT_REDIRECT` to `CONCORDION_ROOT_REDIRECT`
- Resources now send 'Content-Encoding: gzip' when sending gzip'd contents
- Database queries using 'count' now use 'long' as their Java counterpart (was: int)

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
