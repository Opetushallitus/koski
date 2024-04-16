package fi.oph.koski.raportointikanta

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import org.postgresql.util.PSQLException
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class RaportointikantaDbSpec extends AnyFreeSpec with Matchers with RaportointikantaTestMethods with BeforeAndAfterAll {
  "Drop and create public schema" in {
    dropsAndCreatesSchemaObjects(mainRaportointiDb)
  }

  "Drop and create temp schema" in {
    dropsAndCreatesSchemaObjects(tempRaportointiDb)
  }

  "Moves schema" in {
    dropAndCreateSchema(mainRaportointiDb)
    dropsAndCreatesSchemaObjects(tempRaportointiDb)
    schemaExists(tempRaportointiDb)
    tempRaportointiDb.moveTo(Public)
    schemaIsEmpty(tempRaportointiDb)
    schemaExists(mainRaportointiDb)
  }

  "Moves confidental schema" in {
    dropAndCreateSchema(confidentalRaportointiDb)
    dropAndCreateSchema(tempConfidentalRaportointiDb)
    schemaExists(tempConfidentalRaportointiDb)
    tempConfidentalRaportointiDb.moveTo(Confidential)
    schemaIsEmpty(tempConfidentalRaportointiDb)
    schemaExists(confidentalRaportointiDb)
  }

  private def dropsAndCreatesSchemaObjects(db: RaportointiDatabase) = {
    dropAndCreateSchema(db)
    schemaExists(db)
    db.createOtherIndexes()
    db.createCustomFunctions
    db.createPrecomputedTables(KoskiApplicationForTests.valpasRajapäivätService)
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

  private def dropAndCreateSchema(db: RaportointiDatabase) {
    db.dropAndCreateObjects()
  }
}
