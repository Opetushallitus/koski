package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.valpas.servlet.ValpasApiServlet
import fi.oph.koski.valpas.valpasuser.{RequiresValpasKansalainenSession, RequiresValpasSession}

class ValpasKansalainenApiServlet(implicit val application: KoskiApplication) extends ValpasApiServlet with NoCache with RequiresValpasKansalainenSession {

  get("/user") {
    session.user
  }

}
