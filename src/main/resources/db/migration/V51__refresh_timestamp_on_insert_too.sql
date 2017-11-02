DROP TRIGGER update_opiskeluoikeus_aikaleima ON opiskeluoikeus;
CREATE TRIGGER update_opiskeluoikeus_aikaleima BEFORE INSERT OR UPDATE
  ON opiskeluoikeus FOR EACH ROW EXECUTE PROCEDURE
  update_aikaleima_column();