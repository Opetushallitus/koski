package fi.oph.koski.api

import fi.oph.koski.http.{BasicAuthentication, HttpSpecification}
import fi.oph.koski.jettylauncher.SharedJetty
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.koskiuser.MockUsers.MockUser

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

