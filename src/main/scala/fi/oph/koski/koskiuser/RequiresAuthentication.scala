package fi.oph.koski.koskiuser

trait RequiresAuthentication extends AuthenticationSupport {
  def koskiUser: KoskiUser = koskiUserOption.get

  before() {
    requireAuthentication
  }
}