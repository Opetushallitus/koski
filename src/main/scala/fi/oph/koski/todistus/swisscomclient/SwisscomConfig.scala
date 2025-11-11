package fi.oph.koski.todistus.swisscomclient

import com.typesafe.config.Config
import fi.oph.koski.config.{Environment, SecretsManager}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.{Logging, NotLoggable}
import fi.oph.koski.todistus.swisscomclient.SwisscomConfigSecretsSource.{FROM_SECRETS_MANAGER, FROM_SSO_SECRETS_MANAGER, MOCK_FROM_CONFIG, SwisscomConfigSecretsSource}
import org.json4s.jackson.JsonMethods.parse
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest

case class SwisscomConfig(
  configSource: SwisscomConfigSecretsSource,
  signUrl: String,
  keyStore: String,
  keyStorePassword: String,
  digestAlgorithm: String,
  digestUri: String,
  signaturePreferredSize: Int,
  signatureStandard: String,
  signatureRevocationInformation: String,
  signatureClaimedIdentityName: String,
  signatureClaimedIdentityKey: String,
  signatureDistinguishedName: String,
  signatureName: String,
  signatureReason: String,
  signatureLocation: String,
  signatureContactInfo: String
) extends NotLoggable

object SwisscomConfig extends Logging {

  def apply(config: Config): SwisscomConfig = {
    val secretsSource = if (Environment.usesAwsSecretsManager) {
      SwisscomConfigSecretsSource.FROM_SECRETS_MANAGER
    } else if (sys.env.contains("SWISSCOM_SECRET_ID")) {
      SwisscomConfigSecretsSource.FROM_SSO_SECRETS_MANAGER
    } else {
      SwisscomConfigSecretsSource.MOCK_FROM_CONFIG
    }
    secretsSource match {
      case FROM_SECRETS_MANAGER => fromSecretsManager(config, secretsSource)
      case FROM_SSO_SECRETS_MANAGER => fromSsoCredentialsSecretsManager(config, secretsSource)
      case MOCK_FROM_CONFIG => fromConfig(config, secretsSource)
    }
  }

  private def fromConfig(config: Config, secretsSource: SwisscomConfigSecretsSource): SwisscomConfig = {
    val swisscomConfig = config.getConfig("todistus.swisscom")
    val signatureConfig = swisscomConfig.getConfig("signature")

    SwisscomConfig(
      configSource = secretsSource,
      signUrl = swisscomConfig.getString("signUrl"),
      keyStore = "mock",
      keyStorePassword = "mock",
      digestAlgorithm = swisscomConfig.getString("digestAlgorithm"),
      digestUri = swisscomConfig.getString("digestUri"),
      signaturePreferredSize = signatureConfig.getInt("preferredSize"),
      signatureStandard = signatureConfig.getString("standard"),
      signatureRevocationInformation = signatureConfig.getString("revocationInformation"),
      signatureClaimedIdentityName = "mock",
      signatureClaimedIdentityKey = "mock",
      signatureDistinguishedName = "mock",
      signatureName = signatureConfig.getString("name"),
      signatureReason = signatureConfig.getString("reason"),
      signatureLocation = signatureConfig.getString("location"),
      signatureContactInfo = signatureConfig.getString("contactInfo")
    )
  }

  private def fromSecretsManager(config: Config, secretsSource: SwisscomConfigSecretsSource): SwisscomConfig = {
    val swisscomConfig = config.getConfig("todistus.swisscom")
    val signatureConfig = swisscomConfig.getConfig("signature")

    val cachedSecretsClient = new SecretsManager
    val secretId = cachedSecretsClient.getSecretId("Swisscom Secrets", "SWISSCOM_SECRET_ID")
    val secrets = cachedSecretsClient.getStructuredSecret[SwisscomSecretsConfig](secretId)

    fromSecrets(secretsSource, swisscomConfig, signatureConfig, secrets)
  }

  private def fromSsoCredentialsSecretsManager(config: Config, secretsSource: SwisscomConfigSecretsSource): SwisscomConfig = {
    val swisscomConfig = config.getConfig("todistus.swisscom")
    val signatureConfig = swisscomConfig.getConfig("signature")

    val secretId = sys.env.getOrElse("SWISSCOM_SECRET_ID", "swisscom-secrets")

    val ssoFromProfile =
      ProfileCredentialsProvider.builder()
        .profileName(sys.env.getOrElse("AWS_PROFILE", "oph-koski-dev"))
        .build()

    val smClient = SecretsManagerClient.builder()
      .region(Region.EU_WEST_1)
      .credentialsProvider(ssoFromProfile)
      .build()

    val req = GetSecretValueRequest.builder().secretId(secretId).build()
    val resp = smClient.getSecretValue(req)

    val secrets = JsonSerializer.extract[SwisscomSecretsConfig](parse(resp.secretString()), ignoreExtras = true)

    fromSecrets(secretsSource, swisscomConfig, signatureConfig, secrets)
  }

  private def fromSecrets(secretsSource: SwisscomConfigSecretsSource, swisscomConfig: Config, signatureConfig: Config, secrets: SwisscomSecretsConfig): SwisscomConfig = {
    SwisscomConfig(
      configSource = secretsSource,
      signUrl = swisscomConfig.getString("signUrl"),
      keyStore = secrets.keyStore,
      keyStorePassword = secrets.keyStorePassword,
      digestAlgorithm = swisscomConfig.getString("digestAlgorithm"),
      digestUri = swisscomConfig.getString("digestUri"),
      signaturePreferredSize = signatureConfig.getInt("preferredSize"),
      signatureStandard = signatureConfig.getString("standard"),
      signatureRevocationInformation = signatureConfig.getString("revocationInformation"),
      signatureClaimedIdentityName = secrets.signatureClaimedIdentityName,
      signatureClaimedIdentityKey = secrets.signatureClaimedIdentityKey,
      signatureDistinguishedName = secrets.signatureDistinguishedName,
      signatureName = signatureConfig.getString("name"),
      signatureReason = signatureConfig.getString("reason"),
      signatureLocation = signatureConfig.getString("location"),
      signatureContactInfo = signatureConfig.getString("contactInfo")
    )
  }
}

case class SwisscomSecretsConfig(
  keyStore: String,
  keyStorePassword: String,
  signatureClaimedIdentityName: String,
  signatureClaimedIdentityKey: String,
  signatureDistinguishedName: String,
) extends NotLoggable

object SwisscomConfigSecretsSource extends Enumeration {
  type SwisscomConfigSecretsSource = Value
  val FROM_SECRETS_MANAGER,
  FROM_SSO_SECRETS_MANAGER,
  MOCK_FROM_CONFIG = Value
}
