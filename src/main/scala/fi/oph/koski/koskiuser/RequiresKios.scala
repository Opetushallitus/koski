package fi.oph.koski.koskiuser

import fi.oph.koski.http.KoskiErrorCategory

trait RequiresKios extends KoskiCookieAndBasicAuthenticationSupport {
  implicit def koskiSession: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requiresKios
  }

  private def requiresKios: Unit = {
    getUser match {
      case Left(status) if status.statusCode == 401 =>
        haltWithStatus(status)
      case _ =>
        if (!koskiSessionOption.exists(_.hasKiosAccess)) {
          haltWithStatus(KoskiErrorCategory.forbidden())
        }
    }
  }
}
