package fi.oph.koski.valpas.db

import fi.oph.koski.db.{Database, DatabaseConfig}
import slick.jdbc.PostgresProfile.api._

class ValpasDatabase(protected val config: DatabaseConfig) extends Database {
  override final val smallDatabaseMaxRows = 5

  override protected lazy val dbSizeQuery = ValpasSchema.Ilmoitukset.length.result
}
