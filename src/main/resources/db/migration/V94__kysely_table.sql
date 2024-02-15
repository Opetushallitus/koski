CREATE TABLE kysely (
    "id" UUID PRIMARY KEY,
    "requested_by" text NOT NULL,
    "query" jsonb NOT NULL,
    "state" text NOT NULL,
    "creation_time" timestamp with time zone NOT NULL DEFAULT now(),
    "work_start_time" timestamp with time zone,
    "end_time" timestamp with time zone,
    "worker" text,
    "result_files" text[],
    "error" text
);

CREATE INDEX "kysely_index_for_take_next" ON kysely("state" text_ops);
CREATE INDEX "kysely_index_for_get_existing" ON kysely("requested_by" text_ops, "query" jsonb_ops);
