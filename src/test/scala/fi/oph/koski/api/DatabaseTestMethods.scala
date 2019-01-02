package fi.oph.koski.api

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.KoskiDatabaseMethods

trait DatabaseTestMethods extends KoskiDatabaseMethods {
  override protected def db: DB = KoskiApplicationForTests.masterDatabase.db
}
