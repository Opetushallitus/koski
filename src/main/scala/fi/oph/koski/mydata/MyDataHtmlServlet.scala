package fi.oph.koski.mydata

import com.typesafe.config.Config
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{AuthenticationUser, KoskiSession}
import fi.oph.koski.servlet.{HtmlServlet, InvalidRequestException}
import org.scalatra.ScalatraServlet

import scala.collection.JavaConverters._

class MyDataHtmlServlet(implicit val application: KoskiApplication) extends ScalatraServlet with HtmlServlet {
  before() {
    requireKansalainen
  }

  def share_data_yes = "yes"
  def share_data_no = "no"

  override def koskiSessionOption: Option[KoskiSession] = {
    getUser.right.toOption.map { user: AuthenticationUser =>
      KoskiSession(user, request, application.käyttöoikeusRepository)
    }
  }


  post("/:id") {
    def memberConf = getConfigForMember(params.getAs[String]("id").get)
    def clientName = memberConf.getString("name")
    def clientId = memberConf.getString("id")
    def userId = getUser.right.get.oid

    def share_data_response = params.getAs[String]("allow").get

    if (share_data_response == share_data_yes) {
      logger.info(s"User ${userId} agreed to share student data with ${clientName}")
      if (application.mydataService.put(userId, clientId)(koskiSessionOption.get)) {
        <html><body>Permission has been stored</body></html>
      } else {
        <html><body>Failed to store permission</body></html>
      }
    } else {
      logger.info(s"User ${userId} declined to share student data with ${clientName}")
      <html><body>Permission has not been granted</body></html>
    }
  }

  private def getConfigForMember(id: String): com.typesafe.config.Config = {
    application.config.getConfigList("mydata.members").asScala.find(member =>
      member.getString("id") == id)
      .getOrElse(throw InvalidRequestException(KoskiErrorCategory.badRequest.header.invalidXRoadHeader))
  }

  get("/:id") {
    if (!isAuthenticated) {
      redirectToLogin
    }

    def memberCode = params.getAs[String]("id").get

    def clientName = getConfigForMember(memberCode).getString("name")
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
