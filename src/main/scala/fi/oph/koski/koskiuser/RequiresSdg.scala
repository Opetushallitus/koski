package fi.oph.koski.koskiuser

import fi.oph.koski.http.KoskiErrorCategory

trait RequiresSdg extends KoskiLuovutuspalveluHeaderAuthenticationSupport {
  implicit def koskiSession: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requiresSdg
  }

  private def requiresSdg {
    getUser match {
      case Left(status) if status.statusCode == 401 =>
        haltWithStatus(status)
      case _ =>
        if (!koskiSessionOption.exists(_.hasSdgAccess)) {
          haltWithStatus(KoskiErrorCategory.forbidden())
        }
    }
  }
}
