CREATE TABLE scheduler (
  name TEXT NOT NULL,
  nextfiretime TIMESTAMP NOT NULL,
  primary key (name)
);