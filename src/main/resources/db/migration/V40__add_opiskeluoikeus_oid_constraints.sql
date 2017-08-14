ALTER TABLE opiskeluoikeus ADD UNIQUE (oid);
ALTER TABLE opiskeluoikeus ALTER COLUMN oid SET NOT NULL;
