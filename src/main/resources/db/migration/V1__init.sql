create table komoto (
    id serial,
    nimi text,
    kuvaus text,
    komo_id text,
    komo_tyyppi text,
    koodisto_id text,
    koodisto_koodi text,
    eperuste text,
    primary key (id)
);

create table suoritus (
    id serial,
    parent_id integer references suoritus(id) null,
    suorituspaiva timestamp with time zone,
    jarjestaja_organisaatio_id text not null,
    myontaja_organisaatio_id text not null,
    oppija_id text not null,
    status text not null,
    komoto_id integer references komoto(id),
    kuvaus text,
    primary key (id)
);

create table arviointi (
    id serial,
    asteikko text not null,
    numero numeric(1,0) not null,
    kuvaus text,
    suoritus_id integer references suoritus(id),
    primary key (id)
);

create table koodistoviite(
    id serial,
    suoritus_id integer references suoritus(id) not null,
    koodisto_id text,
    koodisto_koodi text,
    primary key (id)
);