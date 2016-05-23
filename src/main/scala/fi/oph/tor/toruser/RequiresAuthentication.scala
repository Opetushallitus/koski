package fi.oph.tor.toruser

trait RequiresAuthentication extends AuthenticationSupport {
  def torUser: TorUser = torUserOption.get

  before() {
    if(!isAuthenticated) {
      scentry.authenticate()
    }
  }
}