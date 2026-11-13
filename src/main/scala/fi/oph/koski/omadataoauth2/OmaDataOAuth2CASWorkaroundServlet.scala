package fi.oph.koski.omadataoauth2

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.frontendvalvonta.FrontendValvontaMode
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}

// Workaround: CAS-oppija ei päästä paluuosoitteessa olevia query-parametreja läpi. Ne on siksi base64url-enkoodattu path-parametriksi.
class OmaDataOAuth2CASWorkaroundServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with OmaDataOAuth2ServletSupport with Unauthenticated with NoCache
{
 val allowFrameAncestors: Boolean = !Environment.isCloudEnvironment(application.config)
  val frontendValvontaMode: FrontendValvontaMode.FrontendValvontaMode =
    FrontendValvontaMode(application.config.getString("frontend-valvonta.mode"))

  get("/authorize/:base64UrlEnkoodattuPaluuosoitteenParametrilista") {
    val decodedParameters = base64UrlDecode(params("base64UrlEnkoodattuPaluuosoitteenParametrilista"))
    val decodedUrl = s"/koski/omadata-oauth2/authorize?${decodedParameters}"

    redirect(decodedUrl)
  }

  get("/post-response/:base64UrlEnkoodattuPaluuosoitteenParametrilista") {
    val decodedParameters = base64UrlDecode(params("base64UrlEnkoodattuPaluuosoitteenParametrilista"))
    val decodedUrl = s"/koski/omadata-oauth2/post-response?${decodedParameters}"

    redirect(decodedUrl)
  }
}
