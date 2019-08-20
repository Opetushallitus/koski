package fi.oph.koski.http

import fi.oph.koski.koskiuser.UserWithPassword
import org.scalatra.test.HttpComponentsClient

trait HttpTester extends HttpComponentsClient {
  type Headers = Map[String, String]

  val jsonContent = Map(("Content-type" -> "application/json"))

  val multipartContent = Map("Content-type" -> "multipart/form-data")

  def defaultUser: UserWithPassword

  def authHeaders(user: UserWithPassword = defaultUser): Headers = {
    Map(BasicAuthentication.basicAuthHeader(user.username, user.password))
  }

  def authGet[A](uri: String, user: UserWithPassword = defaultUser, headers: Map[String, String] = Map.empty)(f: => A) = {
    get(uri, headers = authHeaders(user) ++ headers)(f)
  }
}
