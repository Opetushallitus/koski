package fi.oph.koski.koskiuser

trait RequiresVirkailijaOrPalvelukäyttäjä extends KoskiAuthenticationSupport with HasKoskiSession {
  implicit def koskiSession: KoskiSession = koskiSessionOption.get

  before() {
    requireVirkailijaOrPalvelukäyttäjä
  }
}
