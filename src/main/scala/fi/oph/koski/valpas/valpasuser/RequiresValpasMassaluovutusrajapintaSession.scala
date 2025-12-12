package fi.oph.koski.valpas.valpasuser

import fi.oph.koski.valpas.oppija.ValpasErrorCategory

trait RequiresValpasMassaluovutusrajapintaSession extends ValpasCookieAndBasicAuthAuthenticationSupport with HasValpasSession {
  implicit def session: ValpasSession = koskiSessionOption.get

  before() {
    requiresMassaluovutusrajapinta
  }

  private def requiresMassaluovutusrajapinta {
    getUser match {
      case Left(status) if status.statusCode == 401 =>
        haltWithStatus(status)
      case _ =>
        if (
          !koskiSessionOption.exists(_.hasMassaluovutusrajapintaAccess) &&
            !koskiSessionOption.exists(_.hasGlobalValpasOikeus(Set(ValpasRooli.KUNTA_MASSALUOVUTUS)))
        ) {
          haltWithStatus(ValpasErrorCategory.forbidden())
        }
    }
  }
}
