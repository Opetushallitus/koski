package fi.oph.koski.api

import fi.oph.koski.http.HttpSpecification
import fi.oph.koski.jettylauncher.SharedJetty
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.log.AuditLogTester

trait LocalJettyHttpSpecification extends HttpSpecification {
  override def baseUrl = {
    LocalJettyHttpSpecification.setup(this)
    SharedJetty.baseUrl
  }

  def defaultUser = MockUsers.kalle
}

object LocalJettyHttpSpecification {
  var running = false
  def setup(spec: HttpSpecification) = synchronized {
    if (!running) {
      running = true
      SharedJetty.start
      AuditLogTester.setup
      spec.resetFixtures
    }
  }
}