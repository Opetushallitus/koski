package fi.oph.koski.koskiuser

import fi.oph.koski.http.KoskiErrorCategory

trait RequiresKela extends KoskiAuthenticationSupport with HasKoskiSession {
  implicit def koskiSession: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requiresKela
  }

  private def requiresKela {
    getUser match {
      case Left(status) if status.statusCode == 401 =>
        haltWithStatus(status)
      case _ =>
        if (!koskiSessionOption.exists(_.hasKelaAccess)) {
          haltWithStatus(KoskiErrorCategory.forbidden())
        }
    }
  }
}
