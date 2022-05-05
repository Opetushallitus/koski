package fi.oph.koski.servlet

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.frontendvalvonta.FrontendValvontaMode
import org.scalatra.ScalatraServlet

class VirhesivuServlet(implicit val application: KoskiApplication) extends ScalatraServlet with OppijaHtmlServlet with OmaOpintopolkuSupport {

  val allowFrameAncestors: Boolean = !Environment.isServerEnvironment(application.config)
  val frontendValvontaMode: FrontendValvontaMode.FrontendValvontaMode =
    FrontendValvontaMode(application.config.getString("frontend-valvonta.mode"))

  get("/")(nonce => {
    response.setHeader("X-Virhesivu", "1") // for korhopankki/HetuLogin.jsx
    <html lang={lang}>
      <head>
        <title>Koski - Virhe</title>
        <link nonce="{nonce}" type="text/css" rel="stylesheet" href="/koski/css/virhesivu.css"/>
        {oppijaRaamit.script(nonce)}
      </head>
      <body>
        <div class="odottamaton-virhe">
          <h2>{t("Koski-järjestelmässä tapahtui virhe, ole hyvä ja yritä myöhemmin uudelleen")}</h2>
          <a href="/oma-opintopolku/">{t("Palaa etusivulle")}</a>
        </div>
      </body>
    </html>
  })
}
