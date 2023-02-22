CREATE TABLE ytr_alkuperainen_data (
  oppija_oid TEXT NOT NULL,
  aikaleima TIME WITH TIME ZONE NOT NULL DEFAULT now(),
  data JSONB,
  primary key (oppija_oid)
)
