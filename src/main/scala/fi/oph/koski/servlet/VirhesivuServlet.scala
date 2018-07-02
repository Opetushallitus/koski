package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import org.scalatra.ScalatraServlet

class VirhesivuServlet(implicit val application: KoskiApplication) extends ScalatraServlet with HtmlServlet with OmaOpintopolkuSupport {
  get("/") {
    response.setHeader("X-Virhesivu", "1") // for korhopankki/HetuLogin.jsx
    <html lang={lang}>
      <head>
        <title>Koski - Virhe</title>
        <link type="text/css" rel="stylesheet" href="/koski/css/virhesivu.css"/>
        {oppijaRaamit.script}
      </head>
      <body>
        <div class="odottamaton-virhe">
          <h2>{t("Koski-järjestelmässä tapahtui virhe, ole hyvä ja yritä myöhemmin uudelleen")}</h2>
          <a href="/oma-opintopolku/">{t("Palaa etusivulle")}</a>
        </div>
      </body>
    </html>
  }
}
