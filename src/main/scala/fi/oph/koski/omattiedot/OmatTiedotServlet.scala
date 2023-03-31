package fi.oph.koski.omattiedot

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.{EditorApiServlet, EditorModel}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.RequiresKansalainen
import fi.oph.koski.schema.Oppija
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.util.WithWarnings

/**
  *  Endpoints for the Koski omattiedot UI
  */
class OmatTiedotServlet(implicit val application: KoskiApplication) extends EditorApiServlet with RequiresKansalainen with NoCache {
  private val huoltajaService = application.huoltajaService

  get("/editor") {
    renderOmatTiedot
  }

  get("/editor/:oid") {
    val oid = params("oid")
    if (oid == session.user.oid) {
      renderOmatTiedot
    } else {
      renderHuollettavanTiedot(oid)
    }
  }
  
  private def renderOmatTiedot: Unit = {
    val käyttäjäOppija = huoltajaService.findUserOppijaAllowEmpty(session)
    val editorModel = käyttäjäOppija.map(oppija => OmatTiedotEditorModel.toEditorModel(userOppija = oppija, näytettäväOppija = oppija))

    renderEither[EditorModel](editorModel)
  }

  private def renderHuollettavanTiedot(oid: String): Unit = {
    if (!session.isUsersHuollettava(oid)) {
      haltWithStatus(KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus())
    }

    val huoltajaOppija: Either[HttpStatus, WithWarnings[Oppija]] = huoltajaService.findUserOppijaAllowEmpty(session)
    val huollettavaOppija: Either[HttpStatus, WithWarnings[Oppija]] = huoltajaService.findHuollettavaOppija(oid)(session)

    val editorModel = huoltajaOppija.flatMap(huoltaja => huollettavaOppija.map(huollettava => (huoltaja, huollettava))).map {
      case (huoltaja, huollettava) => OmatTiedotEditorModel.toEditorModel(userOppija = huoltaja, näytettäväOppija = huollettava)
    }
    renderEither[EditorModel](editorModel)
  }
}
