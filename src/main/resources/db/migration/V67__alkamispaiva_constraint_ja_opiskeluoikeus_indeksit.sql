ALTER TABLE opiskeluoikeus ALTER COLUMN alkamispaiva SET NOT NULL;
CREATE INDEX IF NOT EXISTS opiskeluoikeus_alkamispaiva_idx ON opiskeluoikeus (alkamispaiva);
CREATE INDEX IF NOT EXISTS opiskeluoikeus_paattymispaiva_idx ON opiskeluoikeus (paattymispaiva);
