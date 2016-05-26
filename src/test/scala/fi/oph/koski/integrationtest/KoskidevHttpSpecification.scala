package fi.oph.koski.integrationtest

import fi.oph.koski.http.{BasicAuthentication, HttpSpecification}
import org.apache.http.conn.ssl.{SSLConnectionSocketFactory, SSLContextBuilder, TrustSelfSignedStrategy}
import org.apache.http.impl.client.{HttpClients, HttpClientBuilder}

trait KoskidevHttpSpecification extends HttpSpecification {
  private def requiredEnv(name: String) = util.Properties.envOrNone(name).getOrElse(throw new IllegalStateException("Environment property " + name + " missing"))

  override def baseUrl = "https://koskidev.koski.oph.reaktor.fi/koski"

  lazy val username = requiredEnv("KOSKI_USER")
  lazy val password = requiredEnv("KOSKI_PASS")

  def authHeaders = Map(BasicAuthentication.basicAuthHeader(username, password))

  override protected def createClient = {
    val builder = new SSLContextBuilder();
    builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
    val sslsf = new SSLConnectionSocketFactory(builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
    HttpClients.custom().setSSLSocketFactory(sslsf).build();
  }
}
