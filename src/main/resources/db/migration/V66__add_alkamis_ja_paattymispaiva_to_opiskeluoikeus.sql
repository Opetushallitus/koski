ALTER TABLE opiskeluoikeus DISABLE TRIGGER update_opiskeluoikeus_aikaleima;

ALTER TABLE opiskeluoikeus ADD IF NOT EXISTS alkamispaiva DATE;
ALTER TABLE opiskeluoikeus ADD IF NOT EXISTS paattymispaiva DATE;

UPDATE opiskeluoikeus SET alkamispaiva = (data -> 'tila' -> 'opiskeluoikeusjaksot' -> 0 ->> 'alku') :: DATE
WHERE alkamispaiva IS NULL;

UPDATE opiskeluoikeus SET paattymispaiva = (data -> 'tila' -> 'opiskeluoikeusjaksot' -> (jsonb_array_length(data -> 'tila' -> 'opiskeluoikeusjaksot') - 1) ->> 'alku') :: DATE
WHERE paattymispaiva IS NULL
  AND data -> 'tila' -> 'opiskeluoikeusjaksot' -> (jsonb_array_length(data -> 'tila' -> 'opiskeluoikeusjaksot') - 1) -> 'tila' ->> 'koodiarvo' IN ('valmistunut', 'eronnut', 'peruutettu', 'katsotaaneronneeksi');

ALTER TABLE opiskeluoikeus ALTER COLUMN alkamispaiva SET NOT NULL;

CREATE INDEX IF NOT EXISTS opiskeluoikeus_alkamispaiva_idx ON opiskeluoikeus (alkamispaiva);
CREATE INDEX IF NOT EXISTS opiskeluoikeus_paattymispaiva_idx ON opiskeluoikeus (paattymispaiva);

ALTER TABLE opiskeluoikeus ENABLE TRIGGER update_opiskeluoikeus_aikaleima;
