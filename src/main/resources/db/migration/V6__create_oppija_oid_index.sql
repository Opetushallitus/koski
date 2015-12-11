DROP INDEX opinto_oikeus_unique_idx;

ALTER TABLE opiskeluoikeus ALTER COLUMN oppija_oid SET NOT NULL;

CREATE INDEX opiskeluoikeus_oppija_oid_idx ON opiskeluoikeus (oppija_oid);