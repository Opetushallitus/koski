package fi.oph.koski.koskiuser

import fi.oph.koski.http.KoskiErrorCategory

trait RequiresKelaLaaja extends AuthenticationSupport with HasKoskiSession {
  implicit def koskiSession: KoskiSession = koskiSessionOption.get

  before() {
    requiresKelaLaaja
  }

  private def requiresKelaLaaja {
    getUser match {
      case Left(status) if status.statusCode == 401 =>
        haltWithStatus(status)
      case _ =>
        if (!koskiSessionOption.exists(_.hasKelaLaajatAccess)) {
          haltWithStatus(KoskiErrorCategory.forbidden())
        }
    }
  }
}
