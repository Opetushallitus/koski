CREATE TABLE mydata_jako (
  oppija_oid TEXT NOT NULL,
  asiakas TEXT NOT NULL,
  voimassa_asti DATE NOT NULL,
  aikaleima TIMESTAMPTZ NOT NULL,
  primary key (oppija_oid, asiakas)
);
