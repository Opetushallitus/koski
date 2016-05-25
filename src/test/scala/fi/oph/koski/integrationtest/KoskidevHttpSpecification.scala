package fi.oph.koski.integrationtest

import fi.oph.koski.http.{BasicAuthentication, HttpSpecification}

trait KoskidevHttpSpecification extends HttpSpecification {
  private def requiredEnv(name: String) = util.Properties.envOrNone(name).getOrElse(throw new IllegalStateException("Environment property " + name + " missing"))

  override def baseUrl = "https://tordev.tor.oph.reaktor.fi/koski"

  lazy val username = requiredEnv("KOSKI_USER")
  lazy val password = requiredEnv("KOSKI_PASS")

  def authHeaders = Map(BasicAuthentication.basicAuthHeader(username, password))
}
