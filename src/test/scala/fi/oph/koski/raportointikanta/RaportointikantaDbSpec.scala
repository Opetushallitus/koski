package fi.oph.koski.raportointikanta

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import org.postgresql.util.PSQLException
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class RaportointikantaDbSpec extends FreeSpec with Matchers with RaportointikantaTestMethods with BeforeAndAfterAll {
  private lazy val public = new RaportointiDatabase(KoskiApplicationForTests.raportointiConfig, Public)
  private lazy val temp = new RaportointiDatabase(KoskiApplicationForTests.raportointiConfig, Temp)

  override protected def beforeAll(): Unit = startRaportointiDatabase

  "Drop and create public schema" in {
    dropsAndCreatesSchemaObjects(public)
  }

  "Drop and create temp schema" in {
    dropsAndCreatesSchemaObjects(temp)
  }

  "Moves schema" in {
    dropAll(public)
    schemaIsEmpty(public)
    dropsAndCreatesSchemaObjects(temp)
    schemaExists(temp)
    temp.moveTo(Public)
    schemaIsEmpty(temp)
    schemaExists(public)
  }

  private def dropsAndCreatesSchemaObjects(db: RaportointiDatabase) = {
    dropAll(db)
    schemaIsEmpty(db)
    db.dropAndCreateObjects
    schemaExists(db)
  }

  private def schemaExists(db: RaportointiDatabase) {
    db.tables.map(_.baseTableRow.tableName).foreach { tableName =>
      db.runDbSync(sql" SELECT '#${db.schema.name}.#$tableName'::regclass".as[String]).head should endWith(tableName)
    }
  }

  private def schemaIsEmpty(db: RaportointiDatabase) {
    db.tables.map(_.baseTableRow.tableName).foreach { tableName =>
      val thrown = the[PSQLException] thrownBy db.runDbSync(sql" SELECT '#${db.schema.name}.#$tableName'::regclass".as[String])
      thrown.getMessage should include regex s""""${db.schema.name}(.$tableName)?" does not exist"""
    }
  }

  private def dropAll(db: RaportointiDatabase) {
    db.dropAndCreateObjects
    db.runDbSync(RaportointiDatabaseSchema.dropAllIfExists(db.schema))
  }
}
