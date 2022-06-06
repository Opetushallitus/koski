package fi.oph.koski.integrationtest

import org.apache.http.client.config.{CookieSpecs, RequestConfig}
import org.apache.http.conn.ssl.{NoopHostnameVerifier, SSLConnectionSocketFactory}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.ssl.{SSLContextBuilder, TrustStrategy}

import java.security.cert.X509Certificate

object TrustingHttpsClient {
  def createClient: CloseableHttpClient = {
    HttpClients
      .custom
      .setSSLSocketFactory(makeSslConnectionSocketFactory)
      .disableRedirectHandling
      .disableCookieManagement
      .build
  }

  private def makeSslConnectionSocketFactory = {
    val builder = new SSLContextBuilder();
    builder.loadTrustMaterial(
      null, new TrustStrategy() {
        override def isTrusted(x509Certificates: Array[X509Certificate], s: String) = true
      })
    new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE)
  }
}
