package fi.oph.koski.html

import java.io.File

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.servlet.KoskiBaseServlet
import fi.oph.koski.util.XML.CommentedPCData

import scala.xml.NodeSeq.Empty
import scala.xml.{Elem, NodeSeq, Unparsed}

trait HtmlNodes extends KoskiBaseServlet with PiwikNodes {
  def application: KoskiApplication
  def buildVersion: Option[String]
  def localizations: LocalizationRepository = application.localizationRepository

  def htmlIndex(scriptBundleName: String, piwikHttpStatusCode: Option[Int] = None, raamitEnabled: Boolean = false, scripts: NodeSeq = Empty, responsive: Boolean = false): Elem = {
    var bodyClasses = scriptBundleName.replace("koski-", "").replace(".js", "") + "-page"
    <html>
      <head>
        {commonHead(responsive) ++ raamit(raamitEnabled) ++ piwikTrackingScriptLoader(piwikHttpStatusCode)}
      </head>
      <body class={bodyClasses}>
        <div data-inraamit={if (raamitEnabled) "true" else ""} id="content"></div>
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
    <link href="/koski/external_css/OpenSans.css" rel="stylesheet"/> ++
    <link href="/koski/external_css/font-awesome.min.css" rel="stylesheet" type="text/css" /> ++
    <link rel="stylesheet" type="text/css" href="/koski/external_css/highlight-js.default.min.css"/> ++
    <link rel="stylesheet" type="text/css" href="/koski/css/codemirror/codemirror.css"/>



  private def raamit(enabled: Boolean) = if (enabled) <script type="text/javascript" src="/virkailija-raamit/apply-raamit.js"/> else Empty

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
