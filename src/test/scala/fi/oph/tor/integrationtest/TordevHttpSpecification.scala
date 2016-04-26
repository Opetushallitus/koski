package fi.oph.tor.integrationtest

import fi.oph.tor.http.{BasicAuthentication, HttpSpecification}

trait TordevHttpSpecification extends HttpSpecification {
  private def requiredEnv(name: String) = util.Properties.envOrNone(name).getOrElse(throw new IllegalStateException("Environment property " + name + " missing"))

  override def baseUrl = "http://tordev.tor.oph.reaktor.fi/tor"

  lazy val username = requiredEnv("TOR_USER")
  lazy val password = requiredEnv("TOR_PASS")

  def authHeaders = Map(BasicAuthentication.basicAuthHeader(username, password))
}
