package fi.oph.koski.valvira

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresValvira
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}

class ValviraServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with NoCache with RequiresValvira {

  private val valviraService = new ValviraService(application)

  get("/:hetu") {
    renderEither(valviraService.getOppijaByHetu(params("hetu")))
  }
}
