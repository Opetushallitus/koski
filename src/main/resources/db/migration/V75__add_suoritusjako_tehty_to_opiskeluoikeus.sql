ALTER TABLE opiskeluoikeus
  ADD IF NOT EXISTS suoritusjako_tehty BOOLEAN;

UPDATE opiskeluoikeus
SET suoritusjako_tehty = false
WHERE koulutusmuoto IS NULL;

CREATE INDEX IF NOT EXISTS opiskeluoikeus_suoritusjako_tehty_idx
  ON opiskeluoikeus (suoritusjako_tehty);
