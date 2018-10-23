package fi.oph.koski.koskiuser

import fi.oph.koski.http.KoskiErrorCategory

trait RequiresLuovutuspalvelu extends AuthenticationSupport {
  implicit def koskiSession: KoskiSession = koskiSessionOption.get

  before() {
    if (!koskiSession.hasLuovutuspalveluAccess) {
      haltWithStatus(KoskiErrorCategory.forbidden.vainViranomainen())
    }
  }
}
