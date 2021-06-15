create table "oppivelvollisuuden_keskeytys" (
    "uuid"              uuid            NOT NULL PRIMARY KEY,
    "oppija_oid"        VARCHAR         NOT NULL,
    "alku"              DATE            NOT NULL,
    "loppu"             DATE,
    "luotu"             TIMESTAMP       NOT NULL,
    "tekijä_oid"        VARCHAR         NOT NULL,
    "peruttu"           BOOLEAN         NOT NULL
);
