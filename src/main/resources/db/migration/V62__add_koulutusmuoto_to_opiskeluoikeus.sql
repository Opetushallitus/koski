ALTER TABLE opiskeluoikeus
  ADD koulutusmuoto TEXT;

UPDATE opiskeluoikeus
SET koulutusmuoto = (data -> 'tyyppi' ->> 'koodiarvo');

ALTER TABLE opiskeluoikeus
  ALTER COLUMN koulutusmuoto SET NOT NULL;

CREATE INDEX opiskeluoikeus_koulutusmuoto_idx
  ON opiskeluoikeus (koulutusmuoto);
