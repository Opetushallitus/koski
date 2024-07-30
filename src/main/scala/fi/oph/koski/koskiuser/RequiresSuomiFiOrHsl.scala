package fi.oph.koski.koskiuser

import fi.oph.koski.http.KoskiErrorCategory

trait RequiresSuomiFiOrHsl extends KoskiSpecificAuthenticationSupport {
  implicit def koskiSession: KoskiSpecificSession = koskiSessionOption.get

  before() {
    getUser match {
      case Left(status) if status.statusCode == 401 =>
        haltWithStatus(status)
      case _ =>
        if (!koskiSessionOption.exists(_.hasSuomiFiAccess) && !koskiSessionOption.exists(_.hasHSLAccess)) {
          haltWithStatus(KoskiErrorCategory.forbidden.vainPalveluvayla())
        }
    }
  }
}
