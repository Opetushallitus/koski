package fi.oph.koski.ytr

import com.typesafe.config.Config
import fi.oph.koski.config.{Environment, SecretsManager}
import fi.oph.koski.http.Http._
import fi.oph.koski.http.{ClientWithBasicAuthentication, Http, HttpStatus}
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.json.{JsonResources, JsonSerializer}
import fi.oph.koski.log.{Logging, NotLoggable, TimedProxy}
import fi.oph.koski.util.{ClasspathResource, Resource, Streams}
import fi.oph.koski.ytr.download.{YtrLaajaOppija, YtrSsnData}
import org.json4s.JValue

import java.io.OutputStream
import java.time.{LocalDate, LocalDateTime}

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

  def getCertificateStatus(req: YoTodistusRequest): Either[HttpStatus, YtrCertificateResponse]
  def generateCertificate(req: YoTodistusRequest): Either[HttpStatus, YtrCertificateResponse]
  def getCertificate(todistus: YtrCertificateCompleted, outputStream: OutputStream): Unit
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

case class YoTodistusRequest(
  hetu: String,
  language: String,
)

object EmptyYtrClient extends YtrClient {
  override def oppijaJsonByHetu(hetu: String): None.type = None

  override def getJsonHetutBySyntymäaika(birthmonthStart: String, birthmonthEnd: String): Option[JValue] = None

  override def getJsonHetutByModifiedSince(modifiedSince: LocalDate): Option[JValue] = None

  override def oppijatJsonByHetut(ssnData: YtrSsnData): Option[JValue] = None

  override def getCertificateStatus(req: YoTodistusRequest): Either[HttpStatus, YtrCertificateResponse] = Right(YtrCertificateServiceUnavailable(LocalDateTime.now()))

  override def generateCertificate(req: YoTodistusRequest): Either[HttpStatus, YtrCertificateResponse] = Right(YtrCertificateServiceUnavailable(LocalDateTime.now()))

  override def getCertificate(todistus: YtrCertificateCompleted, outputStream: OutputStream): Unit = {}
}

object MockYrtClient extends YtrClient {
  lazy val resource: Resource = new ClasspathResource("/mockdata/yotodistus")
  val requested: collection.mutable.Map[String, LocalDateTime] = collection.mutable.Map.empty

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

  override def getCertificateStatus(req: YoTodistusRequest): Either[HttpStatus, YtrCertificateResponse] =
    requested.get(req.hetu) match {
      case None =>
        Right(YtrCertificateNotStarted())
      case Some(time) if LocalDateTime.now().isAfter(time.plusSeconds(5)) =>
        Right(YtrCertificateCompleted(
          requestedTime = time,
          completedTime = time.plusSeconds(5),
          certificateUrl = "link-to-download",
        ))
      case Some(time) =>
        Right(YtrCertificateInProgress(
          requestedTime = time,
        ))
    }

  override def generateCertificate(req: YoTodistusRequest): Either[HttpStatus, YtrCertificateResponse] = {
    requested += (req.hetu -> LocalDateTime.now())
    getCertificateStatus(req)
  }

  override def getCertificate(todistus: YtrCertificateCompleted, outputStream: OutputStream): Unit =
    resource.resourceSerializer("mock-yotodistus.pdf")(is => Streams.pipeTo(is, outputStream))
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
    runIO(http.post(uri"/api/oph-registrydata/students", ssnData)(json4sEncoderOf[YtrSsnData])(Http.parseJsonOptional[JValue]))
  }

  override def getCertificateStatus(req: YoTodistusRequest): Either[HttpStatus, YtrCertificateResponse] = ???

  override def generateCertificate(req: YoTodistusRequest): Either[HttpStatus, YtrCertificateResponse] = ???

  override def getCertificate(todistus: YtrCertificateCompleted, outputStream: OutputStream): Unit = ???
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
