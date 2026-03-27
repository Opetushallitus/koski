ALTER TABLE kielitutkintotodistus_tiedote_job
    DROP CONSTRAINT kielitutkintotodistus_tiedote_job_opiskeluoikeus_oid_key;

CREATE UNIQUE INDEX kielitutkintotodistus_tiedote_job_opiskeluoikeus_oid_active_key
    ON kielitutkintotodistus_tiedote_job (opiskeluoikeus_oid)
    WHERE state != 'DELETED';
