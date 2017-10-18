CREATE TABLE perustiedot_sync (
  id SERIAL,
  opiskeluoikeus_id INTEGER REFERENCES opiskeluoikeus ON DELETE CASCADE NOT NULL,
  fail_count INTEGER NOT NULL DEFAULT 1,
  aikaleima TIMESTAMP NOT NULL DEFAULT current_timestamp,
  primary key (id)
);