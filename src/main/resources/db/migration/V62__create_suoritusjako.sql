CREATE TABLE suoritusjako (
  uuid TEXT,
  oppija_oid TEXT NOT NULL,
  suoritus_ids JSONB NOT NULL,
  voimassa_asti TIMESTAMP NOT NULL,
  aikaleima TIMESTAMP NOT NULL DEFAULT current_timestamp,
  primary key (uuid)
);
