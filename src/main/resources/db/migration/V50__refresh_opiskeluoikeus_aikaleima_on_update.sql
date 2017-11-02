CREATE OR REPLACE FUNCTION update_aikaleima_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.aikaleima = current_timestamp;
  RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_opiskeluoikeus_aikaleima BEFORE UPDATE
  ON opiskeluoikeus FOR EACH ROW EXECUTE PROCEDURE
  update_aikaleima_column();