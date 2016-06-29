package fi.oph.koski.integrationtest

import fi.oph.koski.http.{BasicAuthentication, HttpSpecification}
import fi.oph.koski.koskiuser.UserWithPassword
import org.apache.http.conn.ssl.{SSLConnectionSocketFactory, SSLContextBuilder, TrustSelfSignedStrategy}
import org.apache.http.impl.client.HttpClients

trait KoskidevHttpSpecification extends HttpSpecification {
  private def requiredEnv(name: String) = util.Properties.envOrNone(name).getOrElse(throw new IllegalStateException("Environment property " + name + " missing"))

  override def baseUrl = sys.env.getOrElse("KOSKI_BASE_URL", "https://koskidev.koski.oph.reaktor.fi/koski")

  def defaultUser = new UserWithPassword {
    override def username = requiredEnv("KOSKI_USER")
    override def password = requiredEnv("KOSKI_PASS")
  }

  override protected def createClient = {
    val builder = new SSLContextBuilder();
    builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
    val sslsf = new SSLConnectionSocketFactory(builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
    HttpClients.custom().setSSLSocketFactory(sslsf).build();
  }
}
