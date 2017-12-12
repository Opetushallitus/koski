package fi.oph.koski.config

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.util.{Files, Futures}
import org.postgresql.util.PSQLException
/**
  *  Detects things about the runtime environment to facilitate some safety checks.
  */
case class Environment(application: KoskiApplication) {
  def databaseIsLarge = Environment.databaseIsLarge(application.masterDatabase.db)

  def indexIsLarge = application.koskiPulssi.opiskeluoikeusTilasto.opiskeluoikeuksienMäärä > 100

  def isDevEnvironment = List("local", "OPHKoskiDev").contains(application.config.getString("env"))
}

object Environment {
  def isLocalDevelopmentEnvironment = Files.exists("Makefile")
  def databaseIsLarge(db: DB) = {
    try {
      val count = Futures.await(db.run(sql"select count(*) from opiskeluoikeus".as[Int]))(0)
      count > 100
    } catch {
      case e: PSQLException =>
        if (e.getMessage.contains("""relation "opiskeluoikeus" does not exist""")) {
          false // Allow for an uninitialized db
        } else {
          throw e
        }
    }
  }
}