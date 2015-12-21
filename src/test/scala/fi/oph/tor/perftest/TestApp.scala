package fi.oph.tor.perftest

import com.unboundid.util.Base64
import org.scalatra.test.HttpComponentsClient

trait TestApp extends HttpComponentsClient {
  val username: String = System.getProperty("tor.username", "kalle")
  val password: String = System.getProperty("tor.password", "kalle")
  override def baseUrl = System.getProperty("tor.url", "http://localhost:7021/tor")


  def authHeaders = {
    val auth: String = "Basic " + Base64.encode((username+":"+password).getBytes("UTF8"))
    Map("Authorization" -> auth)
  }
}
