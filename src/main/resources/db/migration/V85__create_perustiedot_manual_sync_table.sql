CREATE TABLE perustiedot_manual_sync (
  id SERIAL,
  opiskeluoikeus_oid text REFERENCES opiskeluoikeus(oid) ON DELETE CASCADE NOT NULL,
  upsert boolean,
  aikaleima TIMESTAMP NOT NULL DEFAULT current_timestamp,
  primary key (id)
);
