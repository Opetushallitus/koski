CREATE TABLE suoritusjako_v2 (
  secret TEXT PRIMARY KEY,
  oppija_oid TEXT NOT NULL,
  data JSONB NOT NULL,
  voimassa_asti DATE NOT NULL,
  aikaleima TIMESTAMPTZ NOT NULL
);
