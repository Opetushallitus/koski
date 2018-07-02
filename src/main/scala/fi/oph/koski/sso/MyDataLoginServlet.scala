package fi.oph.koski.sso

import fi.oph.koski.config.KoskiApplication

case class MyDataLoginServlet(override val application: KoskiApplication) extends AbstractLoginServlet(application) {

  /* Trying to mount one class two times (two instances) in ScalatraBootstrap causes issues: the one mounted first will
     get all traffic, regardless of mount path.
     So we'll create a new class to bypass this issue and just override the required functions.
   */
  override protected def onSuccess: String = s"${params("onLoginSuccess")}"
  override protected def onFailure: String = "/omadata/error"
  override protected def onUserNotFound: String = "/omadata/error/usernotfound"
}
