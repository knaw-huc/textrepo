create table if not exists files (
  sha224 bit(224) not null constraint files_pkey primary key,
  name text,
  content bytea
);

alter table files owner to textrepo;
