create table "ilmoitus_opiskeluoikeus_konteksti"
(
    "ilmoitus_uuid"      uuid        not null,
    "opiskeluoikeus_oid" text        not null,
    primary key (ilmoitus_uuid, opiskeluoikeus_oid)
);

alter table "ilmoitus_opiskeluoikeus_konteksti"
    add constraint "ilmoitus_opiskeluoikeus_konteksti_fk" foreign key ("ilmoitus_uuid") references "ilmoitus" ("uuid") on update cascade on delete cascade;
