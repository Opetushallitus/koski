package tor

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Scenarios {

  val username = sys.env("TOR_USER")
  val password = sys.env("TOR_PASS")

  val findOppija = scenario("Find oppija").exec(
    http("find by oid")
      .get("/api/oppija/1.2.246.562.24.00000000001")
      .basicAuth(username, password)
  )

  val updateOppija = scenario("Update oppija").exec(
    http("update")
      .put("/api/oppija")
      .body(RawFileBody("oppija.json")).asJSON
      .basicAuth(username, password)
  )

  val queryOppijat = scenario("Query oppijat").exec(
    http("query oppijat")
      .get("/api/oppija?opiskeluoikeusPäättynytAikaisintaan=2016-01-10&opiskeluoikeusPäättynytViimeistään=2016-01-10")
      .basicAuth(username, password)
  )

}
