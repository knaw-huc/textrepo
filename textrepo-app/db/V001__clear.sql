-- Cleanup existing database, dropping all tables and indices

-- Note Drop in reverse order of creation to ensure constraints are dropped
-- before the tables that those constraints depend on, are removed.

drop table if exists files_metadata;
drop table if exists documents_metadata;
drop table if exists versions_metadata;
drop table if exists versions;
drop table if exists documents_files;
drop table if exists files;
drop table if exists types;
drop table if exists contents;
drop table if exists documents;
