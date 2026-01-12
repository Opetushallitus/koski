package fi.oph.koski.koskiuser

import fi.oph.koski.http.KoskiErrorCategory

trait RequiresValvira extends KoskiLuovutuspalveluHeaderAuthenticationSupport with HasKoskiSpecificSession {
  implicit def session: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requiresValvira
  }

  private def requiresValvira: Unit = {
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
