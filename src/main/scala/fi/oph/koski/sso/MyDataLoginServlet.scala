package fi.oph.koski.sso

import fi.oph.koski.config.KoskiApplication

case class MyDataLoginServlet(override val application: KoskiApplication) extends AbstractLoginServlet(application) {

  var memberCode: String = ""

  get("/:memberCode") {
    //memberCode = params.getAs[String]("memberCode").get
    memberCode = "jooh"
    super[AbstractLoginServlet].get _
  }

  override protected def onSuccess: String = s"/omadata/$memberCode"
  override protected def onFailure: String = s"/omadata/failed"
  override protected def onUserNotFound: String = s"/omadata/failed"

}
