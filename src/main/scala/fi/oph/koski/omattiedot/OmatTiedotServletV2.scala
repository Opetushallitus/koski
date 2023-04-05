package fi.oph.koski.omattiedot

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.{EditorApiServlet, EditorModel}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.RequiresKansalainen
import fi.oph.koski.schema.Oppija
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.util.WithWarnings

/**
  *  Endpoints for the Koski omattiedot UI
  */
class OmatTiedotServletV2(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with RequiresKansalainen with NoCache {
  private val huoltajaService = application.huoltajaService

  get("/oppija") {
    renderEither[Oppija](
      huoltajaService
        .findUserOppijaAllowEmpty(session)
        .map(_.map(OmatTiedotEditorModel.piilotetuillaTiedoilla))
        .map(_.getIgnoringWarnings)
    )
  }
}
