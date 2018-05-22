package fi.oph.koski.html

import java.io.File

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.servlet.{KoskiBaseServlet, LanguageSupport}
import fi.oph.koski.util.XML.CommentedPCData

import scala.xml.NodeSeq.Empty
import scala.xml.{Elem, NodeSeq, Unparsed}

trait HtmlNodes extends KoskiBaseServlet with PiwikNodes with LanguageSupport {
  def application: KoskiApplication
  def buildVersion: Option[String]
  def localizations: LocalizationRepository = application.localizationRepository

  def htmlIndex(scriptBundleName: String, piwikHttpStatusCode: Option[Int] = None, raamit: Raamit = EiRaameja, scripts: NodeSeq = Empty, responsive: Boolean = false): Elem = {
    var bodyClasses = scriptBundleName.replace("koski-", "").replace(".js", "") + "-page"
    <html>
      <head>
        {commonHead(responsive) ++ raamit.script ++ piwikTrackingScriptLoader(piwikHttpStatusCode)}
      </head>
      <body class={bodyClasses}>
        <div data-inraamit={if (raamit != EiRaameja) "true" else ""} id="content" class="koski-content"></div>
        <script id="localization">
          {Unparsed("window.koskiLocalizationMap="+JsonSerializer.writeWithRoot(localizations.localizations))}
        </script>
        {scripts}
        <script id="bundle" src={"/koski/js/" + scriptBundleName + "?" + buildVersion.getOrElse(scriptTimestamp(scriptBundleName))}></script>
      </body>
    </html>
  }

  def commonHead(responsive: Boolean = false): NodeSeq =
    <title>Koski - Opintopolku.fi</title> ++
    <meta http-equiv="X-UA-Compatible" content="IE=edge" /> ++
    <meta charset="UTF-8" /> ++
    {if (responsive) <meta name="viewport" content="width=device-width,initial-scale=1" /> else NodeSeq.Empty} ++
    <meta name="robots" content="noindex" /> ++
    <link rel="shortcut icon" href="/koski/favicon.ico" /> ++
    <link rel="stylesheet" href="/koski/external_css/normalize.min.css" /> ++
    <link href="/koski/external_css/SourceSansPro.css" rel="stylesheet"/> ++
    <link href="/koski/external_css/font-awesome.min.css" rel="stylesheet" type="text/css" /> ++
    <link rel="stylesheet" type="text/css" href="/koski/external_css/highlight-js.default.min.css"/> ++
    <link rel="stylesheet" type="text/css" href="/koski/css/codemirror/codemirror.css"/>


  def htmlErrorObjectScript(status: HttpStatus): Elem =
    <script type="text/javascript">
      {CommentedPCData("""
        window.koskiError = {
          httpStatus: """ + status.statusCode + """,
          text: '""" + status.errorString.getOrElse(localizations.get("httpStatus." + status.statusCode).get(lang)).replace("'", "\\'") + """',
          topLevel: true
        }
      """)}
    </script>

  private def scriptTimestamp(scriptBundleName: String) = new File(s"./target/webapp/js/$scriptBundleName").lastModified()
}

trait Raamit {
  def script: NodeSeq
}

case object Virkailija extends Raamit {
  override def script: NodeSeq = <script type="text/javascript" src="/virkailija-raamit/apply-raamit.js"/>
}

case class Oppija(session: Option[KoskiSession], shibbolethUrl: String) extends Raamit {
  override def script: NodeSeq = {
    <script>
      {Unparsed(s"""
        Service = {
          getUser: function() { return Promise.resolve($user) },
          login: function() { window.location = '$shibbolethUrl' },
          logout: function() { window.location = '/koski/user/logout' }
        }
      """)}
    </script> ++
    <script id="apply-raamit" type="text/javascript" src="/oppija-raamit/js/apply-raamit.js"></script>
  }

  private def user = session.map(s => s"""{"name":"${s.user.name}", "oid": "${s.oid}"}""").getOrElse("null")
}

case object EiRaameja extends Raamit {
  override def script: NodeSeq = Empty
}
