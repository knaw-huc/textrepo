-- Contents of a file is identified by its SHA-224.
create table contents (
  sha224 char(56) primary key,
  content bytea
);

-- Types of files in use (e.g., FoLia, Alto, PageXML, TEI, ...)
create table types (
  id serial primary key,
  name varchar (16) not null unique
);

-- A file has a type and versioned contents
create table files (
  id uuid primary key,
  type_id serial not null,
  foreign key (type_id) references types (id)
);

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

-- A document may consist of several files and metadata, all are optional
create table documents (
  id uuid primary key
);

create table document_files (
  document_id uuid not null,
  file_id uuid not null,
  foreign key (document_id) references documents (id),
  foreign key (file_id) references files (id)
);

-- Document metadata items. Each item is a key-value pair linked to a
-- file.
create table document_metadata (
  document_id uuid not null,
  key varchar not null,
  value text,
  primary key (document_id, key),
  foreign key (document_id) references documents (id)
);
