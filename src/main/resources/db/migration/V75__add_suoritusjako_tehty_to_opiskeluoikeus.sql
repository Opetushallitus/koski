ALTER TABLE opiskeluoikeus
  ADD IF NOT EXISTS suoritusjako_tehty_2021_11_15_jalkeen BOOLEAN;

UPDATE opiskeluoikeus
SET suoritusjako_tehty_2021_11_15_jalkeen = false
WHERE suoritusjako_tehty_2021_11_15_jalkeen IS NULL;

CREATE INDEX IF NOT EXISTS opiskeluoikeus_suoritusjako_tehty_2021_11_15_jalkeen_idx
  ON opiskeluoikeus (suoritusjako_tehty_2021_11_15_jalkeen);
