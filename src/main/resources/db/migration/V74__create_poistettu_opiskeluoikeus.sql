CREATE TABLE poistettu_opiskeluoikeus (
  oid TEXT NOT NULL,
  oppilaitos_nimi TEXT,
  oppilaitos_oid TEXT,
  paattymispaiva DATE,
  lahdejarjestelma_koodi TEXT,
  lahdejarjestelma_id TEXT,
  aikaleima TIMESTAMP NOT NULL,
  primary key (oid)
);
