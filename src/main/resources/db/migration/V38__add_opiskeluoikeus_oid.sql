ALTER TABLE opiskeluoikeus ADD oid TEXT;
ALTER TABLE opiskeluoikeus DROP COLUMN sisaltava_opiskeluoikeus_id CASCADE;
ALTER TABLE opiskeluoikeus ADD COLUMN sisaltava_opiskeluoikeus_oid TEXT;
ALTER TABLE opiskeluoikeus ADD CONSTRAINT no_oid_in_json CHECK (NOT data?'oid');
