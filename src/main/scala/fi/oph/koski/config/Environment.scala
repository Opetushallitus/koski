package fi.oph.koski.config

import com.typesafe.config.{Config, ConfigException}

import java.time.{DateTimeException, LocalDate, ZonedDateTime}
import java.time.format.DateTimeParseException

object Environment {
  val Local = "local"
  val UnitTest = "unittest"

  def isUnitTestEnvironment(config: Config): Boolean = currentEnvironment(config) == UnitTest

  def isLocalDevelopmentEnvironment(config: Config): Boolean = currentEnvironment(config) == Local

  def isUsingLocalDevelopmentServices(app: KoskiApplication): Boolean =
    app.masterDatabase.isLocal && isMockEnvironment(app.config)

  def isMockEnvironment(config: Config): Boolean = {
    try {
      config.getString("opintopolku.virkailija.url") == "mock" && config.getString("login.security") == "mock"
    } catch {
      case _: ConfigException => true
    }
  }

  def isProdEnvironment(config: Config): Boolean =
    config.getString("opintopolku.virkailija.url") == "https://virkailija.opintopolku.fi"

  def isServerEnvironment(config: Config): Boolean = !Set(Local, UnitTest).contains(currentEnvironment(config))

  def usesAwsAppConfig: Boolean = {
    sys.env.getOrElse("CONFIG_SOURCE", "") == "appconfig"
  }

  def usesAwsSecretsManager: Boolean = {
    sys.env.getOrElse("USE_SECRETS_MANAGER", "") == "true"
  }

  def currentEnvironment(config: Config): String = config.getString("env")

  def skipFixtures: Boolean =
    sys.env.getOrElse("SKIP_FIXTURES", "") == "true"

  def forceLocalMigration: Option[String] =
    sys.env.get("FORCE_LOCAL_MIGRATION")

  def raportointikantaLoadDueTime: Option[ZonedDateTime] =
    sys.env.get("RAPORTOINTIKANTA_DUETIME").map(time => try {
      ZonedDateTime.parse(time)
    } catch {
      case e: DateTimeParseException => throw new RuntimeException(s"Invalid timestamp format in environment variable RAPORTOINTIKANTA_DUETIME (expected ISO datetime format): ${e.getMessage}")
    })

  def ytrDownloadConfig: YtrDownloadConfig = {
    YtrDownloadConfig(
      birthmonthStart = parseMonth("YTR_DOWNLOAD_BIRTHMONTH_START"),
      birthmonthEnd = parseMonth("YTR_DOWNLOAD_BIRTHMONTH_END"),
      modifiedSince = parseLocalDate("YTR_DOWNLOAD_MODIFIED_SINCE"),
      modifiedSinceLastRun = parseBoolean("YTR_DOWNLOAD_MODIFIED_SINCE_LAST_RUN"),
      force = getYtrForceMode
    )
  }

  private def parseMonth(environmentVariableName: String): Option[String] = {
    sys.env.get(environmentVariableName).map(yearAndMonth => try {
      LocalDate.parse(yearAndMonth + "-01")
      yearAndMonth
    } catch {
      case e: DateTimeException => throw new RuntimeException(s"Invalid month format in environment variable ${environmentVariableName} (expected ISO date format): ${e.getMessage}")
    })
  }

  private def parseLocalDate(environmentVariableName: String): Option[LocalDate] = {
    sys.env.get(environmentVariableName).map(date => try {
      LocalDate.parse(date)
    } catch {
      case e: DateTimeException => throw new RuntimeException(s"Invalid date format in environment variable ${environmentVariableName} (expected ISO date format): ${e.getMessage}")
    })
  }

  private def parseBoolean(environmentVariableName: String): Option[Boolean] = {
    try {
      sys.env.get(environmentVariableName).map(_.toBoolean)
    } catch {
      case e: Exception => throw new RuntimeException(s"Invalid format for environment variable ${environmentVariableName} (expected boolean): ${e.getMessage}")
    }
  }

  private def getYtrForceMode: Boolean = sys.env.get("YTR_DOWNLOAD_FORCE") match {
    case Some("true") => true
    case Some("false") => false
    case Some(s) => throw new RuntimeException(s"Odottamaton arvo muuttujalla YTR_DOWNLOAD_FORCE: ${s} (sallitut arvot: true, false)")
    case None => false
  }
}

case class YtrDownloadConfig(
  birthmonthStart: Option[String],
  birthmonthEnd: Option[String],
  modifiedSince: Option[LocalDate],
  modifiedSinceLastRun: Option[Boolean],
  force: Boolean
)
