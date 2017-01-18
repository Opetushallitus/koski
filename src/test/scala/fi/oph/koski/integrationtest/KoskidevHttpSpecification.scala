package fi.oph.koski.integrationtest

import fi.oph.koski.http.HttpSpecification
import fi.oph.koski.koskiuser.UserWithPassword

trait KoskidevHttpSpecification extends HttpSpecification {
  private def requiredEnv(name: String) = util.Properties.envOrNone(name).getOrElse(throw new IllegalStateException("Environment property " + name + " missing"))

  override def baseUrl = sys.env.getOrElse("KOSKI_BASE_URL", "https://dev.koski.opintopolku.fi/koski")

  def defaultUser = new UserWithPassword {
    override def username = requiredEnv("KOSKI_USER")
    override def password = requiredEnv("KOSKI_PASS")
  }

  override protected def createClient = TrustingHttpsClient.createClient
}

