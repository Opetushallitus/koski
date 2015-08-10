package fi.oph.tor.db

import slick.driver.PostgresDriver
import slick.driver.PostgresDriver.api._

object TorDatabase {
  type DB = PostgresDriver.backend.DatabaseDef
  
  def forConfig(config: DatabaseConfig): DB = Database.forURL(config.url, config.user, config.password)
}





