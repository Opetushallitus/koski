package fi.oph.koski.html

import java.io.File

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.Json
import fi.oph.koski.util.XML.CommentedPCData

import scala.xml.NodeSeq.Empty
import scala.xml.{Elem, NodeSeq, Unparsed}

trait HtmlNodes extends PiwikNodes {
  def application: KoskiApplication
  def buildVersion: Option[String]

  def htmlIndex(scriptBundleName: String, piwikHttpStatusCode: Option[Int] = None, raamitEnabled: Boolean = false): Elem = {
    <html>
      <head>
        {commonHead ++ raamit(raamitEnabled) ++ piwikTrackingScriptLoader(piwikHttpStatusCode)}
      </head>
      <body>
        <div data-inraamit={if (raamitEnabled) "true" else ""} id="content"></div>
      </body>
      <script id="localization">
        {Unparsed("window.koskiLocalizationMap="+Json.write(application.localizationRepository.localizations()))}
      </script>
      <script id="bundle" src={"/koski/js/" + scriptBundleName + "?" + buildVersion.getOrElse(scriptTimestamp(scriptBundleName))}></script>
    </html>
  }

  def commonHead: NodeSeq =
    <title>Koski - Opintopolku.fi</title> ++
    <meta http-equiv="X-UA-Compatible" content="IE=edge" /> ++
    <meta charset="UTF-8" /> ++
    <link rel="shortcut icon" href="/koski/favicon.ico" /> ++
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/normalize/3.0.3/normalize.min.css" /> ++
    <link href="https://fonts.googleapis.com/css?family=Open+Sans:400,600,700,800" rel="stylesheet"/> ++
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet" type="text/css" />

  private def raamit(enabled: Boolean) = if (application.config.getBoolean("useRaamit") && enabled) {
    <script type="text/javascript" src="/virkailija-raamit/apply-raamit.js"/>
  } else {
    Empty
  }

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
