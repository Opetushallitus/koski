create table arviointi (
    id serial,
    asteikko text not null,
    numero numeric(1,0) not null,
    kuvaus text,
    primary key (id)
);

create table tutkintosuoritus (
    id serial,
    organisaatio_oid text not null,
    person_oid text not null,
    komo_oid text not null,
    status text not null,
    arviointi_id integer references arviointi(id),
    primary key (id)
);

create table tutkinnonosasuoritus (
    id serial,
    tutkintosuoritus_id integer references tutkintosuoritus(id) not null,
    komo_oid text not null,
    status text not null,
    arviointi_id integer references arviointi(id),
    primary key (id)
);

create table kurssisuoritus (
    id serial,
    tutkinnonosasuoritus_id integer references tutkinnonosasuoritus(id) not null,
    komo_oid text not null,
    status text not null,
    arviointi_id integer references arviointi(id),
    primary key (id)
);

