ALTER TABLE preferences ADD koulutustoimija_oid TEXT;
CREATE UNIQUE index unique_preference_idx ON preferences (organisaatio_oid, type, key, koulutustoimija_oid);
