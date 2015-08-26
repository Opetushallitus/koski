package fi.oph.tor.db

import slick.driver.PostgresDriver
import slick.driver.PostgresDriver.api._

object TorDatabase {
  type DB = PostgresDriver.backend.DatabaseDef
  
  def forConfig(config: DatabaseConfig)(implicit executor: AsyncExecutor): DB = Database.forURL(config.url, config.user, config.password, executor = executor)
}





