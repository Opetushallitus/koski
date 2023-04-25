DELETE FROM ytr_download_status;

ALTER TABLE ytr_download_status
DROP COLUMN nimi;

ALTER TABLE ytr_download_status
ADD COLUMN id SERIAL PRIMARY KEY;

ALTER TABLE ytr_download_status
ADD COLUMN completed timestamp;

ALTER TABLE ytr_download_status
DROP COLUMN aikaleima;

ALTER TABLE ytr_download_status
ADD COLUMN aikaleima timestamp;
