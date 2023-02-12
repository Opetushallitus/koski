package fi.oph.koski.ytr

import com.typesafe.config.Config
import fi.oph.koski.config.{Environment, SecretsManager}
import fi.oph.koski.http.Http._
import fi.oph.koski.http.{ClientWithBasicAuthentication, Http}
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.json.{JsonResources, JsonSerializer}
import fi.oph.koski.log.{Logging, NotLoggable, TimedProxy}
import fi.oph.koski.ytr.download.{YtrLaajaOppija, YtrSsnData}
import org.json4s.JValue

import java.time.LocalDate

trait YtrClient {
  // Rajapinnat, joilla haetaan kaikki YTL:n datat.
  def oppijaByHetu(hetu: String): Option[YtrOppija] = oppijaJsonByHetu(hetu).map(JsonSerializer.extract[YtrOppija](_, ignoreExtras = true))
  def oppijatByHetut(ssnData: YtrSsnData): List[YtrLaajaOppija] = oppijatJsonByHetut(ssnData).map(JsonSerializer.extract[List[YtrLaajaOppija]](_, ignoreExtras = true)).getOrElse(List.empty)

  def oppijaJsonByHetu(hetu: String): Option[JValue]
  def oppijatJsonByHetut(ssnData: YtrSsnData): Option[JValue]

  def getHetutBySyntymäaika(birthmonthStart: String, birthmonthEnd: String): Option[YtrSsnData] = getJsonHetutBySyntymäaika(birthmonthStart, birthmonthEnd).map(JsonSerializer.extract[YtrSsnData](_, ignoreExtras = true))
  def getJsonHetutBySyntymäaika(birthmonthStart: String, birthmonthEnd: String): Option[JValue]

  def getHetutByModifiedSince(modifiedSince: LocalDate): Option[YtrSsnData] = getJsonHetutByModifiedSince(modifiedSince).map(JsonSerializer.extract[YtrSsnData](_, ignoreExtras = true))
  def getJsonHetutByModifiedSince(modifiedSince: LocalDate): Option[JValue]
}

object YtrClient extends Logging {
    def apply(config: Config): YtrClient = {
      val url = {
        if (Environment.usesAwsSecretsManager) {
          YtrConfig.fromSecretsManager.url
        } else {
          config.getString("ytr.url")
        }
      }
      url match {
        case "mock" =>
          logger.info("Using mock YTR integration")
          MockYrtClient
        case "" =>
          logger.info("YTR integration disabled")
          EmptyYtrClient
        case _ =>
          val ytrConfig = if (Environment.usesAwsSecretsManager) YtrConfig.fromSecretsManager else YtrConfig.fromConfig(config)
          logger.info(s"Using YTR integration endpoint $url")
          TimedProxy[YtrClient](RemoteYtrClient(ytrConfig.url, ytrConfig.username, ytrConfig.password))
      }
    }
}

object EmptyYtrClient extends YtrClient {
  override def oppijaJsonByHetu(hetu: String): None.type = None

  override def getJsonHetutBySyntymäaika(birthmonthStart: String, birthmonthEnd: String): Option[JValue] = None

  override def getJsonHetutByModifiedSince(modifiedSince: LocalDate): Option[JValue] = None

  override def oppijatJsonByHetut(ssnData: YtrSsnData): Option[JValue] = None
}

object MockYrtClient extends YtrClient {
  def oppijaJsonByHetu(hetu: String): Option[JValue] = JsonResources.readResourceIfExists(resourcename(hetu))
  def filename(hetu: String): String = "src/main/resources" + resourcename(hetu)
  private def resourcename(hetu: String) = "/mockdata/ytr/" + hetu + ".json"

  override def getJsonHetutBySyntymäaika(birthmonthStart: String, birthmonthEnd: String): Option[JValue] =
    JsonResources.readResourceIfExists(hetutResourceName(birthmonthStart, birthmonthEnd))
  private def hetutResourceName(birthmonthStart: String, birthmonthEnd: String) =
    s"/mockdata/ytr/ssns_${birthmonthStart}_${birthmonthEnd}.json"

  override def getJsonHetutByModifiedSince(modifiedSince: LocalDate): Option[JValue] =
    JsonResources.readResourceIfExists(hetutResourceName(modifiedSince.toString))
  private def hetutResourceName(modifiedSince: String) =
    s"/mockdata/ytr/ssns_${modifiedSince}.json"

  override def oppijatJsonByHetut(ssnData: YtrSsnData): Option[JValue] =
    JsonResources.readResourceIfExists(
      resourcenameLaaja(ssnData.ssns.getOrElse(List.empty).mkString("_"))
    )
  private def resourcenameLaaja(hetut: String) = "/mockdata/ytr/laaja_" + hetut + ".json"
}

case class RemoteYtrClient(rootUrl: String, user: String, password: String) extends YtrClient with Logging {
  private val http = Http(rootUrl, ClientWithBasicAuthentication(
    Http.retryingClient("ytr"),
    username = user,
    password = password
  ))

  def oppijaJsonByHetu(hetu: String): Option[JValue] = {
    runIO(http.get(uri"/api/oph-koski/student/$hetu")(Http.parseJsonOptional[JValue]))
  }

  override def getJsonHetutBySyntymäaika(birthmonthStart: String, birthmonthEnd: String): Option[JValue] = {
    runIO(http.get(uri"/api/oph-registrydata/ssns?birthmonthStart=${birthmonthStart}&birthmonthEnd=${birthmonthEnd}")(Http.parseJsonOptional[JValue]))
  }

  override def getJsonHetutByModifiedSince(modifiedSince: LocalDate): Option[JValue] = {
    runIO(http.get(uri"/api/oph-registrydata/ssns?modifiedSince=${modifiedSince.toString}")(Http.parseJsonOptional[JValue]))
  }

  override def oppijatJsonByHetut(ssnData: YtrSsnData): Option[JValue] = {
    // TODO: TOR-1639 Tuleekohan tästä ongelma, että iso taulukollinen käsitellään kokonaisena JValue:na? Olisiko parempi deserialisoida lennossa striimiä luettaessa? Käytännössä voidaan kyllä myös varmaan säätää batchin koko tarpeeksi pieneksi?
    runIO(http.post(uri"/api/oph-registrydata/students", ssnData)(json4sEncoderOf[YtrSsnData])(Http.parseJsonOptional[JValue]))
  }
}

case class YtrConfig(username: String, password: String, url: String) extends NotLoggable

object YtrConfig {
  def fromConfig(config: Config): YtrConfig = YtrConfig(
    config.getString("ytr.username"),
    config.getString("ytr.password"),
    config.getString("ytr.url")
  )
  def fromSecretsManager: YtrConfig = {
    val cachedSecretsClient = new SecretsManager
    val secretId = cachedSecretsClient.getSecretId("YTR secrets", "YTR_SECRET_ID")
    cachedSecretsClient.getStructuredSecret[YtrConfig](secretId)
  }
}
