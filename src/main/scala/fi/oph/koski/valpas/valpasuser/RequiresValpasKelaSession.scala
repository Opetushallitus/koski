package fi.oph.koski.valpas.valpasuser

import fi.oph.koski.valpas.oppija.ValpasErrorCategory

trait RequiresValpasKelaSession extends ValpasAuthenticationSupport with HasValpasSession {
  implicit def session: ValpasSession = koskiSessionOption.get

  before() {
    requiresKela
  }

  private def requiresKela {
    getUser match {
      case Left(status) if status.statusCode == 401 =>
        haltWithStatus(status)
      case _ =>
        if (!koskiSessionOption.exists(_.hasKelaAccess)) {
          haltWithStatus(ValpasErrorCategory.forbidden())
        }
    }
  }
}
