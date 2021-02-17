package fi.oph.koski.koskiuser

trait RequiresVirkailijaOrPalvelukäyttäjä extends KoskiSpecificAuthenticationSupport with HasKoskiSpecificSession {
  implicit def koskiSession: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requireVirkailijaOrPalvelukäyttäjä
  }
}
