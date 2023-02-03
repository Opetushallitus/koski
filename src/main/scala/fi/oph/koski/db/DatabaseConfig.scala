package fi.oph.koski.db

import com.typesafe.config.Config
import com.typesafe.config.ConfigValueFactory._
import fi.oph.koski.config.{Environment, FluentConfig, SecretsManager}
import fi.oph.koski.log.{Logging, NotLoggable}
import fi.oph.koski.raportointikanta.Schema
import slick.jdbc.PostgresProfile

import java.util.Properties


object DatabaseConfig {
  val EnvVarForKoskiDbSecret = "DB_KOSKI_SECRET_ID"
  val EnvVarForRaportointiDbSecret = "DB_RAPORTOINTI_SECRET_ID"
}

class KoskiDatabaseConfig(val rootConfig: Config) extends DatabaseConfig {
  override val envVarForSecretId: String = DatabaseConfig.EnvVarForKoskiDbSecret

  override protected def databaseSpecificConfig: Config = rootConfig.getConfig("dbs.koski")

  override def migrationLocations: Option[String] = Some("db.migration")
}

class KoskiReplicaConfig(val rootConfig: Config) extends DatabaseConfig {
  override val envVarForSecretId: String = DatabaseConfig.EnvVarForKoskiDbSecret

  override protected def databaseSpecificConfig: Config = rootConfig.getConfig("dbs.replica")

  override protected def makeConfig(): Config = {
    if (useSecretsManager) {
      val host = sys.env.getOrElse(
        "DB_KOSKI_REPLICA_HOST",
        throw new RuntimeException("Secrets manager enabled for DB secrets but environment variable DB_KOSKI_REPLICA_HOST not set")
      )
      super.makeConfig().withValue("host", fromAnyRef(host))
    } else {
      super.makeConfig()
    }
  }
}

trait RaportointiDatabaseConfigBase extends DatabaseConfig {
  val rootConfig: Config
  val schema: Schema
}

class RaportointiDatabaseConfig(val rootConfig: Config, val schema: Schema) extends RaportointiDatabaseConfigBase {
  override val envVarForSecretId: String = DatabaseConfig.EnvVarForRaportointiDbSecret

  override protected def databaseSpecificConfig: Config =
    rootConfig.getConfig("dbs.raportointi")
      .withValue("poolName", fromAnyRef(s"koskiRaportointiPool-${schema.name}"))
}

class RaportointiGenerointiDatabaseConfig(val rootConfig: Config, val schema: Schema) extends RaportointiDatabaseConfigBase {
  override val envVarForSecretId: String = DatabaseConfig.EnvVarForRaportointiDbSecret

  override protected def databaseSpecificConfig: Config =
    rootConfig.getConfig("dbs.raportointiGenerointi")
      .withValue("poolName", fromAnyRef(s"koskiRaportointiGenerointiPool-${schema.name}"))
}

class ValpasDatabaseConfig(val rootConfig: Config) extends DatabaseConfig {
  override val envVarForSecretId: String = DatabaseConfig.EnvVarForKoskiDbSecret // Samalla instanssilla kuin pääkanta

  override protected def databaseSpecificConfig: Config = rootConfig.getConfig("dbs.valpas")

  override def migrationLocations: Option[String] = Some("valpas.migration")
}

trait DatabaseConfig extends NotLoggable with Logging {
  val rootConfig: Config

  protected val envVarForSecretId: String

  protected final val useSecretsManager: Boolean = Environment.usesAwsSecretsManager

  protected def databaseSpecificConfig: Config

  private final def sharedConfig: Config = rootConfig.getConfig("db")

  private final def configWithSecrets(config: Config): Config = {
    if (useSecretsManager) {
      val secretsClient = new SecretsManager()
      val secretId = secretsClient.getSecretId("Koski DB secrets", envVarForSecretId)
      val secretConfig = secretsClient.getDatabaseSecret(secretId)
      config
        .withValue("host", fromAnyRef(secretConfig.host))
        .withValue("port", fromAnyRef(secretConfig.port))
        .withValue("user", fromAnyRef(secretConfig.username))
        // .withValue("dataSourceClassName", fromAnyRef("fi.oph.koski.db.DataSourceWithRdsIamSupport"))
        // .withValue("wrapperPlugins", fromAnyRef("iam"))
        .withValue("secretId", fromAnyRef(secretId))
    } else {
      config
    }
  }

  private final def configWithTestDb(config: Config): Config = {
    if (Environment.isUnitTestEnvironment(rootConfig)) {
      val postfix = "_test"
      config.withValue("name", fromAnyRef(config.getString("name") + postfix))
    } else {
      config
    }
  }

  private final def configWithAWSIAM(config: Config): Config = {
    if (useSecretsManager) {
      config
      .withValue("dataSourceClassName", fromAnyRef("fi.oph.koski.db.DataSourceWithRdsIamSupport"))
      .withValue("wrapperPlugins", fromAnyRef("iam"))
    } else {
      // config
      config
        .withValue("dataSourceClassName", fromAnyRef("fi.oph.koski.db.DataSourceWithRdsIamSupport"))
        .withValue("wrapperPlugins", fromAnyRef("iam"))
    }
  }

  protected def makeConfig(): Config = {
    databaseSpecificConfig
      .withFallback(sharedConfig)
      .andThen(configWithSecrets)
      .andThen(configWithTestDb)
      .andThen(configWithAWSIAM)
  }

  private final def configForSlick(): Config = {
    (if (useSecretsManager) {
      config
        .withValue("user", config.getValue("secretId"))
    } else {
      config
    }).withValue("url", fromAnyRef(url(useSecretsManagerProtocol = useSecretsManager)))
  }

  private final lazy val config: Config = makeConfig()

  final def host: String = config.getString("host")

  final def port: Int = config.getInt("port")

  final def dbname: String = config.getString("name")

  final def schemaName: String = config.getString("schemaName")

  final def user: String = config.getString("user")

  final def password: String = config.getString("password")

  final def url(useSecretsManagerProtocol: Boolean): String = {
    val protocol = if (useSecretsManagerProtocol) {
      "jdbc:aws-wrapper:postgresql"
    } else {
      "jdbc:postgresql"
    }
    s"$protocol://${host}:${port}/${dbname}"
  }

  final def isLocal: Boolean = host == "localhost" && !useSecretsManager

  final def toSlickDatabase: DB = {
    logger.info(s"Using database $dbname")
    PostgresProfile.api.Database.forConfig("", configForSlick())
  }

  def migrationLocations: Option[String] = None
}
