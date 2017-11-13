package fi.oph.koski.integrationtest

import fi.oph.koski.http.HttpSpecification
import fi.oph.koski.koskiuser.UserWithPassword

trait KoskidevHttpSpecification extends HttpSpecification with EnvVariables {
  def refreshElasticSearchIndex = {

  }

  override def baseUrl = sys.env.getOrElse("KOSKI_BASE_URL", "https://dev.koski.opintopolku.fi/koski")

  def defaultUser = new UserWithPassword {
    override def username = requiredEnv("KOSKI_USER")
    override def password = requiredEnv("KOSKI_PASS")
  }

  override protected def createClient = TrustingHttpsClient.createClient
}