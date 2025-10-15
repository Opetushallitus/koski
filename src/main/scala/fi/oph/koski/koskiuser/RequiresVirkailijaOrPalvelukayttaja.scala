package fi.oph.koski.koskiuser

trait RequiresVirkailijaOrPalvelukäyttäjä extends KoskiCookieAndBasicAuthenticationSupport with HasKoskiSpecificSession {
  implicit def session: KoskiSpecificSession = koskiSessionOption.get

  before() {
    requireVirkailijaOrPalvelukäyttäjä
  }
}
