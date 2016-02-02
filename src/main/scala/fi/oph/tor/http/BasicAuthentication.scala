package fi.oph.tor.http

import com.unboundid.util.Base64
import org.http4s.{Header, Request}
import org.http4s.client.Client

object BasicAuthentication {
  def basicAuthHeader(user: String, password: String) = {
    val auth: String = "Basic " + Base64.encode((user + ":" + password).getBytes("UTF8"))
    ("Authorization", auth)
  }
}

class ClientWithBasicAuthentication(wrappedClient: Client, username: String, password: String) extends Client {
  val (name, value) = BasicAuthentication.basicAuthHeader(username, password)
  override def shutdown() = wrappedClient.shutdown
  override def prepare(req: Request) = wrappedClient.prepare(req.copy(headers = req.headers ++ List(Header(name, value))))
}