package fi.oph.koski.http

import fi.oph.koski.koskiuser.UserWithPassword
import org.scalatra.test.HttpComponentsClient

import java.nio.charset.StandardCharsets
import java.util.Base64

trait HttpTester extends HttpComponentsClient {
  override def submit[A](
    method: String,
    path: String,
    queryParams: Iterable[(String, String)] = Seq.empty,
    headers: Iterable[(String, String)] = Seq.empty,
    body: Array[Byte] = null
  )(f: => A): A = {
    // Percent-encode non-ASCII characters in the path to ensure proper UTF-8 handling with Jetty 12
    val encodedPath = encodeNonAscii(path)
    super.submit(method, encodedPath, queryParams, headers, body)(f)
  }

  private def encodeNonAscii(path: String): String = {
    val sb = new StringBuilder
    for (c <- path) {
      if (c.toInt > 127) {
        for (b <- c.toString.getBytes(StandardCharsets.UTF_8)) {
          sb.append("%" + "%02X".format(b & 0xff))
        }
      } else {
        sb.append(c)
      }
    }
    sb.toString
  }
  type Headers = Map[String, String]

  val jsonContent = Map(("Content-type" -> "application/json"))

  val multipartContent = Map("Content-type" -> "multipart/form-data")

  val formContent = Map("Content-type" -> "application/x-www-form-urlencoded")

  def defaultUser: UserWithPassword

  def basicAuthHeader(user: String, password: String): (String, String) = {
    val auth: String = "Basic " + Base64.getEncoder.encodeToString((user + ":" + password).getBytes("UTF8"))
    ("Authorization", auth)
  }

  def authHeaders(user: UserWithPassword = defaultUser): Headers = {
    Map(basicAuthHeader(user.username, user.password))
  }

  def authGet[A](uri: String, user: UserWithPassword = defaultUser, headers: Map[String, String] = Map.empty)(f: => A) = {
    get(uri, headers = authHeaders(user) ++ headers)(f)
  }
}
