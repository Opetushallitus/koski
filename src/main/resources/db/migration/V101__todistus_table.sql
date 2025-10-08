CREATE TABLE todistus (
    "id" UUID PRIMARY KEY,
    "user_oid" text NOT NULL, -- käyttäjä, joka tämän käynnisti (voi olla eri kuin oppija_oid, jos esim. viranhaltija luo todistusta oppijalle)
    "oppija_oid" text NOT NULL,
    "opiskeluoikeus_oid" text NOT NULL,
    "language" text NOT NULL, -- pyydetty todistuksen kieli (fi/sv/en)
    "opiskeluoikeus_versionumero" integer NOT NULL, -- oo-versio, mistä pdf on luotu. Voi käyttää, kun tarkistetaan, pitääkö todistus luoda uudestaan.
    "oppija_henkilotiedot_hash" text NOT NULL, -- hash todistuksella näkyvistä oppijan henkilötiedoista (etunimet, sukunimi, syntymäaika jne.). Voi käyttää, kun tarkistetaan, pitääkö todistus luoda uudestaan.
    "state" text NOT NULL,
    -- QUEUED
    -- GENERATING_RAW_PDF
    -- SAVING_RAW_PDF
    -- CONSTRUCTING_STAMP_REQUEST
    -- WAITING_STAMP_RESPONSE
    -- STAMPING_PDF
    -- SAVING_STAMPED_PFF
    -- COMPLETED
    -- EXPIRED = Vanhentuneena siivottu, myös esim. jostain muusta syystä voi lisätä tämän tilan: tällöin todistus luodaan uudestaan eikä yritetä käyttä vanhaa
    -- CANCELLED -- tarvitaanko sekä cancelled että expired?
    -- ERROR
    "created_at" timestamp with time zone NOT NULL DEFAULT now(),
    "started_at" timestamp with time zone,
    "completed_at" timestamp with time zone,
    "worker" text,
    "raw_s3_object_key" text,
    "signed_s3_object_key" text,
    "error" text
);

CREATE INDEX "todistus_index_for_take_next" ON todistus("state" text_ops);


