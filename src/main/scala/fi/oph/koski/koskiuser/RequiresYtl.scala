package fi.oph.koski.koskiuser

import fi.oph.koski.http.KoskiErrorCategory

trait RequiresYtl extends KoskiLuovutuspalveluHeaderAuthenticationSupport with HasKoskiSpecificSession {
  implicit def session: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requiresYtl
  }

  private def requiresYtl: Unit = {
    getUser match {
      case Left(status) if status.statusCode == 401 =>
        haltWithStatus(status)
      case _ =>
        if (!koskiSessionOption.exists(_.hasYtlAccess)) {
          haltWithStatus(KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus())
        }
    }
  }
}
