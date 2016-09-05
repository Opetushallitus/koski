package fi.oph.koski.http

import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}
import org.scalatra.test.HttpComponentsClient

trait HttpTester extends HttpComponentsClient {
  type Headers = Map[String, String]

  val jsonContent = Map(("Content-type" -> "application/json"))

  def defaultUser: UserWithPassword

  def authHeaders(user: UserWithPassword = defaultUser): Headers = {
    Map(BasicAuthentication.basicAuthHeader(user.username, user.password))
  }

  def authGet[A](uri: String, user: UserWithPassword = defaultUser)(f: => A) = {
    get(uri, headers = authHeaders(user))(f)
  }
}