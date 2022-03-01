create table "oppivelvollisuuden_keskeytyshistoria" (
    "id"                        SERIAL          PRIMARY KEY,
    "ov_keskeytys_uuid"         uuid            NOT NULL REFERENCES oppivelvollisuuden_keskeytys(uuid),
    "muutos_tehty"              TIMESTAMP       NOT NULL,
    "muutoksen_tekija"          TEXT            NOT NULL,
    "oppija_oid"                VARCHAR         NOT NULL,
    "alku"                      DATE            NOT NULL,
    "loppu"                     DATE,
    "luotu"                     TIMESTAMP       NOT NULL,
    "tekijä_oid"                TEXT            NOT NULL,
    "tekijä_organisaatio_oid"   TEXT            NOT NULL,
    "peruttu"                   BOOLEAN         NOT NULL
);
