package fi.oph.koski.koskiuser

import fi.oph.koski.http.KoskiErrorCategory

trait RequiresVkt extends KoskiSpecificAuthenticationSupport {
  implicit def koskiSession: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requiresVkt
  }

  private def requiresVkt {
    getUser match {
      case Left(status) if status.statusCode == 401 =>
        haltWithStatus(status)
      case _ =>
        if (!koskiSessionOption.exists(_.hasVktAccess)) {
          haltWithStatus(KoskiErrorCategory.forbidden())
        }
    }
  }
}
