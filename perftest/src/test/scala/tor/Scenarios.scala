package tor

import java.time.LocalDate
import com.fasterxml.jackson.databind.ObjectMapper
import com.ning.http.client.RequestBuilder
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.Body

object Scenarios {
  val username = sys.env("TOR_USER")
  val password = sys.env("TOR_PASS")

  val findOppija = scenario("Find oppija").exec(
    http("find by oid")
      .get("/api/oppija/1.2.246.562.24.00000000001")
      .basicAuth(username, password)
  )

  val updateOppija = scenario("Update oppija")
        .feed(jsonFile("src/test/resources/bodies/oppija.json").circular)
        .exec(
          http("update")
            .put("/api/oppija")
            .body(OppijaWithOpiskeluoikeusWithIncrementingStartdate).asJSON
            .basicAuth(username, password).check(status.in(200, 409))
        )



  val queryOppijat = scenario("Query oppijat").exec(
    http("query oppijat")
      .get("/api/oppija?opiskeluoikeusPäättynytAikaisintaan=2016-01-10&opiskeluoikeusPäättynytViimeistään=2016-01-10")
      .basicAuth(username, password)
  )
}

object OppijaWithOpiskeluoikeusWithIncrementingStartdate extends Body {
  var dateCounter = LocalDate.parse("2012-09-01")
  type Map = java.util.LinkedHashMap[Any, Any]
  type Array = java.util.List[Any]

  private def nextDate = this.synchronized {
    dateCounter = dateCounter.plusDays(1)
    dateCounter.toString
  }

  override def setBody(req: RequestBuilder, session: Session) = {
    val contentMap = session.apply("content").as[Map]
    contentMap.get("opiskeluoikeudet").asInstanceOf[Array].get(0).asInstanceOf[Map].put("alkamispäivä", nextDate)
    val contentBytes = new ObjectMapper().writeValueAsBytes(contentMap)
    req.setBody(contentBytes)
  }
}