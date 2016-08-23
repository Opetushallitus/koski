package fi.oph.koski.koski

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.koskiuser.KoskiUser.systemUser
import fi.oph.koski.servlet.ApiServlet

class HealthCheckServlet(val application: KoskiApplication) extends ApiServlet {

  get() {
    application.facade.findOppija(application.config.getString("healthcheck.oppija.oid"))(systemUser) match {
      case Left(status) => renderObject(Map("status" -> status.statusCode))
      case _ => renderObject(Map("status" -> 200))
    }
  }

  override def koskiUserOption: Option[KoskiUser] = Some(systemUser)
}
