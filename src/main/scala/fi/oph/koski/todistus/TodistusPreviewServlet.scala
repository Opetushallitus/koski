package fi.oph.koski.todistus

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.frontendvalvonta.FrontendValvontaMode
import fi.oph.koski.koskiuser.{HasKoskiSpecificSession, KoskiSpecificSession}
import fi.oph.koski.koskiuser.Rooli.OPHPAAKAYTTAJA
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.servlet.{NoCache, VirkailijaHtmlServlet}
import org.scalatra.ScalatraServlet

class TodistusPreviewServlet(implicit val application: KoskiApplication)
  extends ScalatraServlet
    with VirkailijaHtmlServlet
    with NoCache
    with HasKoskiSpecificSession
{

  val allowFrameAncestors: Boolean = !Environment.isServerEnvironment(application.config)

  val frontendValvontaMode: FrontendValvontaMode.FrontendValvontaMode =
    FrontendValvontaMode(application.config.getString("frontend-valvonta.mode"))

  override val unsafeAllowInlineStyles: Boolean = true

  implicit def session: KoskiSpecificSession = koskiSessionOption.get

  val service: TodistusService = application.todistusService

  override protected val virkailijaRaamitSet: Boolean = false

  before() {
    requireOphPääkäyttäjä
  }

  private def requireOphPääkäyttäjä: Unit = {
    if (!session.hasRole(OPHPAAKAYTTAJA)) {
      haltWithStatus(KoskiErrorCategory.forbidden("Sallittu vain OPH-pääkäyttäjälle"))
    }
  }

  get("/:lang/:opiskeluoikeusOid")(nonce => {
    contentType = "text/html"

    val result = for {
      req <- getTodistusGenerateRequest
      html <- service.generateHtmlPreview(req)
    } yield html

    result match {
      case Right(html) =>
        val os = response.getOutputStream
        os.write(html.getBytes)
      case Left(status) =>
        haltWithStatus(status)
    }
  })

  private def getTodistusGenerateRequest = {
    val lang = params("lang")
    val oid = params("opiskeluoikeusOid")

    if (!TodistusLanguage.*.contains(lang)) {
      Left(KoskiErrorCategory.badRequest(s"Virheellinen kieli: $lang. Sallitut arvot: ${TodistusLanguage.*.mkString(", ")}"))
    } else if (!fi.oph.koski.schema.Opiskeluoikeus.isValidOpiskeluoikeusOid(oid)) {
      Left(KoskiErrorCategory.badRequest(s"Virheellinen opiskeluoikeus OID: $oid"))
    } else {
      Right(TodistusGenerateRequest(
        opiskeluoikeusOid = oid,
        language = lang,
      ))
    }
  }
}
