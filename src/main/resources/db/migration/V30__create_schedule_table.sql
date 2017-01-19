CREATE TABLE scheduler (
  name TEXT NOT NULL,
  nextfiretime TIMESTAMP NOT NULL,
  context JSONB,
  primary key (name)
);