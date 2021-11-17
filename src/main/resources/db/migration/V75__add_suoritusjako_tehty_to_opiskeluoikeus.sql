ALTER TABLE opiskeluoikeus
  ADD COLUMN IF NOT EXISTS suoritusjako_tehty_rajapaivan_jalkeen BOOLEAN DEFAULT FALSE;
