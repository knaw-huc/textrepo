-- Contents of a file is identified by its SHA-224.
create table contents (
  sha224 char(56) primary key,
  content bytea
);

-- Types of files in use (e.g., FoLia, Alto, PageXML, TEI, ...)
create table types (
  id smallserial primary key,
  name varchar (16) not null unique,
  mimetype varchar (100) not null DEFAULT 'text/plain'
);

-- A file has a type and versioned contents
create table files (
  id uuid primary key,
  type_id smallserial not null,
  foreign key (type_id) references types (id)
);

create index files_by_type_id on files (type_id);

-- A version is the contents of a file at a specific time.
create table versions (
  file_id uuid not null,
  contents_sha char(56),
  created_at timestamp with time zone not null,
  primary key (file_id, created_at),
  foreign key (file_id) references files (id),
  foreign key (contents_sha) references contents (sha224)
);

create index version_by_file_id on versions (file_id);

-- A document has an external id
create table documents(
  id uuid primary key,
  external_id varchar not null unique
);

-- assist FK lookups: add compound index for PK fields in reverse order
create unique index doc_id_by_external_id on documents (external_id, id);

create table documents_files (
  document_id uuid not null,
  file_id uuid not null references files (id),
  primary key (document_id, file_id),		-- force unique index on pair
  foreign key (document_id) references documents (id)
);

-- assist FK lookups: add compound index for PK fields in reverse order
create unique index doc_files_by_file_id on documents_files (file_id, document_id);

-- Document metadata items. Each item is a key-value pair linked to a document.
create table documents_metadata (
  document_id uuid not null,
  key varchar not null,
  value text,
  primary key (document_id, key),
  foreign key (document_id) references documents (id)
);

-- File metadata items (e.g., filename). Key-value pairs linked to file.
create table files_metadata (
  file_id uuid not null,
  key varchar not null,
  value text,
  primary key (file_id, key)
);
