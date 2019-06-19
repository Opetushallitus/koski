package fi.oph.koski.http

import java.util.Base64
import org.http4s.client.Client
import org.http4s.{Header, Request, Service}

object BasicAuthentication {
  def basicAuthHeader(user: String, password: String) = {
    val auth: String = "Basic " + Base64.getEncoder.encodeToString((user + ":" + password).getBytes("UTF8"))
    ("Authorization", auth)
  }
}

object ClientWithBasicAuthentication {
  def apply(wrappedClient: Client, username: String, password: String): Client = {
    val (name, value) = BasicAuthentication.basicAuthHeader(username, password)

    def open(req: Request) = wrappedClient.open(req.withHeaders(req.headers ++ List(Header(name, value))))

    Client(
      open = Service.lift(open _),
      shutdown = wrappedClient.shutdown
    )
  }
}
