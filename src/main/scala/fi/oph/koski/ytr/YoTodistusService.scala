package fi.oph.koski.ytr

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}

import java.io.OutputStream

class YoTodistusService(application: KoskiApplication) {
  val client: YtrClient = application.ytrClient
  val henkilöRepository = application.henkilöRepository

  def currentStatus(req: YoTodistusOidRequest): Either[HttpStatus, YtrCertificateResponse] =
    toHetuReq(req).flatMap(client.getCertificateStatus)

  def initiateGenerating(req: YoTodistusOidRequest): Either[HttpStatus, YtrCertificateResponse] =
    for {
      hetuReq  <- toHetuReq(req)
      response <- client.getCertificateStatus(hetuReq) match {
                    case Right(_: YtrCertificateNotStarted) => generate(hetuReq)
                    case Right(_: YtrCertificateTimeout) => generate(hetuReq)
                    case s: Any => s
                  }
    } yield response

  def download(req: YtrCertificateCompleted, output: OutputStream): Unit =
    client.getCertificate(req, output)

  private def generate(req: YoTodistusHetuRequest): Either[HttpStatus, YtrCertificateResponse] =
    client.generateCertificate(req)

  private def toHetuReq(req: YoTodistusOidRequest): Either[HttpStatus, YoTodistusHetuRequest] =
    henkilöRepository
      .findByOid(req.oid)
      .flatMap(_.hetu)
      .map(hetu => YoTodistusHetuRequest(hetu = hetu, language = req.language))
      .toRight(KoskiErrorCategory.notFound())
}

