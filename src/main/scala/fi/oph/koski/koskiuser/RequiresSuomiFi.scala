package fi.oph.koski.koskiuser

import fi.oph.koski.http.KoskiErrorCategory

trait RequiresSuomiFi extends KoskiLuovutuspalveluHeaderAuthenticationSupport with HasKoskiSpecificSession {
  implicit def session: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requiresSuomiFi
  }

  private def requiresSuomiFi: Unit = {
    getUser match {
      case Left(status) if status.statusCode == 401 =>
        haltWithStatus(status)
      case _ =>
        if (!koskiSessionOption.exists(_.hasSuomiFiAccess)) {
          haltWithStatus(KoskiErrorCategory.forbidden())
        }
    }
  }
}
