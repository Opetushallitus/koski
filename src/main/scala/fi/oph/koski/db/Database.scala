package fi.oph.koski.db

import fi.oph.koski.config.Environment
import fi.oph.koski.log.Logging

trait Database extends Logging {
  protected val config: DatabaseConfig

  val db: DB = config.toSlickDatabase

  val smallDatabaseMaxRows: Int

  protected val dbSizeQuery: DatabaseUtilQueries.SizeQuery

  val util = new DatabaseUtilQueries(db, dbSizeQuery, smallDatabaseMaxRows)

  if ((config.isLocal || Environment.isLocalDevelopmentEnvironment(config.rootConfig)) && util.databaseIsLarge) {
    // Prevent running migrations against a (large) remote database when running locally
    val forceMigration = Environment.forceLocalMigration.contains(config.host)
    if (forceMigration) {
      Migration.migrateSchema(config)
    } else {
      val hint = s"If you really want to run this migration set environment variable FORCE_LOCAL_MIGRATION=${config.host}"
      logger.error(s"Migration not allowed with a large database in local development environment. $hint")
    }
  } else {
    Migration.migrateSchema(config)
  }
}
