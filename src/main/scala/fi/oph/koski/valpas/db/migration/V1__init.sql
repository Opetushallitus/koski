create table "ilmoitus"
(
    "id"                      serial                  not null primary key,
    "luotu"                   timestamp default now() not null,
    "oppija_oid"              text                    not null,
    "kunta_oid"               text                    not null,
    "tekijä_organisaatio_oid" text                    not null,
    "tekijä_oid"              text
);

create table "ilmoitus_yhteystiedot"
(
    "id"                serial  not null primary key,
    "ilmoitus_id"       integer not null,
    "yhteydenottokieli" text,
    "puhelin"           text,
    "sähköposti"        text,
    "lähiosoite"        text,
    "postinumero"       text,
    "postitoimipaikka"  text,
    "maa"               text
);

alter table "ilmoitus_yhteystiedot"
    add constraint "ilmoitus_fk" foreign key ("ilmoitus_id") references "ilmoitus" ("id") on update cascade on delete cascade;
