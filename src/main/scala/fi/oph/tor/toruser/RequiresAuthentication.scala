package fi.oph.tor.toruser

import fi.oph.tor.servlet.ErrorHandlingServlet

trait RequiresAuthentication extends ErrorHandlingServlet with AuthenticationSupport {
  def torUser: TorUser = torUserOption.get

  before() {
    if(!isAuthenticated) {
      scentry.authenticate()
    }
  }
}