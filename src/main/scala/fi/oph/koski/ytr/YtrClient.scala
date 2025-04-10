package fi.oph.koski.ytr

import com.typesafe.config.Config
import fi.oph.koski.config.{Environment, SecretsManager}
import fi.oph.koski.henkilo.Hetu
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.{ylioppilasLukiolainenMaksamatonSuoritus, ylioppilasLukiolainenRikki, ylioppilasLukiolainenTimeouttaava, ylioppilasLukiolainenVanhaSuoritus}
import fi.oph.koski.http.Http._
import fi.oph.koski.http._
import fi.oph.koski.json.Json4sHttp4s.json4sEncoderOf
import fi.oph.koski.json.{JsonResources, JsonSerializer}
import fi.oph.koski.log.{Logging, NotLoggable, TimedProxy}
import fi.oph.koski.schema.KoskiSchema.lenientDeserializationWithoutValidation
import fi.oph.koski.util.{ClasspathResource, Resource}
import fi.oph.koski.ytr.download.{YtrLaajaOppija, YtrSsnData, YtrSsnDataWithPreviousSsns}
import fi.oph.scalaschema.{ExtractionContext, SchemaValidatingExtractor}
import org.json4s.{JArray, JField, JObject, JString, JValue}

import java.time.{LocalDate, ZonedDateTime}
import scala.collection.mutable

trait YtrClient {
  def oppijaByHetu(ssn: YtrSsnWithPreviousSsns): Option[YtrOppija] = {
    oppijaJsonByHetu(ssn).map(JsonSerializer.extract[YtrOppija](_, ignoreExtras = true))
  }
  def oppijatByHetut(ssnData: YtrSsnDataWithPreviousSsns): List[YtrLaajaOppija] = oppijatJsonByHetut(ssnData).map(JsonSerializer.extract[List[YtrLaajaOppija]](_, ignoreExtras = true)).getOrElse(List.empty)

  def oppijaJsonByHetu(ssn: YtrSsnWithPreviousSsns): Option[JValue]
  protected def oppijatJsonByHetut(ssnData: YtrSsnDataWithPreviousSsns): Option[JValue]

  def getHetutBySyntymäaika(birthmonthStart: String, birthmonthEnd: String): Option[YtrSsnData] = getJsonHetutBySyntymäaika(birthmonthStart, birthmonthEnd).map(JsonSerializer.extract[YtrSsnData](_, ignoreExtras = true))
  protected def getJsonHetutBySyntymäaika(birthmonthStart: String, birthmonthEnd: String): Option[JValue]

  def getHetutByModifiedSince(modifiedSince: LocalDate): Option[YtrSsnData] = getJsonHetutByModifiedSince(modifiedSince).map(JsonSerializer.extract[YtrSsnData](_, ignoreExtras = true))
  protected def getJsonHetutByModifiedSince(modifiedSince: LocalDate): Option[JValue]

  def getCertificateStatus(req: YoTodistusHetuRequest): Either[HttpStatus, YtrCertificateResponse]
  def generateCertificate(req: YoTodistusHetuRequest): Either[HttpStatus, Unit]
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
          MockYtrClient
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

case class YoTodistusHetuRequest(
  ssn: String,
  previousSsns: List[String],
  language: String,
)

case class YoTodistusOidRequest(
  oid: String,
  language: String,
)

object EmptyYtrClient extends YtrClient {
  override def oppijaJsonByHetu(ssn: YtrSsnWithPreviousSsns): Option[JValue] = None

  override protected def getJsonHetutBySyntymäaika(birthmonthStart: String, birthmonthEnd: String): Option[JValue] = None

  override protected def getJsonHetutByModifiedSince(modifiedSince: LocalDate): Option[JValue] = None

  override protected def oppijatJsonByHetut(ssnData: YtrSsnDataWithPreviousSsns): Option[JValue] = None

  override def getCertificateStatus(req: YoTodistusHetuRequest): Either[HttpStatus, YtrCertificateResponse] = Right(YtrCertificateServiceUnavailable())

  override def generateCertificate(req: YoTodistusHetuRequest): Either[HttpStatus, Unit] = Right(Unit)
}

object MockYtrClient extends YtrClient {
  lazy val yoTodistusResource: Resource = new ClasspathResource("/mockdata/yotodistus")
  val yoTodistusRequestTimes: collection.mutable.Map[String, ZonedDateTime] = collection.mutable.Map.empty
  val yoTodistusGeneratingTimeSecs = 2
  private var failureHetu: Option[String] = None
  private var timeoutHetu: Option[String] = None


