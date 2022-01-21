CREATE INDEX IF NOT EXISTS opiskeluoikeus_suoritustyypit_index ON opiskeluoikeus USING GIN(suoritustyypit);
