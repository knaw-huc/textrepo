-- FILES
create table files (
  sha224 char(56) not null constraint files_pkey primary key,
  name text,
  content bytea
);

-- DOCUMENTS
-- implicit; create view to support, e.g., 'select * from documents'

-- VERSIONS
create table versions (
  document_uuid uuid not null,
  version_number int not null,
  date timestamp with time zone not null,
  file_sha char(56),
  primary key (document_uuid, version_number),
  foreign key (file_sha) references files (sha224)
);

-- METADATA
create table metadata (
  document_uuid uuid not null,
  key varchar not null,
  value text,
  primary key (document_uuid, key)
);
