CREATE TABLE kysely (
    "id" UUID PRIMARY KEY,
    "user_oid" text NOT NULL,
    "session" jsonb NOT NULL,
    "query" jsonb NOT NULL,
    "state" text NOT NULL,
    "created_at" timestamp with time zone NOT NULL DEFAULT now(),
    "started_at" timestamp with time zone,
    "finished_at" timestamp with time zone,
    "worker" text,
    "result_files" text[],
    "error" text
);

CREATE INDEX "kysely_index_for_take_next" ON kysely("state" text_ops);
CREATE INDEX "kysely_index_for_get_existing" ON kysely("user_oid" text_ops, "query" jsonb_ops);
