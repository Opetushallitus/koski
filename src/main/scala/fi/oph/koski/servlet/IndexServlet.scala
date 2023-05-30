package fi.oph.koski.servlet

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.frontendvalvonta.FrontendValvontaMode
import fi.oph.koski.util.JsStringInterpolation.setWindowVar
import org.scalatra.ScalatraServlet

class IndexServlet(implicit val application: KoskiApplication) extends ScalatraServlet with VirkailijaHtmlServlet with OmaOpintopolkuSupport {

  val allowFrameAncestors: Boolean = !Environment.isServerEnvironment(application.config)

  // Salli framet, koska Kosken raporttien Excel-tiedostojen lataaminen käyttää niitä
  override val allowFrameSrcSelf: Boolean = true

  val frontendValvontaMode: FrontendValvontaMode.FrontendValvontaMode =
    FrontendValvontaMode(application.config.getString("frontend-valvonta.mode"))

  before("/.+".r) {
    if (!isAuthenticated) {
      redirectToVirkailijaLogin
    }
  }

  get("/")(nonce => {
    if (!isAuthenticated) {
      subdomainRedirectNeeded match {
        case Some(url) => redirect(url)
        case None =>
          setLangCookieFromDomainIfNecessary
          landerHtml(nonce)
      }
    } else {
      val url = if (koskiSessionOption.exists(_.user.kansalainen)) {
        "/koski/omattiedot"
      } else {
        "/koski/virkailija"
      }
      redirect(url)
    }
  })

  get("/virkailija/?")(nonce => {
    if (koskiSessionOption.exists(_.hasKelaAccess)) {
      redirect("/koski/kela")
    } else {
      indexHtml(nonce)
    }
  })

  get("/validointi")(indexHtml)

  get("/uusioppija")(indexHtml)

  get("/oppija/:oid")(indexHtml)

  get("/tiedonsiirrot*")(indexHtml)

  get("/raportit*")(indexHtml)

  get("/kela*")(indexHtml)

  private def indexHtml(nonce: String) =
    htmlIndex("koski-main.js", raamit = virkailijaRaamit, nonce = nonce)

  private def landerHtml(nonce: String) = {
    htmlIndex(
      scriptBundleName = "koski-lander.js",
      raamit = oppijaRaamit,
      scripts = <script nonce={nonce} id="auth">
        {setWindowVar("kansalaisenAuthUrl", "/koski/login/oppija")}
      </script>,
      responsive = true,
      nonce = nonce
    )
  }
}
