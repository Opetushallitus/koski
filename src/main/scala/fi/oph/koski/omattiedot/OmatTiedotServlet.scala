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

  get("/editor/:oid") {
    val oid = params("oid")
    if (oid == koskiSession.user.oid) {
      renderOmatTiedot
    } else {
      renderHuollettavanTiedot(oid)
    }
  }

  private def renderOmatTiedot: Unit = {
    val käyttäjäOppija = huoltajaService.findUserOppijaAllowEmpty(koskiSession)
    val editorModel = käyttäjäOppija.map(oppija => OmatTiedotEditorModel.toEditorModel(userOppija = oppija, näytettäväOppija = oppija))

    renderEither[EditorModel](editorModel)
  }

  private def renderHuollettavanTiedot(oid: String): Unit = {
    val huoltajaOppija: Either[HttpStatus, WithWarnings[Oppija]] = huoltajaService.findUserOppijaAllowEmpty(koskiSession)
    val huollettavaOppija: Either[HttpStatus, WithWarnings[Oppija]] = huoltajaService.findHuollettavaOppija(oid)(koskiSession)

    val editorModel = huoltajaOppija.flatMap(huoltaja => huollettavaOppija.map(huollettava => (huoltaja, huollettava))).map {
      case (huoltaja, huollettava) => OmatTiedotEditorModel.toEditorModel(userOppija = huoltaja, näytettäväOppija = huollettava)
    }
    renderEither[EditorModel](editorModel)
  }

  import reflect.runtime.universe.TypeTag
  override def toJsonString[T: TypeTag](x: T): String = Serialization.write(x.asInstanceOf[AnyRef])(LegacyJsonSerialization.jsonFormats + EditorModelSerializer)
}