  private val mockOphRegistrydataHetus = List(
    "080380-2432",
    "140380-336X",
    "220680-7850",
    "240680-087S",
    "101097-6107",
    "300805A756F",
    "300805A847D",
    "060807A7787",
    "050122A673D",
    "300805A1918",
  )

  var latestOppijaJsonByHetu: Option[YtrSsnWithPreviousSsns] = None
  var latestOppijatJsonByHetut: Option[YtrSsnDataWithPreviousSsns] = None
  var latestCertificateRequest: Option[YoTodistusHetuRequest] = None

  private val oppijaVersions = new mutable.HashMap[String,Int]()

  def incrementOppijaVersion(hetu: String) = {
    oppijaVersions.put(hetu, oppijaVersions.getOrElse(hetu, 0) + 1)
  }

  override def oppijaJsonByHetu(ssn: YtrSsnWithPreviousSsns): Option[JValue] = {
    latestOppijaJsonByHetu = Some(ssn)
    val hetu = ssn.ssn
    if (timeoutHetu.contains(hetu)) {
      Thread.sleep(20000)
    }
    if (failureHetu.contains(hetu)) {
      throw new RuntimeException("Mocked failure on hetu " + hetu)
    } else {
      JsonResources.readResourceIfExists(resourcename(hetu))
    }
  }
  def filename(hetu: String): String = "src/main/resources" + resourcename(hetu)
  private def resourcename(hetu: String) = "/mockdata/ytr/oph-koski/" + hetu + ".json"

  override protected def getJsonHetutBySyntymäaika(birthmonthStart: String, birthmonthEnd: String): Option[JValue] = {
    Some(
      JObject(JField("ssns",
        JArray(
          mockOphRegistrydataHetus.filter(hetu => {
            val startDate = LocalDate.parse(birthmonthStart + "-01")
            val endDate = LocalDate.parse(birthmonthEnd + "-01")
            Hetu.toBirthday(hetu).exists(date => date.isAfter(startDate.minusDays(1)) && date.isBefore(endDate))
          }).map(JString)))))
  }

  private def ophRegistryHetuResorceName(hetu: String) = {
    val versionedHetu = oppijaVersions.get(hetu) match {
      case Some(ver) => hetu + "_" + ver
      case None => hetu
    }
    s"/mockdata/ytr/oph-registrydata/${versionedHetu}.json"
  }

  override protected def getJsonHetutByModifiedSince(modifiedSince: LocalDate): Option[JValue] =
    JsonResources.readResourceIfExists(hetutResourceName(modifiedSince.toString))

  private def hetutResourceName(modifiedSince: String) =
    s"/mockdata/ytr/modifiedSince/ssns_${modifiedSince}.json"

  override protected def oppijatJsonByHetut(ssnData: YtrSsnDataWithPreviousSsns): Option[JValue] = {
    latestOppijatJsonByHetut = Some(ssnData)
    Some(JArray(
      ssnData.ssns.getOrElse(List.empty).map(_.ssn).sorted.flatMap(hetu => JsonResources.readResourceIfExists(ophRegistryHetuResorceName(hetu)))
    ))
  }

  override def getCertificateStatus(req: YoTodistusHetuRequest): Either[HttpStatus, YtrCertificateResponse] = {
    latestCertificateRequest = Some(req)
    (req.ssn, yoTodistusRequestTimes.get(s"${req.ssn}_${req.language}")) match {
      case _ if req.ssn == ylioppilasLukiolainenRikki.hetu.get && req.language == "sv" =>
        Right(YtrCertificateServiceUnavailable())
      case (hetu, _) if hetu == ylioppilasLukiolainenMaksamatonSuoritus.hetu.get =>
        Right(YtrCertificateBlocked())
      case (hetu, time) if hetu == ylioppilasLukiolainenVanhaSuoritus.hetu.get =>
        Right(YtrCertificateOldExamination(requestedTime = time.getOrElse(ZonedDateTime.now())))
      case (_, None) =>
        Right(YtrCertificateNotStarted())
      case (_, Some(time)) if ZonedDateTime.now().isAfter(time.plusSeconds(yoTodistusGeneratingTimeSecs)) =>
        if (req.ssn == ylioppilasLukiolainenTimeouttaava.hetu.get) {
          Right(YtrCertificateTimeout(time))
        } else if (req.ssn == ylioppilasLukiolainenRikki.hetu.get) {
          req.language match {
            case "fi" => Right(YtrCertificateInternalError(time))
            case _ => Left(KoskiErrorCategory.internalError())
          }
        } else {
          Right(YtrCertificateCompleted(
            requestedTime = time,
            completionTime = time.plusSeconds(yoTodistusGeneratingTimeSecs),
            certificateUrl = "link-to-download",
          ))
        }
      case (_, Some(time)) =>
        Right(YtrCertificateInProgress(
          requestedTime = time,
        ))
    }
  }

