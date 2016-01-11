package tor

import io.gatling.core.Predef._
import io.gatling.http.Predef._

trait TorSimulation extends Simulation {
  val baseUrl = sys.env.getOrElse("TOR_BASE_URL", "http://tordev.tor.oph.reaktor.fi/tor")

  val httpConf = http.baseURL(baseUrl).acceptEncodingHeader("gzip, deflate")

  val headers = Map("Content-Type" -> "application/json")

}
