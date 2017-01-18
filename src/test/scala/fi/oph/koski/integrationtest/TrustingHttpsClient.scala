package fi.oph.koski.integrationtest

import java.security.cert.X509Certificate

import org.apache.http.conn.ssl.{SSLConnectionSocketFactory, SSLContextBuilder, TrustStrategy}
import org.apache.http.impl.client.HttpClients

object TrustingHttpsClient {
  def createClient = {
    val builder = new SSLContextBuilder();
    builder.loadTrustMaterial(null, new TrustStrategy() {
      override def isTrusted(x509Certificates: Array[X509Certificate], s: String) = true
    })
    val sslsf = new SSLConnectionSocketFactory(builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
    HttpClients.custom().setSSLSocketFactory(sslsf).build();
  }
}
