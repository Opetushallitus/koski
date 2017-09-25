ALTER TABLE opiskeluoikeus ADD mitatoity BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX opiskeluoikeus_mitatoity_idx ON opiskeluoikeus(mitatoity);
