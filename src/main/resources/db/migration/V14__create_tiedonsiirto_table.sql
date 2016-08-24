CREATE TABLE tiedonsiirto (
  id SERIAL,
  kayttaja_oid TEXT NOT NULL,
  tallentaja_organisaatio_oid TEXT NOT NULL,
  data JSONB,
  aikaleima TIMESTAMP NOT NULL DEFAULT current_timestamp,
  primary key (id)
);