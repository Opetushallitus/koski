CREATE TABLE preferences (
  organisaatio_oid TEXT NOT NULL,
  type TEXT NOT NULL,
  "key" TEXT NOT NULL,
  value JSONB NOT NULL,
  primary key (organisaatio_oid, "type", key)
);