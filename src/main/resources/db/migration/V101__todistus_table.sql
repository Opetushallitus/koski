CREATE TABLE todistus (
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
    "raw_s3_object_key" text,
    "signed_s3_object_key" text,
    "error" text
);

CREATE INDEX "todistus_index_for_take_next" ON todistus("state" text_ops);


