package fi.oph.koski.editor

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.documentation.SchemaToEditorModel
import fi.oph.koski.henkilo.HenkiloOid
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koski.ValidationAndResolvingContext
import fi.oph.koski.koskiuser.{AccessType, KoskiUser, RequiresAuthentication}
import fi.oph.koski.schema._
import fi.oph.koski.servlet.ApiServlet
import fi.oph.scalaschema.ClassSchema

class EditorServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication {
  get("/:oid") {
    renderEither(findByOid(params("oid"), koskiUser))
  }

  private def findByOid(oid: String, user: KoskiUser): Either[HttpStatus, EditorModel] = {
    implicit val opiskeluoikeusOrdering = new Ordering[Option[LocalDate]] {
      override def compare(x: Option[LocalDate], y: Option[LocalDate]) = (x, y) match {
        case (None, Some(_)) => 1
        case (Some(_), None) => -1
        case (None, None) => 0
        case (Some(x), Some(y)) => if (x.isBefore(y)) { -1 } else { 1 }
      }
    }
    HenkiloOid.validateHenkilöOid(oid).right.flatMap { oid =>
      application.facade.findOppija(oid)(user).right.map{oppija =>
        val oppilaitokset = oppija.opiskeluoikeudet.groupBy(_.oppilaitos).map {
          case (oppilaitos, opiskeluoikeudet) => OppilaitoksenOpiskeluoikeudet(oppilaitos, opiskeluoikeudet.toList.sortBy(_.alkamispäivä))
        }.toList.sortBy(_.opiskeluoikeudet(0).alkamispäivä)
        val editorView = OppijaEditorView(oppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot], oppilaitokset)
        val context: ValidationAndResolvingContext = ValidationAndResolvingContext(application.koodistoViitePalvelu, application.organisaatioRepository)

        val modelCreator: SchemaToEditorModel = new SchemaToEditorModel(context, EditorSchema.schema)(koskiUser)
        timed("buildModel") { modelCreator.buildModel(EditorSchema.schema, editorView)}
      }
    }
  }
}

object EditorSchema {
  lazy val schema = KoskiSchema.schemaFactory.createSchema(classOf[OppijaEditorView].getName).asInstanceOf[ClassSchema].moveDefinitionsToTopLevel
}

case class OppijaEditorView(
  @Hidden
  henkilö: TäydellisetHenkilötiedot,
  opiskeluoikeudet: List[OppilaitoksenOpiskeluoikeudet]
)
case class OppilaitoksenOpiskeluoikeudet(oppilaitos: Oppilaitos, opiskeluoikeudet: List[Opiskeluoikeus])