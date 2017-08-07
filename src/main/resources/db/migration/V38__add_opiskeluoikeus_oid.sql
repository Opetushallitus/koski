ALTER TABLE opiskeluoikeus ADD oid TEXT UNIQUE;
ALTER TABLE opiskeluoikeus DROP COLUMN sisaltava_opiskeluoikeus_id CASCADE;
ALTER TABLE opiskeluoikeus ADD COLUMN sisaltava_opiskeluoikeus_oid TEXT;
