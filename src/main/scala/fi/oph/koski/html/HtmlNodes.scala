package fi.oph.koski.html

import java.io.File

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.util.XML.CommentedPCData

import scala.xml.Elem

trait HtmlNodes extends PiwikNodes {
  def buildVersion: Option[String]

  def htmlIndex(scriptBundleName: String, piwikHttpStatusCode: Option[Int] = None): Elem =
    <html>
      {htmlHead(piwikHttpStatusCode)}
      <body>
        <div id="content"></div>
      </body>
      <script id="bundle" src={"/koski/js/" + scriptBundleName + "?" + buildVersion.getOrElse(scriptTimestamp(scriptBundleName))}></script>
    </html>

  def htmlHead(piwikHttpStatusCode: Option[Int] = None) : Elem =
    <head>
      <title>Koski - Opintopolku.fi</title>
      <meta http-equiv="X-UA-Compatible" content="IE=edge" />
      <meta charset="UTF-8" />
      <link rel="shortcut icon" href="/koski/favicon.ico" />
      <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/normalize/3.0.3/normalize.min.css" />
      <link href="https://fonts.googleapis.com/css?family=Open+Sans:400,600,700,800" rel="stylesheet"/>
      <link href="//cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet" type="text/css" />
      {piwikTrackingScriptLoader(piwikHttpStatusCode)}
    </head>

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
