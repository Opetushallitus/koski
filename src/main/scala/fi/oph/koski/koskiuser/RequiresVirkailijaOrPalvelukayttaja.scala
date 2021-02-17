package fi.oph.koski.koskiuser

trait RequiresVirkailijaOrPalvelukäyttäjä extends KoskiSpecificAuthenticationSupport with HasKoskiSpecificSession {
  implicit def session: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requireVirkailijaOrPalvelukäyttäjä
  }
}
