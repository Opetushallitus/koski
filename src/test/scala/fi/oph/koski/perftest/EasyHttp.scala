package fi.oph.koski.perftest

import fi.oph.koski.http.OpintopolkuCallerId
import fi.oph.koski.integrationtest.TrustingHttpsClient
import fi.oph.koski.json.JsonSerializer
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.core5.http.io.HttpClientResponseHandler
import org.json4s.jackson.JsonMethods

import scala.reflect.runtime.universe.TypeTag

object EasyHttp {
  private lazy val httpclient = TrustingHttpsClient.createClient

  def getJson[A : TypeTag](url: String) = {
    val httpGet = new HttpGet(url)
    httpGet.addHeader("Caller-Id", OpintopolkuCallerId.koski)

    val handler: HttpClientResponseHandler[org.json4s.JValue] = response =>
      JsonMethods.parse(response.getEntity.getContent)
    val jValue = httpclient.execute(httpGet, handler)
    JsonSerializer.extract[A](jValue, ignoreExtras = true)
  }
}
