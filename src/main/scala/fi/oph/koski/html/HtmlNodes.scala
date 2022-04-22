package fi.oph.koski.html

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.servlet.{KoskiSpecificBaseServlet, LanguageSupport}
import fi.oph.koski.util.JsStringInterpolation._
import fi.oph.koski.util.XML.CommentedPCData
import org.scalatra.servlet.RichRequest

import java.io.File
import java.net.URLDecoder
import scala.xml.NodeSeq.Empty
import scala.xml.{Elem, NodeSeq}

trait HtmlNodes extends KoskiSpecificBaseServlet with PiwikNodes with LanguageSupport {
  def application: KoskiApplication
  def buildVersion: Option[String]
  def localizations: LocalizationRepository = application.koskiLocalizationRepository

  def htmlIndex(scriptBundleName: String, piwikHttpStatusCode: Option[Int] = None, raamit: Raamit = EiRaameja, scripts: NodeSeq = Empty, responsive: Boolean = false, allowIndexing: Boolean = false,
    nonce: String
  ): Elem = {
    val bodyClasses = scriptBundleName.replace("koski-", "").replace(".js", "") + "-page"
    <html lang={lang}>
      <head>
        {commonHead(responsive, allowIndexing, nonce) ++ raamit.script(nonce) ++ piwikTrackingScriptLoader(nonce, piwikHttpStatusCode)}
        <script nonce={nonce}>
          {setWindowVar("nonce", nonce)}
        </script>
      </head>
      <body class={bodyClasses}>
        <!-- virkailija-raamit header is inserted here -->
        {if (raamit.includeJumpToContentLink) <a href="#content" class="jump-to-main-content" tabindex="1">{t("Hyppää sisältöön")}</a> else NodeSeq.Empty}
        <div id="oppija-raamit-header-here"><!-- oppija-raamit header is inserted here --></div>
        <div data-inraamit={raamit.toString} id="content" class="koski-content"></div>
        <script nonce={nonce} id="localization">
          {setWindowVar("koskiLocalizationMap", localizations.localizations)}
        </script>
        <script nonce={nonce} >
          {setWindowVar("environment", Environment.currentEnvironment(application.config))}
        </script>
        {scripts}
        <script nonce={nonce} id="bundle" src={"/koski/js/" + scriptBundleName + "?" + buildVersion.getOrElse(scriptTimestamp(scriptBundleName))}></script>
        <!-- oppija-raamit footer is inserted here -->
      </body>
    </html>
  }

  def commonHead(responsive: Boolean = false, allowIndexing: Boolean = false, nonce: String): NodeSeq = {
    <title>Koski - Opintopolku.fi</title> ++
    <meta http-equiv="X-UA-Compatible" content="IE=edge" /> ++
    <meta charset="UTF-8" /> ++
    {if (responsive) <meta name="viewport" content="width=device-width,initial-scale=1" /> else NodeSeq.Empty} ++
    {if (allowIndexing) NodeSeq.Empty else <meta name="robots" content="noindex" />} ++
    <link nonce={nonce} rel="shortcut icon" href="/koski/favicon.ico" /> ++
    <link nonce={nonce} rel="stylesheet" href="/koski/external_css/normalize.min.css" /> ++
    <link nonce={nonce} href="/koski/external_css/SourceSansPro.css" rel="stylesheet"/> ++
    <link nonce={nonce} href="/koski/external_css/font-awesome.min.css" rel="stylesheet" type="text/css" /> ++
    <link nonce={nonce} rel="stylesheet" type="text/css" href="/koski/external_css/highlight-js.default.min.css"/> ++
    <link nonce={nonce} rel="stylesheet" type="text/css" href="/koski/css/codemirror/codemirror.css"/>
    <link nonce={nonce} rel="stylesheet" type="text/css" href="/koski/css/koski-oppija-raamit.css"/>
  }


  def htmlErrorObjectScript(nonce: String, status: HttpStatus): Elem =
    <script nonce={nonce} type="text/javascript">
      {CommentedPCData(js"""
        window.koskiError = {
          httpStatus: ${status.statusCode},
          text: ${status.errorString.getOrElse(localizations.get("httpStatus." + status.statusCode).get(lang)).replace("'", "\\'")},
          topLevel: true
        }
      """)}
    </script>

  private def scriptTimestamp(scriptBundleName: String) = new File(s"./target/webapp/js/$scriptBundleName").lastModified()
}

trait Raamit {
  def script(nonce: String): NodeSeq
  def includeJumpToContentLink: Boolean = false
}

case object Virkailija extends Raamit {
  override def script(nonce: String): NodeSeq = <script nonce={nonce} type="text/javascript" src="/virkailija-raamit/apply-raamit.js"/>
  override def toString: String = "virkailija"
}

case class Oppija(session: Option[KoskiSpecificSession], request: RichRequest, loginUrl: String) extends Raamit {
  override def script(nonce: String): NodeSeq = {
    <script nonce={nonce}>
      {jsAtom"""
        Service = {
          getUser: function() { return Promise.resolve($user) },
          login: function() { window.location = $loginUrl },
          logout: function() { window.location = '/koski/user/logout' }
        }
      """}
    </script> ++
    <script nonce={nonce} defer="defer" id="apply-raamit" type="text/javascript" src="/oppija-raamit/js/apply-raamit.js"></script>
  }

  private def user: Option[CasUser] =
    session
      .map(CasUser.apply)
      .orElse(casUserFromCookie)

  private def casUserFromCookie: Option[CasUser] =
    request.cookies.get("eisuorituksia")
      .map(c => URLDecoder.decode(c, "UTF-8"))
      .map(name => CasUser(name))

  override def toString: String = "oppija"

  override def includeJumpToContentLink: Boolean = true
}

case object EiRaameja extends Raamit {
  override def script(nonce: String): NodeSeq = Empty
  override def toString: String = ""
}

case class CasUser(
  name: String,
  oid: Option[String] = None,
)

object CasUser {
  def apply(session: KoskiSpecificSession): CasUser = CasUser(
    name = session.user.name,
    oid = Some(session.oid),
  )
}
