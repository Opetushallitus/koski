CREATE TABLE oph.paivitetty_opiskeluoikeus (
    id serial,
    opiskeluoikeus_oid text NOT NULL,
    aikaleima timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    prosessoitu boolean NOT NULL DEFAULT false
);

CREATE UNIQUE INDEX paivitetty_opiskeluoikeus_pkey ON oph.paivitetty_opiskeluoikeus(id int4_ops);
CREATE INDEX paivitetty_opiskeluoikeus_aikaleima_idx ON oph.paivitetty_opiskeluoikeus(aikaleima timestamptz_ops);
CREATE INDEX paivitetty_opiskeluoikeus_prosessoitu_idx ON oph.paivitetty_opiskeluoikeus(prosessoitu bool_ops);
