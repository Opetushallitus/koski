package fi.oph.koski.api

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.db.DB
import fi.oph.koski.db.QueryMethods

trait DatabaseTestMethods extends QueryMethods {
  override protected def db: DB = KoskiApplicationForTests.masterDatabase.db
}
