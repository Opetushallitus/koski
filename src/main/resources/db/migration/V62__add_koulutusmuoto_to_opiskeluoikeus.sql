ALTER TABLE opiskeluoikeus
  ADD IF NOT EXISTS koulutusmuoto TEXT;

UPDATE opiskeluoikeus
SET koulutusmuoto = (data -> 'tyyppi' ->> 'koodiarvo')
WHERE koulutusmuoto IS NULL;

ALTER TABLE opiskeluoikeus
  ALTER COLUMN koulutusmuoto SET NOT NULL;

CREATE INDEX IF NOT EXISTS opiskeluoikeus_koulutusmuoto_idx
  ON opiskeluoikeus (koulutusmuoto);
