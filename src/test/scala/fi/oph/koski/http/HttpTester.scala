package fi.oph.koski.http

import fi.oph.koski.koskiuser.UserWithPassword
import org.scalatra.test.HttpComponentsClient

import java.util.Base64

trait HttpTester extends HttpComponentsClient {
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
