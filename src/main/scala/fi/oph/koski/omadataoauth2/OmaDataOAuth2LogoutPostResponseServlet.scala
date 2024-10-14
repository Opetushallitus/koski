package fi.oph.koski.omadataoauth2

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.frontendvalvonta.FrontendValvontaMode
import fi.oph.koski.servlet.{NoCache, OppijaHtmlServlet}
import org.scalatra.ScalatraServlet
import fi.oph.koski.util.JsStringInterpolation._

import scala.xml.NodeSeq

// Julkinen servlet, joka tarvitaan, että CAS-oppija logoutin jälkeen voidaan ohjata käyttäjä URLeihin, joita CAS-oppijan
// nykyiset redirect-regexpit eivät salli.
class OmaDataOAuth2LogoutPostResponseServlet(implicit val application: KoskiApplication) extends ScalatraServlet with OppijaHtmlServlet with NoCache with OmaDataOAuth2Support {

  val allowFrameAncestors: Boolean = !Environment.isServerEnvironment(application.config)
  val frontendValvontaMode: FrontendValvontaMode.FrontendValvontaMode =
    FrontendValvontaMode(application.config.getString("frontend-valvonta.mode"))

  get("/")(nonce => {
    validateQueryClientParams() match {
      case Left(validationError) =>
        logger.error(s"Internal error: ${validationError.errorDescription}")
        halt(500)
      case Right(ClientInfo(clientId, redirectUri, state)) =>
        val paramNames = Seq("redirect_uri", "code", "state", "error", "error_description", "error_uri")
        paramNames.foreach(n => logger.info(s"${n}: ${multiParams(n)}"))

        val inputParams = Seq(
          "state",
          "code",
          "error",
          "error_description",
          "error_uri"
        )

        <html lang={lang}>
          <head>
            <title>
              Submit This Form
            </title>
            <script nonce={nonce}>
              <!-- Workaround to autosubmit form after loading, since nonce cannot be specified for onload eventhandler. -->
              {jsAtom"const s = document.createElement('script'); s.src = '/koski/empty.js'; s.onload = () => { document.forms[0].submit(); }; document.documentElement.appendChild(s); "}
            </script>
          </head>
          <body>
            <form method="post" action={redirectUri}>
              {inputParams.map(renderInputIfParameterDefined)}
            </form>
          </body>
        </html>
    }
  })

  private def renderInputIfParameterDefined(paramName: String) = {
    multiParams(paramName).headOption.map(v => <input type="hidden" name={paramName} value={v}/>).getOrElse(NodeSeq.Empty)
  }
}
