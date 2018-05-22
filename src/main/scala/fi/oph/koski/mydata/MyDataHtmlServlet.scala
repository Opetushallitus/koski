package fi.oph.koski.mydata

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.servlet.HtmlServlet
import org.scalatra.ScalatraServlet

class MyDataHtmlServlet(implicit val application: KoskiApplication) extends ScalatraServlet with HtmlServlet {

  get("/:id") {
    if (!isAuthenticated) {
      redirectToLogin
    }

    def clientId = params.getAs[String]("id").get
    def clientName = application.config.getString(s"mydata.${clientId}.name")

    def userId = getUser.right.get.oid

    logger.info(s"Requesting permissions for ${clientName} to access the data of student ${userId}")

    <html>
      <head>
        {commonHead() ++ piwikTrackingScriptLoader()}
        <link rel="stylesheet" type="text/css" href="/koski/css/raportti.css"></link>
      </head>
      <body id="share-data">
        Allow {clientName} to access your data?
      </body>
    </html>
  }

}
