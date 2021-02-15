package fi.oph.koski.koskiuser

import fi.oph.koski.http.KoskiErrorCategory

trait RequiresValvira extends KoskiAuthenticationSupport with HasKoskiSession {
  implicit def koskiSession: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requiresValvira
  }

  private def requiresValvira {
    getUser match {
      case Left(status) if status.statusCode == 401 =>
        haltWithStatus(status)
      case _ =>
        if (!koskiSessionOption.exists(_.hasValviraAccess)) {
          haltWithStatus(KoskiErrorCategory.forbidden())
        }
    }
  }
}
