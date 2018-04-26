package fi.oph.koski.omattiedot

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.{EditorModel, EditorModelSerializer}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.LegacyJsonSerialization
import fi.oph.koski.koskiuser.RequiresKansalainen
import fi.oph.koski.schema._
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.oph.koski.util.WithWarnings
import org.json4s.jackson.Serialization

/**
  *  Endpoints for the Koski omattiedot UI
  */
class OmatTiedotServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresKansalainen with NoCache {

  get("/editor") {
    val oppija: Either[HttpStatus, WithWarnings[Oppija]] = application.oppijaFacade.findUserOppija
    renderEither[EditorModel](oppija.right.map { o =>
      OmatTiedotEditorModel.toEditorModel(o)
    })
  }

  import reflect.runtime.universe.TypeTag
  override def toJsonString[T: TypeTag](x: T): String = Serialization.write(x.asInstanceOf[AnyRef])(LegacyJsonSerialization.jsonFormats + EditorModelSerializer)
}

