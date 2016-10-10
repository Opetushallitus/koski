alter table opiskeluoikeus add oppilaitos_oid text;
alter table opiskeluoikeus add koulutustoimija_oid text;

update opiskeluoikeus
set oppilaitos_oid = (data -> 'oppilaitos' ->> 'oid'), koulutustoimija_oid = (data -> 'koulutustoimija' ->> 'oid');

alter table opiskeluoikeus alter column oppilaitos_oid set not null;