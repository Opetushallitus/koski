package fi.oph.koski.db

import com.typesafe.config.Config
import com.typesafe.config.ConfigValueFactory._
import fi.oph.koski.config.{Environment, FluentConfig}
import fi.oph.koski.log.{Logging, NotLoggable}
import fi.oph.koski.raportointikanta.Schema
import slick.jdbc.PostgresProfile

class KoskiDatabaseConfig(val rootConfig: Config) extends DatabaseConfig {
  override protected def databaseSpecificConfig: Config = rootConfig.getConfig("dbs.koski")

  override protected def iamHostEnvVar: Option[String] = Some("DB_KOSKI_HOST")

  override def migrationLocations: Option[String] = Some("db.migration")
}

class KoskiReplicaConfig(val rootConfig: Config) extends DatabaseConfig {
  override protected def databaseSpecificConfig: Config = rootConfig.getConfig("dbs.replica")

  override protected def iamHostEnvVar: Option[String] = Some("DB_KOSKI_REPLICA_HOST")
}

trait RaportointiDatabaseConfigBase extends DatabaseConfig {
  val rootConfig: Config
  val schema: Schema

  def withSchema(schema: Schema): RaportointiDatabaseConfigBase
}

class RaportointiDatabaseConfig(val rootConfig: Config, val schema: Schema) extends RaportointiDatabaseConfigBase {
  override protected def databaseSpecificConfig: Config =
    rootConfig.getConfig("dbs.raportointi")
      .withValue("poolName", fromAnyRef(s"koskiRaportointiPool-${schema.name}"))

  override protected def iamHostEnvVar: Option[String] = Some("DB_RAPORTOINTI_HOST")

  def withSchema(schema: Schema): RaportointiDatabaseConfig = new RaportointiDatabaseConfig(rootConfig, schema)
}

class RaportointiGenerointiDatabaseConfig(val rootConfig: Config, val schema: Schema) extends RaportointiDatabaseConfigBase {
  override protected def databaseSpecificConfig: Config =
    rootConfig.getConfig("dbs.raportointiGenerointi")
      .withValue("poolName", fromAnyRef(s"koskiRaportointiGenerointiPool-${schema.name}"))

  override protected def iamHostEnvVar: Option[String] = Some("DB_RAPORTOINTI_HOST")  // same RDS instance as raportointi

  def withSchema(schema: Schema): RaportointiGenerointiDatabaseConfig = new RaportointiGenerointiDatabaseConfig(rootConfig, schema)
}

class ValpasDatabaseConfig(val rootConfig: Config) extends DatabaseConfig {
  override protected def databaseSpecificConfig: Config = rootConfig.getConfig("dbs.valpas")

  override protected def iamHostEnvVar: Option[String] = Some("DB_KOSKI_HOST")  // same RDS instance as koski master

  override def migrationLocations: Option[String] = Some("valpas.migration")
}

trait DatabaseConfig extends NotLoggable with Logging {
  val rootConfig: Config

  // Reads USE_SECRETS_MANAGER env var (kept for infra compat, also gates non-DB callers).
  final val useIamAuth: Boolean = Environment.usesAwsSecretsManager

  protected def databaseSpecificConfig: Config

  // Env var that overrides the host when IAM auth is on. CDK populates these in server
  // environments (it knows the RDS endpoints). None means "no override" — local dev keeps
  // host = "localhost" from reference.conf since useIamAuth is false there anyway.
  protected def iamHostEnvVar: Option[String] = None

  private final def sharedConfig: Config = rootConfig.getConfig("db")

  private final def configWithTestDb(config: Config): Config = {
    if (Environment.isUnitTestEnvironment(rootConfig)) {
      val postfix = "_test"
      config.withValue("name", fromAnyRef(config.getString("name") + postfix))
    } else {
      config
    }
  }

  private final def configForIamAuth(config: Config): Config = {
    if (useIamAuth) {
      val host = iamHostEnvVar.flatMap(sys.env.get).getOrElse(config.getString("host"))
      config
        .withValue("host", fromAnyRef(host))
        .withValue("user", fromAnyRef("koski_iam"))
    } else {
      config
    }
  }

  protected def makeConfig(): Config = {
    databaseSpecificConfig
      .withFallback(sharedConfig)
      .andThen(configForIamAuth)
      .andThen(configWithTestDb)
  }

  private final def configForSlick(): Config = {
    if (useIamAuth) {
      config
        .withValue("driverClassName", fromAnyRef("software.amazon.jdbc.Driver"))
        .withValue("password", fromAnyRef(""))
        .withValue("url", fromAnyRef(url(useIamProtocol = true)))
    } else {
      config.withValue("url", fromAnyRef(url(useIamProtocol = false)))
    }
  }

  private final lazy val config: Config = makeConfig()

  final def host: String = config.getString("host")

  final def port: Int = config.getInt("port")

  final def dbname: String = config.getString("name")

  final def schemaName: String = config.getString("schemaName")

  final def user: String = config.getString("user")

  final def password: String = if (useIamAuth) "" else config.getString("password")

  final def url(useIamProtocol: Boolean): String = {
    if (useIamProtocol) {
      // Pair IAM auth with SSL in the URL so both Slick and Flyway see the params
      // (Flyway's setDataSource(url, user, password) overload doesn't accept Properties).
      // RDS IAM requires TLS; ssl=true&sslmode=require enforces it for any consumer of this URL.
      s"jdbc:aws-wrapper:postgresql://${host}:${port}/${dbname}?wrapperPlugins=iam&ssl=true&sslmode=require"
    } else {
      s"jdbc:postgresql://${host}:${port}/${dbname}"
    }
  }

  final def isLocal: Boolean = host == "localhost" && !useIamAuth

  final def toSlickDatabase: DB = {
    logger.info(s"Using database $dbname")
    PostgresProfile.api.Database.forConfig("", configForSlick())
  }

  def migrationLocations: Option[String] = None
}
