package fi.oph.koski.ytr

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log.Logging
import fi.oph.koski.util.Streams
import fi.oph.koski.ytr.MockYtrClient.yoTodistusResource
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.services.s3.{S3Client, S3Utilities}
import software.amazon.awssdk.services.s3.model.{GetObjectRequest, GetObjectResponse}

import java.io.OutputStream
import java.net.URI

object YoTodistusService {
  def apply(application: KoskiApplication): YoTodistusService = {
    val s3config: YtrS3Config = YtrS3Config.getEnvironmentConfig(application)
    if (s3config.bucket == "mock") {
      new MockYoTodistusService(application)
    } else {
      new RemoteYoTodistusService(application, s3config)
    }
  }
}

abstract class YoTodistusService(application: KoskiApplication) {
  val client: YtrClient = application.ytrClient
  val henkilöRepository = application.henkilöRepository

  def currentStatus(req: YoTodistusOidRequest): Either[HttpStatus, YtrCertificateResponse] =
    toHetuReq(req).flatMap(client.getCertificateStatus)

  def initiateGenerating(req: YoTodistusOidRequest): Either[HttpStatus, Unit] =
    toHetuReq(req).flatMap(client.generateCertificate)

  def download(req: YtrCertificateCompleted, output: OutputStream): Unit

  def reset(): Unit = {}

  private def toHetuReq(req: YoTodistusOidRequest): Either[HttpStatus, YoTodistusHetuRequest] =
    henkilöRepository
      .findByOid(req.oid)
      .flatMap {
        case henkilö if henkilö.hetu.isDefined => Some(YoTodistusHetuRequest(ssn = henkilö.hetu.get, previousSsns = henkilö.vanhatHetut, language = req.language))
        case _ => None
      }
      .toRight(KoskiErrorCategory.notFound())
}

class MockYoTodistusService(application: KoskiApplication) extends YoTodistusService(application) {
  override def download(req: YtrCertificateCompleted, output: OutputStream): Unit =
    yoTodistusResource.resourceSerializer("mock-yotodistus.pdf")(input => Streams.pipeTo(input, output))

  override def reset(): Unit = MockYtrClient.reset()
}

class RemoteYoTodistusService(application: KoskiApplication, config: YtrS3Config) extends YoTodistusService(application) with Logging {
  private val s3 = new YtrS3(config)

  override def download(req: YtrCertificateCompleted, output: OutputStream): Unit =
    try {
      logger.info(s"getCertificate begin $req")
      s3.client.getObject(
        objectRequest(req.certificateUrl),
        ResponseTransformer.toOutputStream[GetObjectResponse](output),
      )
    } finally {
      logger.info(s"getCertificate end $req")
      output.flush()
      output.close()
    }

  private def objectRequest(url: String) = {
    val uri = S3Client.create.utilities.parseUri(URI.create(url))
    val bucket = uri.bucket().get()
    val key = uri.key().get()
    logger.info(s"getCertificate ${key} from bucket ${bucket} (url = $url)")
    GetObjectRequest.builder.bucket(bucket).key(key).build
  }
}
