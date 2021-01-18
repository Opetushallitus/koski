package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresSession
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class ValpasApiServlet(implicit val application: KoskiApplication) extends ApiServlet with NoCache with RequiresSession {
  get("/hello") {
    "Hello world!"
  }

  get("/user") {
    koskiSession.user
  }
}
