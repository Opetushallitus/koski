package fi.oph.tor.api

import fi.oph.tor.http.{BasicAuthentication, HttpSpecification}
import fi.oph.tor.jettylauncher.SharedJetty
import fi.oph.tor.toruser.MockUsers
import fi.oph.tor.toruser.MockUsers.MockUser

trait LocalJettyHttpSpecification extends HttpSpecification {
  SharedJetty.start

  override def baseUrl = SharedJetty.baseUrl

  def authHeaders(user: MockUser = MockUsers.kalle): Headers = {
    Map(BasicAuthentication.basicAuthHeader(user.username, user.username))
  }

  def authGet[A](uri: String, user: MockUser = MockUsers.kalle)(f: => A) = {
    get(uri, headers = authHeaders(user))(f)
  }
}

