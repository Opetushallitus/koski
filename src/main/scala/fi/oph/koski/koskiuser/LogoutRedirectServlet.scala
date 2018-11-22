package fi.oph.koski.koskiuser

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.mydata.MyDataConfig
import org.scalatra.ScalatraServlet

class LogoutRedirectServlet(implicit val koskiApplication: KoskiApplication) extends ScalatraServlet {
  /**
    * Simple servlet for redirecting to whatever URL is passed as a parameter.
    * Required for Shibboleth logout, as it will only redirect to whitelisted addresses.
    */

  private val mydataconfig: MyDataConfig = new MyDataConfig {
    override def application: KoskiApplication = koskiApplication
  }

  get("/") {
    if (mydataconfig.isWhitelistedCallbackURL(params("target"))) {
      redirect(params("target"))
    } else {
      halt(KoskiErrorCategory.badRequest.statusCode, KoskiErrorCategory.badRequest.queryParam.invalidCallbackParameter)
    }
  }
}
