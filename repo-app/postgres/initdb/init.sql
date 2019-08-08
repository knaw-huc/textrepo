create table if not exists files (
  id integer not null constraint files_pkey primary key,
  name varchar(500)
);

alter table files owner to textrepo;

