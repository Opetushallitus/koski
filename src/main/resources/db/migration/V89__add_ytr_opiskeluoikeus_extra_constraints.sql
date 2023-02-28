ALTER TABLE ytr_opiskeluoikeus
  ADD CONSTRAINT ytr_opiskeluoikeus_only_ytr_koulutusmuoto_check CHECK (koulutusmuoto = 'ylioppilastutkinto');
