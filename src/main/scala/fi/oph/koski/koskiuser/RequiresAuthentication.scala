package fi.oph.koski.koskiuser

trait RequiresAuthentication extends AuthenticationSupport {
  def torUser: KoskiUser = torUserOption.get

  before() {
    if(!isAuthenticated) {
      scentry.authenticate()
    }
  }
}