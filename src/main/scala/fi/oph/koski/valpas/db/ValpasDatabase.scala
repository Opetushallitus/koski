package fi.oph.koski.valpas.db

import fi.oph.koski.db.{DB, DatabaseConfig}
import fi.oph.koski.log.Logging

class ValpasDatabase(config: DatabaseConfig) extends Logging {
  val db: DB = config.toSlickDatabase
}
