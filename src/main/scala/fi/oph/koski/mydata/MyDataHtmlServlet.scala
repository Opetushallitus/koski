package fi.oph.koski.mydata

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.servlet.HtmlServlet
import org.scalatra.ScalatraServlet

class MyDataHtmlServlet(implicit val application: KoskiApplication) extends ScalatraServlet with HtmlServlet {
  before() {
    requireKansalainen
  }

  def share_data_yes = "yes"
  def share_data_no = "no"

  post("/:id") {
    def clientId = params.getAs[String]("id").get
    def clientName = application.config.getString(s"mydata.${clientId}.name")
    def userId = getUser.right.get.oid

    def share_data_response = params.getAs[String]("allow").get

    if (share_data_response == share_data_yes) {
      logger.info(s"User ${userId} agreed to share student data with ${clientName}")
    } else {
      logger.info(s"User ${userId} declined to share student data with ${clientName}")
    }
  }

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
        Do you want to allow {clientName} to access your data?
        <form method="post">

          <input type="submit" name="allow" value={share_data_yes} />
          <input type="submit" name="allow" value={share_data_no} />
        </form>
      </body>
    </html>
  }

}
