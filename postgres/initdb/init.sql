CREATE TABLE IF NOT EXISTS files (
  hash character varying(40) NOT NULL,
  testfield character varying(200) NOT NULL,
  time_created timestamp with time zone,
  time_changed timestamp with time zone
);
