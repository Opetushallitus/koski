package fi.oph.koski.huoltaja

import com.typesafe.config.Config
import fi.oph.koski.config.Environment
import fi.oph.koski.log.Logging

import java.io.ByteArrayInputStream
import java.security.{KeyStore, SecureRandom}
import java.util.Base64
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

// ---------------------------
// 1. VtjClientBuilder
// ---------------------------
object VtjClientBuilder extends Logging {
  import cats.effect.unsafe.implicits.global
  def build(config: Config): VtjClient = {
    val runtimeCfg =
      if (Environment.usesAwsSecretsManager)
        VtjRuntimeConfig.fromAws()
      else
        VtjRuntimeConfig.fromAppConfig(config)

    val sslContext = VtjSsl.create(
      runtimeCfg.keystoreBytes,
      runtimeCfg.truststoreBytes,
      runtimeCfg.keystorePassword,
      runtimeCfg.truststorePassword
    )

    val httpClient = new VtjHttpClient(runtimeCfg.serviceUrl, sslContext)
    logger.info(s"Initialized VtjClient for ${runtimeCfg.serviceUrl}")

    new VtjClient(runtimeCfg, httpClient)
  }
}

// ---------------------------
// 2. VtjRuntimeConfig
// ---------------------------
object VtjRuntimeConfig {
  def fromAws(
    configSecretId: String = "vtj-kysely-config",
    keystoreSecretId: String = "vtj-kysely-keystore",
    truststoreSecretId: String = "vtj-kysely-truststore"
  ): VtjRuntimeConfig = {
    val cfg = VtjConfig.fromSecretsManager(configSecretId)
    val ks  = VtjKeystore.fromSecretsManager(keystoreSecretId)
    val ts  = VtjTruststore.fromSecretsManager(truststoreSecretId)

    VtjRuntimeConfig(
      cfg.serviceUrl, cfg.username, cfg.password,
      cfg.keystorePassword, cfg.truststorePassword,
      Base64.getDecoder.decode(ks.keystore),
      Base64.getDecoder.decode(ts.truststore)
    )
  }

  def fromAppConfig(appConfig: Config): VtjRuntimeConfig = {
    val cfg = VtjConfig.fromAppConfig(appConfig)
    val ks  = VtjKeystore.fromAppConfig(appConfig)
    val ts  = VtjTruststore.fromAppConfig(appConfig)
    VtjRuntimeConfig(
      cfg.serviceUrl, cfg.username, cfg.password,
      cfg.keystorePassword, cfg.truststorePassword,
      Base64.getDecoder.decode(ks.keystore),
      Base64.getDecoder.decode(ts.truststore)
    )
  }
}

case class VtjRuntimeConfig(
  serviceUrl: String,
  username: String,
  password: String,
  keystorePassword: String,
  truststorePassword: String,
  keystoreBytes: Array[Byte],
  truststoreBytes: Array[Byte]
)

// ---------------------------
// 3. VtjSsl
// ---------------------------
class VtjSslException(message: String) extends Exception(message)

object VtjSsl extends Logging {

  def create(
    keystoreBytes: Array[Byte],
    truststoreBytes: Array[Byte],
    keystorePass: String,
    truststorePass: String
  ): SSLContext = {

    def loadStore(storeBytes: Array[Byte], password: String, store: KeyStore): Unit = {
      val in = new ByteArrayInputStream(storeBytes)
      try store.load(in, password.toCharArray)
      finally in.close()
    }

    try {
      val keystore = KeyStore.getInstance("PKCS12")
      loadStore(keystoreBytes, keystorePass, keystore)

      val kmf = KeyManagerFactory.getInstance("SunX509")
      kmf.init(keystore, keystorePass.toCharArray)

      val truststore = KeyStore.getInstance("JKS")
      loadStore(truststoreBytes, truststorePass, truststore)

      val tmf = TrustManagerFactory.getInstance("SunX509")
      tmf.init(truststore)

      val sslContext = SSLContext.getInstance("TLS")
      sslContext.init(kmf.getKeyManagers, tmf.getTrustManagers, new SecureRandom())
      sslContext
    } catch {
      case e: Exception =>
        logger.error(e)("Error setting up SSL context")
        throw new VtjSslException("SSL context setup failed")
    }
  }
}
