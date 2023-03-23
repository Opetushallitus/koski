package fi.oph.koski.ytr

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.util.Streams
import fi.oph.koski.ytr.MockYrtClient.resource
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.services.s3.model.{GetObjectRequest, GetObjectResponse}

import java.io.OutputStream

object YoTodistusService {
  def apply(application: KoskiApplication): YoTodistusService = {
    val s3config: YtrS3Config = YtrS3SignedCertificateConfig.getEnvironmentConfig(application)
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

  def initiateGenerating(req: YoTodistusOidRequest): Either[HttpStatus, YtrCertificateResponse] =
    toHetuReq(req).flatMap(client.generateCertificate)

  def download(req: YtrCertificateCompleted, output: OutputStream): Unit

  def reset(): Unit = {}

  private def toHetuReq(req: YoTodistusOidRequest): Either[HttpStatus, YoTodistusHetuRequest] =
    henkilöRepository
      .findByOid(req.oid)
      .flatMap(_.hetu)
      .map(hetu => YoTodistusHetuRequest(ssn = hetu, language = req.language))
      .toRight(KoskiErrorCategory.notFound())
}

class MockYoTodistusService(application: KoskiApplication) extends YoTodistusService(application) {
  override def download(req: YtrCertificateCompleted, output: OutputStream): Unit =
    resource.resourceSerializer("mock-yotodistus.pdf")(input => Streams.pipeTo(input, output))

  override def reset(): Unit = MockYrtClient.reset()
}

class RemoteYoTodistusService(application: KoskiApplication, config: YtrS3Config) extends YoTodistusService(application) {
  private val s3 = new YtrS3(config)
  private val bucket = s3.bucket

  override def download(req: YtrCertificateCompleted, output: OutputStream): Unit =
    try {
      s3.client.getObject(
        objectRequest(req.certificateUrl),
        ResponseTransformer.toOutputStream[GetObjectResponse](output),
      )
    } finally {
      output.close()
    }

  private def objectRequest(key: String) = GetObjectRequest.builder.bucket(bucket).key(key).build
}
