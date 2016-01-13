ALTER TABLE opiskeluoikeus
  ADD aikaleima TIMESTAMP NOT NULL DEFAULT current_timestamp,
  ADD versionumero INTEGER NOT NULL DEFAULT 1;

ALTER TABLE opiskeluoikeushistoria
  ADD versionumero INTEGER NOT NULL DEFAULT 1;

DELETE FROM opiskeluoikeushistoria;
ALTER TABLE opiskeluoikeushistoria DROP CONSTRAINT opiskeluoikeushistoria_pkey;
ALTER TABLE opiskeluoikeushistoria ADD PRIMARY KEY (opiskeluoikeus_id, versionumero);
ALTER TABLE opiskeluoikeushistoria DROP COLUMN id;