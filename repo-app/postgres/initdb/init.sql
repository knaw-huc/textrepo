create table if not exists files (
  id integer not null constraint files_pkey primary key,
  name text,
  content bytea
);

alter table files owner to textrepo;

