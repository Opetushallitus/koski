package tor

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.{Http, HttpRequestBuilder}
import tor.Scenarios.findOppija
import scala.concurrent.duration._


class TorSimulation extends Simulation {
  val httpConf = http
    .baseURL("http://tordev.tor.oph.reaktor.fi/tor")
    .acceptEncodingHeader("gzip, deflate")

  val headers = Map("Content-Type" -> """application/x-www-form-urlencoded""")


  println(Scenarios.username, Scenarios.password)
  setUp(findOppija.inject(atOnceUsers(1)).protocols(httpConf))
}

object Scenarios {

  val username = sys.env("TOR_USER")
  val password = sys.env("TOR_PASS")

  val findOppija = scenario("Find oppija").exec(http("/api/oppija/:oid").get("/api/oppija/1.2.246.562.24.10031632987").basicAuth(username, password))
}
