package fi.oph.koski.koskiuser

trait RequiresAuthentication extends AuthenticationSupport {
  implicit def koskiSession: KoskiSession = koskiSessionOption.get

  before() {
    requireAuthentication
  }
}