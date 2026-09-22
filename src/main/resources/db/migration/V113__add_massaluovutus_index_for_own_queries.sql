CREATE INDEX "massaluovutus_index_for_own_queries" ON massaluovutus("user_oid" text_ops, "created_at" timestamptz_ops);
