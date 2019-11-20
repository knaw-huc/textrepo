-- Contents of a file is identified by its SHA-224.
create table contents (
  sha224 char(56) not null constraint contents_pkey primary key,
  content bytea
);

-- DOCUMENTS
-- implicit; create view to support, e.g., 'select * from documents'

-- A version is the contents of a document at a specific time.
create table versions (
  document_uuid uuid not null,
  date timestamp with time zone not null,
  contents_sha char(56),
  primary key (document_uuid, date),
  foreign key (contents_sha) references contents (sha224)
);

create index version_by_uuid on versions (document_uuid);

-- Document metadata items. Each item is a key-value pair linked to a
-- document.
create table metadata (
  document_uuid uuid not null,
  key varchar not null,
  value text,
  primary key (document_uuid, key)
);
