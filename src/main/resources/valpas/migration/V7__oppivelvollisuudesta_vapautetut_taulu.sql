-- Table Definition ----------------------------------------------

CREATE TABLE oppivelvollisuudesta_vapautetut (
    oppija_oid text NOT NULL,
    virkailija_oid text NOT NULL,
    kunta_koodiarvo text NOT NULL,
    vapautettu date NOT NULL,
    aikaleima timestamp without time zone NOT NULL DEFAULT now(),
    mitatoity timestamp without time zone
);

-- Indices -------------------------------------------------------

CREATE INDEX oppivelvollisuudesta_vapautetut_oppija_oid_mitatoity_idx ON oppivelvollisuudesta_vapautetut(oppija_oid text_ops,mitatoity timestamp_ops);
