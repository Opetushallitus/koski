package fi.oph.common.koskiuser

trait RequiresVirkailijaOrPalvelukäyttäjä extends AuthenticationSupport with HasKoskiSession {
  implicit def koskiSession: KoskiSession = koskiSessionOption.get

  before() {
    requireVirkailijaOrPalvelukäyttäjä
  }
}
