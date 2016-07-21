package fi.oph.koski.editor

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
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

  get("/koodit/:koodistoUri") {
    val koodistoUri = params("koodistoUri")
    val koodit: List[Koodistokoodiviite] = context.koodistoPalvelu.getLatestVersion(koodistoUri).toList.flatMap(application.koodistoViitePalvelu.getKoodistoKoodiViitteet(_)).flatten
    koodit.map(modelCreator.koodistoEnumValue(_))
  }

  get("/organisaatiot") {
    def organisaatiot = koskiUser.organisationOids(AccessType.write).flatMap(context.organisaatioRepository.getOrganisaatio).toList
    organisaatiot.map(modelCreator.organisaatioEnumValue(_))
  }

  get("/oppilaitokset") {
    def organisaatiot = koskiUser.organisationOids(AccessType.write).flatMap(context.organisaatioRepository.getOrganisaatio).toList
    organisaatiot.flatMap(_.toOppilaitos).map(modelCreator.organisaatioEnumValue(_))
  }

  private val context: ValidationAndResolvingContext = ValidationAndResolvingContext(application.koodistoViitePalvelu, application.organisaatioRepository)
  private def modelCreator = new EditorModelBuilder(context, EditorSchema.schema)(koskiUser)

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