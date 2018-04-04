package fi.oph.koski.koskiuser

// better name might be "RequiresVirkailijaOrPalvelukäyttäjä"
trait RequiresAuthentication extends AuthenticationSupport {
  implicit def koskiSession: KoskiSession = koskiSessionOption.get

  before() {
    requireAuthentication
  }
}
