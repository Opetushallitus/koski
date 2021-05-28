package fi.oph.koski.servlet

import fi.oph.koski.koskiuser.Unauthenticated

import java.time.ZonedDateTime

class StatusApiServlet extends KoskiSpecificApiServlet with NoCache with Unauthenticated {
  get("/") {
    render(Map(
      "server time" -> ZonedDateTime.now()
    ))
  }
}
