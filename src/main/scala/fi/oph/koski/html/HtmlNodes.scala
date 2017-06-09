package fi.oph.koski.html

import java.io.File

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.Json
import fi.oph.koski.util.XML.CommentedPCData

import scala.xml.{Elem, NodeSeq, Unparsed, XML}

trait HtmlNodes extends PiwikNodes {
  def application: KoskiApplication
  def buildVersion: Option[String]

  def htmlIndex(scriptBundleName: String, piwikHttpStatusCode: Option[Int] = None): Elem =
    <html>
      <head>
      {commonHead(piwikHttpStatusCode)}
      </head>
      <body>
        <div data-inraamit={if (useRaamit) "true" else ""} id="content"></div>
      </body>
      <script id="localization">
        {Unparsed("window.koskiLocalizationMap="+Json.write(application.localizationRepository.localizations()))}
      </script>
      <script id="bundle" src={"/koski/js/" + scriptBundleName + "?" + buildVersion.getOrElse(scriptTimestamp(scriptBundleName))}></script>
    </html>

  def commonHead(piwikHttpStatusCode: Option[Int] = None): NodeSeq =
    <title>Koski - Opintopolku.fi</title> ++
    <meta http-equiv="X-UA-Compatible" content="IE=edge" /> ++
    <meta charset="UTF-8" /> ++
    <link rel="shortcut icon" href="/koski/favicon.ico" /> ++
    <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/normalize/3.0.3/normalize.min.css" /> ++
    <link href="https://fonts.googleapis.com/css?family=Open+Sans:400,600,700,800" rel="stylesheet"/> ++
    <link href="//cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet" type="text/css" /> ++
    raamit ++
    piwikTrackingScriptLoader(piwikHttpStatusCode)

  def raamit: NodeSeq = if (useRaamit) {
    <script type="text/javascript" src="/virkailija-raamit/apply-raamit.js"/>
  } else {
    NodeSeq.Empty
  }

  private def useRaamit = application.config.getBoolean("useRaamit")

  def htmlErrorObjectScript(status: HttpStatus): Elem =
    <script type="text/javascript">
      {CommentedPCData("""
      window.koskiError = {
        httpStatus: """ + status.statusCode + """,
        text: '""" + status.errors.head.message.toString.replace("'", "\\'") + """',
        topLevel: true
      }
      """)}
    </script>

  private def scriptTimestamp(scriptBundleName: String) = new File(s"./target/webapp/js/$scriptBundleName").lastModified()
}
