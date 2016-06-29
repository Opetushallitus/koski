package fi.oph.koski.api

import fi.oph.koski.http.HttpSpecification
import fi.oph.koski.jettylauncher.SharedJetty
import fi.oph.koski.koskiuser.MockUsers

trait LocalJettyHttpSpecification extends HttpSpecification {
  SharedJetty.start
  resetFixtures

  override def baseUrl = SharedJetty.baseUrl

  def defaultUser = MockUsers.kalle
}

