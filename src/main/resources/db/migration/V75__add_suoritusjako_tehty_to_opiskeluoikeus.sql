ALTER TABLE opiskeluoikeus
  ADD IF NOT EXISTS suoritusjako_tehty_rajapaivan_jalkeen BOOLEAN;

UPDATE opiskeluoikeus
SET suoritusjako_tehty_rajapaivan_jalkeen = false
WHERE suoritusjako_tehty_rajapaivan_jalkeen IS NULL;

CREATE INDEX IF NOT EXISTS suoritusjako_tehty_rajapaivan_jalkeen
  ON opiskeluoikeus (suoritusjako_tehty_rajapaivan_jalkeen);
