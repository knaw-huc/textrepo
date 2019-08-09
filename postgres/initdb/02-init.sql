-- FILES
create table files (
  sha224 char(56) not null constraint files_pkey primary key,
  name text,
  content bytea
);
create index if not exists index_files_sha1
  on files (sha1);

-- DOCUMENTS
create table documents (
  id serial,
  uuid uuid not null constraint documents_pkey primary key
);
create index if not exists index_documents_uuid
  on documents (uuid);

-- VERSIONS
create table versions (
  id serial,
  document_uuid uuid not null,
  version_number int not null,
  date timestamp with time zone not null,
  file_sha varchar(40),
  primary key (id),
  unique (document_uuid, version_number),
  foreign key (document_uuid) references documents (uuid),
  foreign key (file_sha) references files (sha1)
);

-- METADATA
create table metadata (
  version_id int not null,
  key varchar not null,
  value text,
  primary key (version_id, key),
  foreign key (version_id) references versions (id)
);
