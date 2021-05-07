create table "opiskeluoikeus_lisätiedot"
(
    "oppija_oid"         text    not null,
    "opiskeluoikeus_oid" text    not null,
    "oppilaitos_oid"     text    not null,
    "muu_haku"           boolean not null,
    primary key (oppija_oid, opiskeluoikeus_oid, oppilaitos_oid)
);
