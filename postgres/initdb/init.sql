drop table if exists files;

create table files (
  sha224 char(56) not null constraint files_pkey primary key,
  name text,
  content bytea
);

alter table files owner to textrepo;
