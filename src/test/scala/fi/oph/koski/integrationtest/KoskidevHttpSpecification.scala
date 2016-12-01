package fi.oph.koski.integrationtest

import java.security.cert.X509Certificate

import fi.oph.koski.http.HttpSpecification
import fi.oph.koski.koskiuser.UserWithPassword
import org.apache.http.conn.ssl.{SSLConnectionSocketFactory, SSLContextBuilder, TrustStrategy}
import org.apache.http.impl.client.HttpClients

trait KoskidevHttpSpecification extends HttpSpecification {
  private def requiredEnv(name: String) = util.Properties.envOrNone(name).getOrElse(throw new IllegalStateException("Environment property " + name + " missing"))

  override def baseUrl = sys.env.getOrElse("KOSKI_BASE_URL", "https://dev.koski.opintopolku.fi/koski")

  def defaultUser = new UserWithPassword {
    override def username = requiredEnv("KOSKI_USER")
    override def password = requiredEnv("KOSKI_PASS")
  }

  override protected def createClient = {
    val builder = new SSLContextBuilder();
    builder.loadTrustMaterial(null, new TrustStrategy() {
      override def isTrusted(x509Certificates: Array[X509Certificate], s: String) = true
    })
    val sslsf = new SSLConnectionSocketFactory(builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
    HttpClients.custom().setSSLSocketFactory(sslsf).build();
  }
}