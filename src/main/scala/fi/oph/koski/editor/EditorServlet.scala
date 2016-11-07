package fi.oph.koski.editor

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.HenkiloOid
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koski.ValidationAndResolvingContext
import fi.oph.koski.koskiuser.{AccessType, KoskiSession, RequiresAuthentication}
import fi.oph.koski.schema._
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.oph.scalaschema.ClassSchema

class EditorServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with NoCache {
  get("/:oid") {
    renderEither(findByOid(params("oid"), koskiSession))
  }

  get("/omattiedot") {
    renderEither(findByUserOppija(koskiSession))
  }

  get("/koodit/:koodistoUri") {
    getKoodit()
  }

  get("/koodit/:koodistoUri/:koodiarvot") {
    val koodiarvot = params("koodiarvot").split(",").toSet
    getKoodit().filter(v => koodiarvot.contains(v.value))
  }

  get("/organisaatiot") {
    def organisaatiot = koskiSession.organisationOids(AccessType.write).flatMap(context.organisaatioRepository.getOrganisaatio).toList
    organisaatiot.map(modelBuilder.organisaatioEnumValue(_))
  }

  get("/oppilaitokset") {
    def organisaatiot = koskiSession.organisationOids(AccessType.write).flatMap(context.organisaatioRepository.getOrganisaatio).toList
    organisaatiot.flatMap(_.toOppilaitos).map(modelBuilder.organisaatioEnumValue(_))
  }

  private def getKoodit() = {
    val koodistoUri = params("koodistoUri")
    val koodit: List[Koodistokoodiviite] = context.koodistoPalvelu.getLatestVersion(koodistoUri).toList.flatMap(application.koodistoViitePalvelu.getKoodistoKoodiViitteet(_)).flatten

    koodit.map(modelBuilder.koodistoEnumValue(_)).sortBy(_.title)
  }

  private val context: ValidationAndResolvingContext = ValidationAndResolvingContext(application.koodistoViitePalvelu, application.organisaatioRepository)
  private def modelBuilder = new EditorModelBuilder(context, EditorSchema.schema)(koskiSession)

  private def findByOid(oid: String, user: KoskiSession): Either[HttpStatus, EditorModel] = {
    HenkiloOid.validateHenkilöOid(oid).right.flatMap { oid =>
      toEditorModel(application.facade.findOppija(oid)(user))
    }
  }

  private def findByUserOppija(user: KoskiSession): Either[HttpStatus, EditorModel] = {
    toEditorModel(application.facade.findUserOppija(user))
  }

  private def toEditorModel(oppija: Either[HttpStatus, Oppija]): Either[HttpStatus, EditorModel] = {
    implicit val opiskeluoikeusOrdering = new Ordering[Option[LocalDate]] {
      override def compare(x: Option[LocalDate], y: Option[LocalDate]) = (x, y) match {
        case (None, Some(_)) => 1
        case (Some(_), None) => -1
        case (None, None) => 0
        case (Some(x), Some(y)) => if (x.isBefore(y)) { -1 } else { 1 }
      }
    }

    oppija.right.map{oppija =>
      val oppilaitokset = oppija.opiskeluoikeudet.groupBy(_.oppilaitos).map {
        case (oppilaitos, opiskeluoikeudet) => OppilaitoksenOpiskeluoikeudet(oppilaitos, opiskeluoikeudet.toList.sortBy(_.alkamispäivä))
      }.toList.sortBy(_.opiskeluoikeudet(0).alkamispäivä)
      val editorView = OppijaEditorView(oppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot], oppilaitokset)

      timed("buildModel") { modelBuilder.buildModel(EditorSchema.schema, editorView)}
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