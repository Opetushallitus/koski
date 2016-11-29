package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.OpiskeluOikeusRow
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class OpiskeluoikeusServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with Logging with NoCache {
  get("/:id") {
    val result: Option[OpiskeluOikeusRow] = application.opiskeluOikeusRepository.findById(getIntegerParam("id"))(koskiSession)
    renderEither(result match {
      case Some(oo) => Right(oo.toOpiskeluOikeus)
      case _ => Left(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
    })
  }
}