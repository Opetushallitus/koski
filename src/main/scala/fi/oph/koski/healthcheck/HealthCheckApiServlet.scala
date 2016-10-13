package fi.oph.koski.healthcheck

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.koskiuser.KoskiUser.systemUser
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class HealthCheckApiServlet(val application: KoskiApplication) extends ApiServlet with NoCache {
  get() {
    HeathChecker(application).healthcheck match {
      case Left(status) => renderObject(Map("status" -> status.statusCode))
      case _ => renderObject(Map("status" -> 200))
    }
  }

  override def koskiUserOption: Option[KoskiUser] = Some(systemUser)
}

