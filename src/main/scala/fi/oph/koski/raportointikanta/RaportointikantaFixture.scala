package fi.oph.koski.raportointikanta

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.fixture.FixtureState
import fi.oph.koski.log.Logging

import scala.io.Source

class RaportointikantaFixture(raportointiDatabase: RaportointiDatabase, fixtureState: FixtureState) extends Logging with QueryMethods {
  val db: DB = raportointiDatabase.db
  val schemaName: String = s"fixture_${fixtureState.name}_${fixtureState.key}".toLowerCase
  protected lazy val setup: Int = runDbSync(sqlu"#${Source.fromResource("clone_schema.sql").mkString}")

  def exists: Boolean =
    runDbSync(sql"SELECT schema_name FROM information_schema.schemata WHERE schema_name = $schemaName".as[String]).nonEmpty

  def save(): Unit = {
    logger.info(s"Store raportointikanta fixture $schemaName")
    cloneSchema("public", schemaName)
  }

  def restore(): Unit = {
    logger.info(s"Load raportointikanta fixture $schemaName")
    cloneSchema(schemaName, "public")
  }

  protected def cloneSchema(source: String, target: String): Unit = {
    setup
    runDbSync(sql"SELECT clone_schema($source, $target, 'DATA')".as[String])
  }
}

class RaportointikantaFixturePurge(raportointiDatabase: RaportointiDatabase) extends Logging with QueryMethods {
  val db: DB = raportointiDatabase.db

  def run(): Unit = {
    val fixtureSchemas = runDbSync(sql"select schema_name from information_schema.schemata where schema_name like 'fixture_%'".as[String])
    if (fixtureSchemas.nonEmpty) {
      logger.info(s"Drop cached fixtures: ${fixtureSchemas.mkString(", ")}")
      fixtureSchemas.foreach(schema => runDbSync(sqlu"drop schema #$schema cascade"))
    }
  }
}
