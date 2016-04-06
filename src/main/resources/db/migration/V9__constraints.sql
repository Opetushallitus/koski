alter table opiskeluoikeus add constraint no_id_in_json check (not data?'id');
alter table opiskeluoikeus add constraint no_versionumero_in_json check (not data?'versionumero');