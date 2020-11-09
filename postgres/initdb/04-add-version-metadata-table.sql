-- Version metadata: each entry is a key-value pair linked to a version.
create table versions_metadata (
  version_id uuid not null,
  key varchar not null,
  value text,
  primary key (version_id, key),
  constraint versions_metadata_version_id_fkey foreign key (version_id) references versions (id) on delete cascade
);
