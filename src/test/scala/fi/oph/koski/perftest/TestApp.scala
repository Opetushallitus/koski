package fi.oph.koski.perftest

import com.unboundid.util.Base64
import org.scalatra.test.HttpComponentsClient

trait TestApp extends HttpComponentsClient {
  val username: String = System.getProperty("koski.username", "kalle")
  val password: String = System.getProperty("koski.password", "kalle")
  override def baseUrl = System.getProperty("koski.url", "http://localhost:7021/koski")


  def authHeaders = {
    val auth: String = "Basic " + Base64.encode((username + ":" + password).getBytes("UTF8"))
    Map("Authorization" -> auth)
  }
}
