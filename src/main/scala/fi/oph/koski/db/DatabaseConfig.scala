package fi.oph.koski.db

import com.typesafe.config.Config
import com.typesafe.config.ConfigValueFactory._
import fi.oph.koski.config.{Environment, SecretsManager}
import fi.oph.koski.log.NotLoggable
import fi.oph.koski.raportointikanta.Schema
import slick.jdbc.PostgresProfile


class KoskiDatabaseConfig(val rootConfig: Config) extends DatabaseConfig {
  override val envVarForSecretId: String = "DB_KOSKI_SECRET_ID"

  override protected def databaseSpecificConfig: Config = rootConfig.getConfig("dbs.koski")
}

class KoskiReplicaConfig(val rootConfig: Config) extends DatabaseConfig {
  override val envVarForSecretId: String = "DB_KOSKI_SECRET_ID"

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

class RaportointiDatabaseConfig(val rootConfig: Config, val schema: Schema) extends DatabaseConfig {
  override val envVarForSecretId: String = "DB_RAPORTOINTI_SECRET_ID"

  override protected def databaseSpecificConfig: Config =
    rootConfig.getConfig("dbs.raportointi")
      .withValue("poolName", fromAnyRef(s"koskiRaportointiPool-${schema.name}"))
}

class ValpasDatabaseConfig(val rootConfig: Config) extends DatabaseConfig {
  override val envVarForSecretId: String = "DB_KOSKI_SECRET_ID" // Samalla instanssilla kuin pääkanta

  override protected def databaseSpecificConfig: Config = rootConfig.getConfig("dbs.valpas")
}

trait DatabaseConfig extends NotLoggable {
  protected val rootConfig: Config
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
        .withValue("name", fromAnyRef(secretConfig.dbname))
        .withValue("user", fromAnyRef(secretConfig.username))
        .withValue("password", fromAnyRef(secretConfig.password))
        .withValue("secretId", fromAnyRef(secretId))
    } else {
      config
    }
  }

  protected def makeConfig(): Config = {
    configWithSecrets(databaseSpecificConfig.withFallback(sharedConfig))
  }

  private final def configForSlick(): Config = {
    (if (useSecretsManager) {
      // Secrets Manager JDBC-ajuri haluaa käyttäjänimenä secret ID:n. Korvataan
      // konfiguraation käyttäjänimi vasta tässä vaiheessa, jotta konfiguraatio
      // toimii myös Flywayn käyttämän tavallisen JDBC-ajurin kanssa.
      config
        .withValue("user", config.getValue("secretId"))
        .withValue("driverClassName", fromAnyRef("com.amazonaws.secretsmanager.sql.AWSSecretsManagerPostgreSQLDriver"))
    } else {
      config
    }).withValue("url", fromAnyRef(url(useSecretsManagerProtocol = useSecretsManager)))
  }

  private final lazy val config: Config = makeConfig()

  final def host: String = config.getString("host")
  final def port: Int = config.getInt("port")
  final def dbname: String = config.getString("name")
  final def user: String = config.getString("user")
  final def password: String = config.getString("password")

  final def url(useSecretsManagerProtocol: Boolean): String = {
    val protocol = if (useSecretsManagerProtocol) {
      "jdbc-secretsmanager:postgresql"
    } else {
      "jdbc:postgresql"
    }
    s"$protocol://${host}:${port}/${dbname}"
  }

  final def isLocal: Boolean = host == "localhost" && !useSecretsManager

  final def toSlickDatabase: DB = PostgresProfile.api.Database.forConfig("", configForSlick())
}
