ALTER TABLE poistettu_opiskeluoikeus
    ADD COLUMN IF NOT EXISTS koulutusmuoto TEXT,
    ADD COLUMN IF NOT EXISTS suoritustyypit TEXT[],
    ADD COLUMN IF NOT EXISTS versio INTEGER;

-- Ainoastaan vstvapaatavoitteisia suorituksia on poistettu tällä hetkellä
UPDATE poistettu_opiskeluoikeus
    SET koulutusmuoto = 'vapaansivistystyonkoulutus',
        suoritustyypit = '{vstvapaatavoitteinenkoulutus}',
        versio = '0';
