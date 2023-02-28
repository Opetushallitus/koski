CREATE TABLE ytr_opiskeluoikeus (LIKE opiskeluoikeus INCLUDING ALL);

ALTER TABLE ytr_opiskeluoikeus
    ADD CONSTRAINT ytr_opiskeluoikeus_oppija_oid FOREIGN KEY (oppija_oid) REFERENCES henkilo (oid);

ALTER TABLE ytr_opiskeluoikeus
    ADD CONSTRAINT ytr_opiskeluoikeus_sisaltava_ytr_opiskeluoikeus_oid_fkey FOREIGN KEY (sisaltava_opiskeluoikeus_oid) REFERENCES ytr_opiskeluoikeus (oid);

CREATE TABLE ytr_opiskeluoikeushistoria (LIKE opiskeluoikeushistoria INCLUDING ALL);

ALTER TABLE ytr_opiskeluoikeushistoria
    ADD CONSTRAINT ytr_opiskeluoikeushistoria_ytr_opiskeluoikeus_id FOREIGN KEY (opiskeluoikeus_id) REFERENCES ytr_opiskeluoikeus (id);

CREATE TABLE ytr_paivitetty_opiskeluoikeus (LIKE paivitetty_opiskeluoikeus INCLUDING ALL);

CREATE TABLE ytr_opiskeluoikeushistoria_virheet (LIKE opiskeluoikeushistoria_virheet INCLUDING ALL);
