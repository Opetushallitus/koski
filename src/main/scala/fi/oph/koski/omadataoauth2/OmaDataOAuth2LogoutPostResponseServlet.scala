package fi.oph.koski.omadataoauth2

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.frontendvalvonta.FrontendValvontaMode
import fi.oph.koski.servlet.{NoCache, OppijaHtmlServlet}
import org.scalatra.ScalatraServlet
import fi.oph.koski.util.JsStringInterpolation._
import org.http4s.Uri

import scala.xml.NodeSeq

class OmaDataOAuth2LogoutPostResponseServlet(implicit val application: KoskiApplication) extends ScalatraServlet with OppijaHtmlServlet with NoCache with OmaDataOAuth2Support {

  val allowFrameAncestors: Boolean = !Environment.isServerEnvironment(application.config)
  val frontendValvontaMode: FrontendValvontaMode.FrontendValvontaMode =
    FrontendValvontaMode(application.config.getString("frontend-valvonta.mode"))

  override def formActionSources: Option[String] = {
    if (useFormActionCspHeader(multiParams("client_id").headOption.getOrElse(""))) {
      val redirectUriFromParams = multiParams("redirect_uri").headOption.getOrElse("")

      Uri.fromString(redirectUriFromParams).map(fullUri => Uri(fullUri.scheme, fullUri.authority, fullUri.path)) match {
        case Right(result) if result.host.isDefined =>
          val formActionUri = result.toString
          Some(formActionUri)
        case _ =>
          super.formActionSources
      }
    } else {
      None
    }
  }

  get("/")(nonce => {
    validateQueryClientParams() match {
      case Left(validationError) =>
        // .error toistaiseksi, koska tätä virhettä ei yleisesti pitäisi tapahtua, jos clientin fronttikoodissa ei ole bugeja
        logger.error(validationError.getLoggedErrorMessage)

        redirectWithErrorsToResourceOwnerFrontend(validationError.getClientErrorParams)
      case Right(ClientInfo(clientId, redirectUri, state)) =>
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
