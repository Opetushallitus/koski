package fi.oph.koski.http

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.http4s.{BasicCredentials, Headers}

object ClientWithBasicAuthentication {
  def apply(client: Client[IO], username: String, password: String): Client[IO] = {
    val authHeader = Authorization(BasicCredentials(username, password))

    Client { request =>
      client.run(request.withHeaders(request.headers ++ Headers(authHeader)))
    }
  }
}
