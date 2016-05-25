package fi.oph.koski.integrationtest

import fi.oph.koski.http.{BasicAuthentication, HttpSpecification}

trait KoskidevHttpSpecification extends HttpSpecification {
  private def requiredEnv(name: String) = util.Properties.envOrNone(name).getOrElse(throw new IllegalStateException("Environment property " + name + " missing"))

  override def baseUrl = "http://tordev.tor.oph.reaktor.fi/koski"

  lazy val username = requiredEnv("TOR_USER")
  lazy val password = requiredEnv("TOR_PASS")

  def authHeaders = Map(BasicAuthentication.basicAuthHeader(username, password))
}
