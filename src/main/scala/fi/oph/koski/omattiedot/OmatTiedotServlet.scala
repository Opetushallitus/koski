package fi.oph.koski.omattiedot

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.{EditorModel, EditorModelSerializer}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.LegacyJsonSerialization
import fi.oph.koski.koskiuser.RequiresKansalainen
import fi.oph.koski.schema.Oppija
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.oph.koski.util.WithWarnings
import org.json4s.jackson.Serialization

/**
  *  Endpoints for the Koski omattiedot UI
  */
class OmatTiedotServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresKansalainen with NoCache {
  private val huoltajaService = application.huoltajaService

  get("/editor") {
    renderOmatTiedot
  }

  get("/editor/:huollettavanOid") {
    renderHuollettavanTiedot(params("huollettavanOid"))
  }

  private def renderOmatTiedot: Unit = {
    val käyttäjäOppija = huoltajaService.findUserOppijaAllowEmpty(koskiSession)
    val editorModel = käyttäjäOppija.map(OmatTiedotEditorModel.toEditorModel(_, None))

    renderEither[EditorModel](editorModel)
  }

  private def renderHuollettavanTiedot(oid: String): Unit = {
    val käyttäjäOppija = huoltajaService.findUserOppijaAllowEmpty(koskiSession)
    var haluttuOppija: Option[WithWarnings[Oppija]] = None;

    if (oid != koskiSession.oid) {
      haluttuOppija = huoltajaService.findHuollettavaOppija(oid)(koskiSession) match {
        case Left(status) => None
        case Right(oppija) => Some(oppija)
      }
    }
    val editorModel = käyttäjäOppija.map(OmatTiedotEditorModel.toEditorModel(_, haluttuOppija))
    renderEither[EditorModel](editorModel)
  }

  import reflect.runtime.universe.TypeTag
  override def toJsonString[T: TypeTag](x: T): String = Serialization.write(x.asInstanceOf[AnyRef])(LegacyJsonSerialization.jsonFormats + EditorModelSerializer)
}
