package fi.oph.koski.sso

import fi.oph.koski.config.KoskiApplication

case class MyDataLoginServlet(override val application: KoskiApplication) extends AbstractLoginServlet(application) {

  // ScalatraBootstrap cannot mount one class two times without issues, so lets extend and reconfigure based on our needs
  override protected def onSuccess: String = "/omadata/hsl"
  override protected def onFailure: String = "/omadata/failed"
  override protected def onUserNotFound: String = "/omadata/usernotfound"
}
