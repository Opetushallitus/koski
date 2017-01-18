package fi.oph.koski.perftest

import fi.oph.koski.json.Json
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients

object EasyHttp {
  def getJson[A](url: String)(implicit mf: Manifest[A]) = {
    val httpclient = HttpClients.createDefault()
    val httpGet = new HttpGet(url)
    Json.read[A](httpclient.execute(httpGet).getEntity.getContent)
  }
}
