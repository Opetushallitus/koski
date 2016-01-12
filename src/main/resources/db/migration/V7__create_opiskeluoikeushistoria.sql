CREATE TABLE opiskeluoikeushistoria (
  id SERIAL,
  opiskeluoikeus_id INTEGER REFERENCES opiskeluoikeus ON DELETE CASCADE NOT NULL,
  aikaleima TIMESTAMP NOT NULL DEFAULT current_timestamp,
  kayttaja_oid TEXT NOT NULL,
  muutos JSONB NOT NULL,
  primary key (id)
);

CREATE INDEX opiskeluoikeushistoria_opiskeluoikeus_idx ON opiskeluoikeushistoria(opiskeluoikeus_id);
