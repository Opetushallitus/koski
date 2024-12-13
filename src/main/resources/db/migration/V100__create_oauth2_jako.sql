CREATE TABLE oauth2_jako (
  code_sha256 TEXT NOT NULL,
  oppija_oid TEXT NOT NULL,
  client_id TEXT NOT NULL,

  scope TEXT NOT NULL,
  code_challenge TEXT NOT NULL,
  redirect_uri TEXT NOT NULL,

  access_token_sha256 TEXT UNIQUE,

  code_voimassa_asti TIMESTAMPTZ NOT NULL,
  voimassa_asti TIMESTAMPTZ NOT NULL,

  luotu TIMESTAMPTZ NOT NULL,
  muokattu TIMESTAMPTZ NOT NULL,

  mitatoity BOOLEAN NOT NULL DEFAULT FALSE,
  mitatoity_syy TEXT,

  primary key (code_sha256)
);

CREATE OR REPLACE FUNCTION update_oauth2_jako_aikaleima_columns()
RETURNS TRIGGER AS $$
BEGIN
  IF row(NEW.*) IS DISTINCT FROM row(OLD.*) THEN
    NEW.muokattu = current_timestamp;
    NEW.luotu = OLD.luotu; -- estä luontiaikaleiman muokkaus vahingossa
  RETURN NEW;
  ELSE
    RETURN OLD;
  END IF;
END;
$$ language 'plpgsql';

CREATE OR REPLACE FUNCTION insert_oauth2_jako_aikaleima_columns()
RETURNS TRIGGER AS $$
BEGIN
  NEW.muokattu = current_timestamp;
  NEW.luotu = current_timestamp;
  RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_oauth2_aikaleimat BEFORE UPDATE
  ON oauth2_jako FOR EACH ROW EXECUTE PROCEDURE
    update_oauth2_jako_aikaleima_columns();

CREATE TRIGGER insert_oauth2_aikaleimat BEFORE INSERT
    ON oauth2_jako FOR EACH ROW EXECUTE PROCEDURE
    insert_oauth2_jako_aikaleima_columns();

CREATE INDEX IF NOT EXISTS oauth2_jako_oppija_oid_idx ON oauth2_jako(oppija_oid);
CREATE INDEX IF NOT EXISTS oauth2_jako_mitatoity_idx ON oauth2_jako(mitatoity);
