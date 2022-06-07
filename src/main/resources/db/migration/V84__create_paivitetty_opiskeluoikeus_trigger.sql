CREATE OR REPLACE FUNCTION add_paivitetty_opiskeluoikeus_row()
RETURNS TRIGGER AS $$
    BEGIN
        INSERT INTO paivitetty_opiskeluoikeus
                    (opiskeluoikeus_oid, aikaleima)
             VALUES (NEW.oid, NEW.aikaleima);
        RETURN NEW;
    END;
$$ language 'plpgsql';

CREATE TRIGGER opiskeluoikeuden_paivitys
    AFTER INSERT OR UPDATE
    ON opiskeluoikeus
    FOR EACH ROW
    EXECUTE FUNCTION add_paivitetty_opiskeluoikeus_row();
