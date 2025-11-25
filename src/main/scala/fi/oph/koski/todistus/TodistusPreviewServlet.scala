package fi.oph.koski.todistus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.frontendvalvonta.FrontendValvontaMode
import fi.oph.koski.html.{EiRaameja, Raamit}
import fi.oph.koski.koskiuser.{KoskiCookieAndBasicAuthenticationSupport, KoskiSpecificSession}
import fi.oph.koski.servlet.{KoskiHtmlServlet, NoCache}

class TodistusPreviewServlet(implicit val application: KoskiApplication)
  extends KoskiHtmlServlet
    with TodistusServlet
    with NoCache
    with KoskiCookieAndBasicAuthenticationSupport
{
  implicit def session: KoskiSpecificSession = koskiSessionOption.get

  protected val virkailijaRaamitSet: Boolean = false
  protected def virkailijaRaamit: Raamit = EiRaameja

  val allowFrameAncestors: Boolean = false
  override val unsafeAllowInlineStyles: Boolean = true // HTML on täysin inline-sisältöä, mukaanlukien tyylit
  val frontendValvontaMode: FrontendValvontaMode.FrontendValvontaMode = FrontendValvontaMode.ENABLED

  before() {
    requireOphPääkäyttäjä
  }

  get("/:lang/:opiskeluoikeusOid")(nonce => {
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
}
