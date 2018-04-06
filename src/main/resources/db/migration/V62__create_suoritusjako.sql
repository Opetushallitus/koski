CREATE TABLE suoritusjako (
  id BIGSERIAL UNIQUE,
  secret TEXT UNIQUE NOT NULL,
  oppija_oid TEXT REFERENCES henkilo (oid),
  suoritus_ids JSONB NOT NULL,
  voimassa_asti DATE NOT NULL,
  aikaleima TIMESTAMPTZ NOT NULL,
  primary key (id)
);
