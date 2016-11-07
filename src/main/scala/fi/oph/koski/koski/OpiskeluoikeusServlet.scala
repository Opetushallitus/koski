package fi.oph.koski.koski

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.OpiskeluOikeusRow
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Opiskeluoikeus
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class OpiskeluoikeusServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with Logging with NoCache {
  get("/:id") {
    val findResult: Either[HttpStatus, OpiskeluOikeusRow] = application.facade.findOpiskeluOikeus(getIntegerParam("id"))(koskiSession)
    val result: Either[HttpStatus, Opiskeluoikeus] = findResult
      .right.map(_.toOpiskeluOikeus)
    renderEither(result)
  }
}