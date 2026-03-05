ALTER TABLE kielitutkintotodistus_tiedote_job
  ADD COLUMN todistus_job_id UUID REFERENCES todistus_job(id);