  override def generateCertificate(req: YoTodistusHetuRequest): Either[HttpStatus, Unit] = {
    latestCertificateRequest = Some(req)
    yoTodistusRequestTimes += (s"${req.ssn}_${req.language}" -> ZonedDateTime.now())
    Right(())
  }

  def reset(): Unit = {
    yoTodistusRequestTimes.clear()
    failureHetu = None
    timeoutHetu = None
    oppijaVersions.clear()
  }

  def setFailureHetu(hetu: String): Unit = {
    failureHetu = Some(hetu)
  }
  def setTimeoutHetu(hetu: String): Unit = {
    timeoutHetu = Some(hetu)
  }
}


case class RemoteYtrClient(rootUrl: String, user: String, password: String) extends YtrClient with Logging {
  private val http = Http(rootUrl, ClientWithBasicAuthentication(
    Http.retryingClient("ytr"),
    username = user,
    password = password
  ))

  // Can be used in order to recover from temporary failures, since POST requests to students API are idempotent.
  val postRetryingHttp = Http(rootUrl, ClientWithBasicAuthentication(
    unsafeRetryingClient("ytrWithPostRetry"),
    username = user,
    password = password
  ))

  def oppijaJsonByHetu(ssn: YtrSsnWithPreviousSsns): Option[JValue] = {
    runIO(postRetryingHttp.post(uri"/api/oph-koski/student", ssn)(json4sEncoderOf[YtrSsnWithPreviousSsns])(Http.parseJsonOptional[JValue]))
  }

  override protected def getJsonHetutBySyntymäaika(birthmonthStart: String, birthmonthEnd: String): Option[JValue] = {
    runIO(http.get(uri"/api/oph-registrydata/ssns?birthmonthStart=${birthmonthStart}&birthmonthEnd=${birthmonthEnd}")(Http.parseJsonOptional[JValue]))
  }

  override protected def getJsonHetutByModifiedSince(modifiedSince: LocalDate): Option[JValue] = {
    runIO(http.get(uri"/api/oph-registrydata/ssns?modifiedSince=${modifiedSince.toString}")(Http.parseJsonOptional[JValue]))
  }

  override protected def oppijatJsonByHetut(ssnData: YtrSsnDataWithPreviousSsns): Option[JValue] = {
    runIO(postRetryingHttp.post(uri"/api/oph-registrydata/students", ssnData)(json4sEncoderOf[YtrSsnDataWithPreviousSsns])(Http.parseJsonOptional[JValue]))
  }

  override def getCertificateStatus(req: YoTodistusHetuRequest): Either[HttpStatus, YtrCertificateResponse] = {
    val uri = uri"/api/oph-koski/signed-certificate/status"
    val json = runIO(postRetryingHttp.post(uri, req)(json4sEncoderOf[YoTodistusHetuRequest])(Http.parseJson[JValue]))
    val response = SchemaValidatingExtractor.extract[YtrCertificateResponse](json)
    response.left.map(e => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(e)))
  }

  override def generateCertificate(req: YoTodistusHetuRequest): Either[HttpStatus, Unit] = {
    val uri = uri"/api/oph-koski/signed-certificate"
    runIO(postRetryingHttp.post(uri, req)(json4sEncoderOf[YoTodistusHetuRequest]) {
      case (status, _, _) if status < 300 => Right(())
      case (404, _, _) => Left(KoskiErrorCategory.notFound.oppijaaEiLöydy())
      case (400, _, _) =>Left(KoskiErrorCategory.badRequest())
      case (status, text, _) =>
        logger.error(s"Odottamaton virhe digitaalinen yo-todistus-apista: $status $text")
        Left(KoskiErrorCategory.internalError())
    })
  }

  protected implicit val deserializationContext: ExtractionContext = lenientDeserializationWithoutValidation
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

case class YtrSsnWithPreviousSsns(
  ssn: String,
  previousSsns: List[String] = List.empty
) {
  def containsOnlyValidSsns(hetu: Hetu): Boolean = {
    val allSsns = List(ssn) ++ previousSsns
    !allSsns.exists(ssn => hetu.validate(ssn).isLeft)
  }
}
