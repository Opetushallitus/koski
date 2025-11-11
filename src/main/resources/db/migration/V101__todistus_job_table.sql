CREATE TABLE todistus_job (
    "id" UUID PRIMARY KEY,
    "user_oid" text NOT NULL,
    "oppija_oid" text NOT NULL,
    "opiskeluoikeus_oid" text NOT NULL,
    "language" text NOT NULL,
    "opiskeluoikeus_versionumero" integer,
    "oppija_henkilotiedot_hash" text,
    "state" text NOT NULL,
    "created_at" timestamp with time zone NOT NULL DEFAULT now(),
    "started_at" timestamp with time zone,
    "completed_at" timestamp with time zone,
    "worker" text,
    "attempts" integer NOT NULL DEFAULT 0,
    "error" text
);

CREATE INDEX "todistus_job_index_for_take_next" ON todistus_job(state);

CREATE INDEX "todistus_job_index_for_find_by_parameters" ON todistus_job(opiskeluoikeus_oid, language, state);
