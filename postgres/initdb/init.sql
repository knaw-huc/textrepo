create table if not exists files (
  sha1 varchar(40) not null constraint files_pkey primary key,
  name text,
  content bytea
);

alter table files owner to textrepo;

