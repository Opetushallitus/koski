ALTER TABLE suoritusjako DROP COLUMN IF EXISTS kokonaisuudet;
ALTER TABLE suoritusjako ADD COLUMN kokonaisuudet JSONB NOT NULL DEFAULT '[]'::jsonb;
ALTER TABLE suoritusjako ALTER COLUMN suoritus_ids SET DEFAULT '[]'::jsonb;
