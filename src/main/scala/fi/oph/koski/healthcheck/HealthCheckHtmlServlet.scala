package fi.oph.koski.healthcheck

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.html.ContentSecurityPolicy
import fi.oph.koski.servlet.VirkailijaHtmlServlet
import fi.oph.koski.util.Cryptographic

class HealthCheckHtmlServlet(implicit val application: KoskiApplication) extends VirkailijaHtmlServlet{
  get("/") {
    val nonce = Cryptographic.nonce

    val healthcheck = application.healthCheck.healthcheckWithExternalSystems
    val version = buildVersionProperties.map(_.getProperty("version", null)).filter(_ != null).getOrElse("local")
    val buildDate = buildVersionProperties.map(_.getProperty("buildDate", null)).filter(_ != null).getOrElse("")

    val style = <style nonce={nonce}>
      body {{
        text-align: center;
      }}
      .healthcheck-ok {{
        height: 100%;
        background: #528a1e;
        background: linear-gradient(to bottom, darkgreen 0%,green 100%);
        color: white;
        font-family: sans-serif;
      }}
      .healthcheck-ok-heading {{
        font-size: 20vw;
        margin-bottom: 4vw;
      }}
      .healthcheck-fail {{
        height: 100%;
        background: red;
      }}
      .healthcheck-fail-heading {{
        font-size: 20vw;
        margin-bottom: 0;
      }}
      .healthcheck-fail-errors {{
        font-size: 4vw;
        list-style: none;
        line-height: 5vw;
        padding-left: 0;
      }}
      .healthcheck-version {{
        font-size: 4vw;
        padding-bottom: 4vw;
      }}
    </style>

    val status = healthcheck.isOk match {
      case true =>
        <div class="healthcheck-ok">
          <h1 class="healthcheck-ok-heading">OK</h1>
          <div class="healthcheck-version">{version} — {buildDate}</div>
        </div>
      case false =>
        <div class="healthcheck-fail">
          <h1 class="healthcheck-fail-heading">FAIL</h1>
          <ul class="healthcheck-fail-errors" >
            {healthcheck.errors.map(_.key).distinct.map(e => <li>{e}</li>)}
          </ul>
          <div class="healthcheck-version">{version} — {buildDate}</div>
        </div>
    }
    <html lang={lang}>
      <head>
        {ContentSecurityPolicy.create(nonce)}
        {style}
      </head>
      {status}
    </html>
  }
}
