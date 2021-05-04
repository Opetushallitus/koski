create extension if not exists "uuid-ossp";

create table "ilmoitus"
(
    "uuid"                    uuid        not null primary key,
    "luotu"                   timestamptz not null,
    "oppija_oid"              text        not null,
    "kunta_oid"               text        not null,
    "tekijä_organisaatio_oid" text        not null,
    "tekijä_oid"              text        not null
);

create table "ilmoitus_lisätiedot"
(
    "ilmoitus_uuid" uuid  not null primary key,
    "data"          jsonb not null
);

alter table "ilmoitus_lisätiedot"
    add constraint "ilmoitus_fk" foreign key ("ilmoitus_uuid") references "ilmoitus" ("uuid") on update cascade on delete cascade;
