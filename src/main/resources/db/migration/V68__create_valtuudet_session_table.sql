CREATE TABLE valtuudet_session (
  oppija_oid TEXT NOT NULL,
  session_id TEXT NOT NULL,
  user_id TEXT NOT NULL,
  code TEXT,
  access_token TEXT,
  aikaleima TIMESTAMP NOT NULL DEFAULT current_timestamp,
  primary key (oppija_oid)
);
