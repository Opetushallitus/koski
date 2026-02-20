CREATE TABLE kielitutkintotodistus_tiedote_job (
    id UUID PRIMARY KEY,
    oppija_oid text NOT NULL,
    opiskeluoikeus_oid text NOT NULL UNIQUE,
    state text NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT now(),
    completed_at timestamp with time zone,
    worker text,
    attempts integer NOT NULL DEFAULT 0,
    error text,
    opiskeluoikeus_versio integer NOT NULL
);

CREATE INDEX kielitutkintotodistus_tiedote_job_state_idx
    ON kielitutkintotodistus_tiedote_job(state);

CREATE INDEX kielitutkintotodistus_tiedote_job_created_at_idx
    ON kielitutkintotodistus_tiedote_job(created_at DESC);
