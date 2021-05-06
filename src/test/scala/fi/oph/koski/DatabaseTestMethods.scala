package fi.oph.koski

import fi.oph.koski.db.{DB, QueryMethods}

trait DatabaseTestMethods extends QueryMethods {
  override protected def db: DB = KoskiApplicationForTests.masterDatabase.db
}
