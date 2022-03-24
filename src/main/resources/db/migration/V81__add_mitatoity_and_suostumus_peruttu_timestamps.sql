ALTER TABLE poistettu_opiskeluoikeus
    ADD COLUMN IF NOT EXISTS mitatoity_aikaleima TIMESTAMP,
    ADD COLUMN IF NOT EXISTS suostumus_peruttu_aikaleima TIMESTAMP;

UPDATE poistettu_opiskeluoikeus SET suostumus_peruttu_aikaleima = aikaleima;

ALTER TABLE poistettu_opiskeluoikeus
    DROP COLUMN aikaleima;
