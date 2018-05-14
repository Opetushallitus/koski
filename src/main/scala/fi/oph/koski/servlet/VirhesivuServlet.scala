package fi.oph.koski.servlet

import fi.oph.koski.config.KoskiApplication
import org.scalatra.ScalatraServlet

class VirhesivuServlet(implicit val application: KoskiApplication) extends ScalatraServlet with HtmlServlet with OmaOpintopolkuSupport {
  get("/") {
    response.setHeader("X-Virhesivu", "1") // for korhopankki/HetuLogin.jsx
    <html>
      <head>
        <title>Koski - Virhe</title>
        <link type="text/css" rel="stylesheet" href="/koski/css/virhesivu.css"/>
        {oppijaRaamit.script}
      </head>
      <body>
        <div class="odottamaton-virhe">
          <h2>Koski-järjestelmässä tapahtui virhe, yritä myöhemmin uudelleen</h2>
          <a href="/koski/">Palaa etusivulle</a>
        </div>
      </body>
    </html>
  }
}
