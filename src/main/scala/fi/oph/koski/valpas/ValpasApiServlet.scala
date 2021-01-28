package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.oph.koski.valpas.valpasuser.RequiresValpasSession

class ValpasApiServlet(implicit val application: KoskiApplication) extends ApiServlet with NoCache with RequiresValpasSession {
  get("/user") {
    valpasSession.user
  }
}
