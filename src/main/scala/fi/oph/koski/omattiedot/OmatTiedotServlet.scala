package fi.oph.koski.omattiedot

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.{EditorModel, EditorModelSerializer}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.LegacyJsonSerialization
import fi.oph.koski.koskiuser.RequiresKansalainen
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import org.json4s.jackson.Serialization

/**
  *  Endpoints for the Koski omattiedot UI
  */
class OmatTiedotServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresKansalainen with NoCache {
  private val huoltajaService = application.huoltajaService

  get("/editor") {
    renderOmatTiedot
  }

  get("/editor/:valtuutusKoodi") {
    renderHuollettava(params("valtuutusKoodi"))
  }

  private def renderOmatTiedot: Unit = {
    val editorModel = huoltajaService.findUserOppijaAllowEmpty.map(OmatTiedotEditorModel.toEditorModel(_, None))
    renderEither[EditorModel](editorModel)
  }

  private def renderHuollettava(valtuutusKoodi: String): Unit = {
    renderEither[EditorModel](mkEditorModel(valtuutusKoodi))
  }

  private def mkEditorModel(valtuutusKoodi: String): Either[HttpStatus, EditorModel] = {
    val (huoltajaWarnings, huollettava) = huoltajaService.findHuollettavaOppija(valtuutusKoodi, koskiRoot) match {
      case Left(status) => (status, None)
      case Right(oppija) => (Nil, oppija)
    }

    huoltajaService.findUserOppijaNoAuditLog.map { userOppija =>
      // copy errors from huollettava to useroppija warnings
      userOppija.copy(warnings = userOppija.warnings ++ huoltajaWarnings)
    }.map(OmatTiedotEditorModel.toEditorModel(_, huollettava))
  }

  import reflect.runtime.universe.TypeTag
  override def toJsonString[T: TypeTag](x: T): String = Serialization.write(x.asInstanceOf[AnyRef])(LegacyJsonSerialization.jsonFormats + EditorModelSerializer)
}

