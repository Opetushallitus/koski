package fi.oph.koski.koskiuser

trait RequiresAuthentication extends AuthenticationSupport {
  def koskiSession: KoskiSession = koskiSessionOption.get

  before() {
    requireAuthentication
  }
}