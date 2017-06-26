ALTER TABLE opiskeluoikeus ADD sisaltava_opiskeluoikeus_id INTEGER REFERENCES opiskeluoikeus (id) NULL;
ALTER TABLE opiskeluoikeus ADD sisaltava_opiskeluoikeus_oppilaitos_oid TEXT NULL;