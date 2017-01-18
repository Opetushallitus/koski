package fi.oph.koski.perftest

import fi.oph.koski.integrationtest.TrustingHttpsClient
import fi.oph.koski.json.Json
import org.apache.http.client.methods.HttpGet

object EasyHttp {
  lazy val httpclient = TrustingHttpsClient.createClient
  def getJson[A](url: String)(implicit mf: Manifest[A]) = {
    val httpGet = new HttpGet(url)
    Json.read[A](httpclient.execute(httpGet).getEntity.getContent)
  }
}
