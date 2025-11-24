package fi.oph.koski.todistus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.frontendvalvonta.FrontendValvontaMode
import fi.oph.koski.html.{EiRaameja, Raamit}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{KoskiCookieAndBasicAuthenticationSupport, KoskiSpecificSession}
import fi.oph.koski.servlet.{KoskiHtmlServlet, NoCache}

import scala.util.Using

class TodistusContentServlet(implicit val application: KoskiApplication)
  extends KoskiHtmlServlet
    with TodistusServlet
    with NoCache
    with KoskiCookieAndBasicAuthenticationSupport
{
  implicit def session: KoskiSpecificSession = koskiSessionOption.get

  protected val virkailijaRaamitSet: Boolean = false
  protected def virkailijaRaamit: Raamit = EiRaameja

  val allowFrameAncestors: Boolean = false
  val frontendValvontaMode: FrontendValvontaMode.FrontendValvontaMode = FrontendValvontaMode.ENABLED

  before() {
    requireKansalainenOrOphPääkäyttäjä
  }

  get("/preview/:lang/:opiskeluoikeusOid")(nonce => {
    requireOphPääkäyttäjä

    contentType = "text/html"

    val result = for {
      req <- getTodistusGenerateRequest
      result <- service.generateHtmlPreview(req)
    } yield result

    result match {
      case Right((html, dummyJob)) =>
        auditLogTodistusPreview(dummyJob)

        val os = response.getOutputStream
        os.write(html.getBytes)
        os.flush()
      case Left(status) =>
        renderStatus(status)
    }
  })

  get("/download/:id") { nonce =>
    val result = for {
      todistusJob <- validateCompletedTodistus
      stream <- service.getDownloadStream(BucketType.STAMPED, todistusJob)
    } yield {
      val filename = generateFilename(todistusJob)
      auditLogTodistusDownload(todistusJob)

      contentType = "application/pdf"
      response.setHeader("Content-Disposition", s"""attachment; filename="$filename"""")

      Using.resource(stream) { inputStream =>
        val outputStream = response.getOutputStream
        inputStream.transferTo(outputStream)
        outputStream.flush()
      }
    }

    result match {
      case Left(status) =>
        logger.error(s"Download failed for ${getOptionalStringParam("id").getOrElse("UNKNOWN")}: " + status.toString)
        renderStatus(status)
      case Right(_) =>
        // PDF has been written directly to output stream, no further rendering needed
    }
  }

  get("/download/presigned/:id") { nonce =>
    requireOphPääkäyttäjä

    val result = for {
      todistusJob <- validateCompletedTodistus
      filename = generateFilename(todistusJob)
      url <- service.getDownloadUrl(BucketType.STAMPED, filename, todistusJob)
    } yield {
      auditLogTodistusDownload(todistusJob)

      contentType = "application/pdf"
      redirect(url)
    }

    result match {
      case Left(status) =>
        logger.error(s"Download failed for ${getOptionalStringParam("id").getOrElse("UNKNOWN")}: " + status.toString)
        renderStatus(status)
      case Right(_) =>
        // Redirect has been issued, no further rendering needed
    }
  }
}
