package fi.oph.koski.perftest

import fi.oph.koski.http.OpintopolkuCallerId
import fi.oph.koski.integrationtest.TrustingHttpsClient
import fi.oph.koski.json.JsonSerializer
import org.apache.http.client.methods.HttpGet
import org.json4s.jackson.JsonMethods

import scala.reflect.runtime.universe.TypeTag

object EasyHttp {
  private lazy val httpclient = TrustingHttpsClient.createClient

  def getJson[A : TypeTag](url: String) = {
    val httpGet = new HttpGet(url)
    httpGet.addHeader("Caller-Id", OpintopolkuCallerId.koski)

    val jValue = JsonMethods.parse(httpclient.execute(httpGet).getEntity.getContent)
    JsonSerializer.extract[A](jValue, ignoreExtras = true)
  }
}
