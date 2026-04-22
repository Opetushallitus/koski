package fi.oph.koski.integrationtest

import org.apache.hc.client5.http.impl.classic.{CloseableHttpClient, HttpClients}
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.apache.hc.client5.http.ssl.{NoopHostnameVerifier, TrustAllStrategy}
import org.apache.hc.core5.ssl.SSLContextBuilder

object TrustingHttpsClient {
  def createClient: CloseableHttpClient = {
    val sslContext = new SSLContextBuilder()
      .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
      .build()

    val connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
      .setTlsSocketStrategy(new org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy(sslContext, NoopHostnameVerifier.INSTANCE))
      .build()

    HttpClients
      .custom()
      .setConnectionManager(connectionManager)
      .disableRedirectHandling()
      .build()
  }
}
