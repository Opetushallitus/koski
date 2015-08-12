create table arviointi (
    id serial,
    asteikko text not null,
    numero numeric(1,0) not null,
    kuvaus text,
    primary key (id)
);

create table suoritus (
    id serial,
    parent_id integer references suoritus(id) null,
    organisaatio_oid text not null,
    person_oid text not null,
    komo_oid text not null,
    komo_tyyppi text not null,
    status text not null,
    arviointi_id integer references arviointi(id),
    primary key (id)
);