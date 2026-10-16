-- Käsin SQL:llä poistetut opiskeluoikeudet jäivät perustiedot-indeksiin, koska poisto ei kulkenut Kosken kautta.
-- Laukaisin jonottaa poiston indeksiin. Viiteavain estäisi jonorivin, joka viittaa jo poistettuun opiskeluoikeuteen.
-- Idempotentti, jotta tämän voi ajaa tuotantoon käsin ennen julkaisua (ks. documentation/tietokantamigraatiot.md):
-- viiteavaimen poisto lukitsee opiskeluoikeus-taulun hetkeksi kokonaan, myös lukuja vastaan.
ALTER TABLE perustiedot_sync DROP CONSTRAINT IF EXISTS perustiedot_sync_opiskeluoikeus_id_fkey;

CREATE INDEX IF NOT EXISTS perustiedot_sync_opiskeluoikeus_id_idx ON perustiedot_sync (opiskeluoikeus_id);

-- Odottavat päivitykset poistetaan kuten viiteavaimen ON DELETE CASCADE teki, jotta mikään ei ohita poistoa.
-- search_path kiinnitetään, koska käsin ajetun poiston istunnossa Kosken skeema ei välttämättä ole polulla.
-- Ei FROM CURRENT: Flyway ei aseta search_pathia, ja oletuspolun "$user" osoittaisi poistajan omaan skeemaan.
CREATE OR REPLACE FUNCTION add_perustiedot_sync_delete_row()
RETURNS TRIGGER AS $$
    BEGIN
        DELETE FROM perustiedot_sync WHERE opiskeluoikeus_id = OLD.id;
        INSERT INTO perustiedot_sync
                    (opiskeluoikeus_id, data, upsert)
             VALUES (OLD.id, '{}', false);
        RETURN OLD;
    END;
$$ language 'plpgsql'
SET search_path = oph;

CREATE OR REPLACE TRIGGER opiskeluoikeuden_poisto
    AFTER DELETE
    ON opiskeluoikeus
    FOR EACH ROW
    EXECUTE FUNCTION add_perustiedot_sync_delete_row();
