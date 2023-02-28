CREATE TRIGGER update_ytr_opiskeluoikeus_aikaleima BEFORE INSERT OR UPDATE
  ON ytr_opiskeluoikeus FOR EACH ROW EXECUTE PROCEDURE
  update_aikaleima_column();
