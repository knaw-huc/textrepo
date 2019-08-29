-- A file is the contents of a document, identified by its SHA-224.
create table files (
  sha224 char(56) not null constraint files_pkey primary key,
  content bytea
);

-- DOCUMENTS
-- implicit; create view to support, e.g., 'select * from documents'

-- A version is the contents of a document at a specific time.
create table versions (
  document_uuid uuid not null,
  date timestamp with time zone not null,
  file_sha char(56),
  foreign key (file_sha) references files (sha224)
);

-- create index version_by_uuid on (document_uuid);

-- Document metadata items. Each item is a key-value pair linked to a
-- document.
create table metadata (
  document_uuid uuid not null,
  key varchar not null,
  value text,
  primary key (document_uuid, key)
);
