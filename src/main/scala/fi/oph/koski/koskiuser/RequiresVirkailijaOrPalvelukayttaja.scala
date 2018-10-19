package fi.oph.koski.koskiuser

import fi.oph.koski.http.KoskiErrorCategory

trait RequiresVirkailijaOrPalvelukäyttäjä extends AuthenticationSupport {
  implicit def koskiSession: KoskiSession = koskiSessionOption.get

  before() {
    requireVirkailijaOrPalvelukäyttäjä
    if (koskiSession.hasLuovutuspalveluAccess) {
      haltWithStatus(KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu luovutuspalvelukäyttöoikeuksilla"))
    }
  }
}
