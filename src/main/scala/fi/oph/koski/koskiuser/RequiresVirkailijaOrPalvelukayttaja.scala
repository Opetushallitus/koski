package fi.oph.koski.koskiuser

trait RequiresVirkailijaOrPalvelukäyttäjä extends AuthenticationSupport {
  implicit def koskiSession: KoskiSession = koskiSessionOption.get

  before() {
    requireVirkailijaOrPalvelukäyttäjä
  }
}
