ALTER TABLE opiskeluoikeus ADD oid TEXT NOT NULL UNIQUE;
ALTER TABLE opiskeluoikeus DROP COLUMN sisaltava_opiskeluoikeus_id CASCADE;
ALTER TABLE opiskeluoikeus ADD COLUMN sisaltava_opiskeluoikeus_oid TEXT;
ALTER TABLE opiskeluoikeus ADD CONSTRAINT oid_matches_oid_in_json CHECK (oid = (data->>'oid'));
