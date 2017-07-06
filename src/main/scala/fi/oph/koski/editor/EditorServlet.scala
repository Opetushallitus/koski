package fi.oph.koski.editor

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.HenkilöOid
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{AccessType, KoskiSession, RequiresAuthentication}
import fi.oph.koski.preferences.PreferencesService
import fi.oph.koski.schema._
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.oph.koski.todistus.LocalizedHtml
import fi.oph.koski.tutkinto.Koulutustyyppi
import fi.oph.koski.validation.ValidationAndResolvingContext
import fi.oph.scalaschema.{ClassSchema, ExtractionContext}

import scala.collection.immutable

/**
  *  Endpoints for the Koski UI
  */
class EditorServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with NoCache {
  private val preferencesService = PreferencesService(application.masterDatabase.db)
  private def localization = LocalizedHtml.get(koskiSession, application.localizationRepository)
  get("/:oid") {
    renderEither((getOptionalIntegerParam("opiskeluoikeus"), getOptionalIntegerParam("versionumero")) match {
      case (Some(opiskeluoikeusId), Some(versionumero)) =>
        findVersion(params("oid"), opiskeluoikeusId, versionumero, koskiSession)
      case _ =>
        findByOid(params("oid"), koskiSession)
    })
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
    organisaatiot.map(EditorModelBuilder.organisaatioEnumValue(localization)(_))
  }

  get("/oppilaitokset") {
    def organisaatiot = koskiSession.organisationOids(AccessType.write).flatMap(context.organisaatioRepository.getOrganisaatio).toList
    organisaatiot.flatMap(_.toOppilaitos).map(EditorModelBuilder.organisaatioEnumValue(localization)(_))
  }

  get("/organisaatio/:oid/kotipaikka") {
    renderEither(OrganisaatioOid.validateOrganisaatioOid(params("oid")).right.flatMap { oid =>
      application.organisaatioRepository.getOrganisaatio(oid).flatMap(_.kotipaikka) match {
        case None => Left(KoskiErrorCategory.notFound())
        case Some(kotipaikka) => Right(KoodistoEnumModelBuilder.koodistoEnumValue(localization)(kotipaikka))
      }
    })
  }

  get("/suoritukset/prefill/:koodistoUri/:koodiarvo") {
    def toListModel(suoritukset: List[Suoritus]) = {
      val models = suoritukset.map { suoritus => buildModel(suoritus, true)}
      ListModel(models, None, Map.empty)
    }
    val luokkaAstePattern = """(\d)""".r
    val toimintaAlueittain = params.get("toimintaAlueittain").map(_.toBoolean).getOrElse(false)

    (params("koodistoUri"), params("koodiarvo")) match {
      case ("perusopetuksenluokkaaste", luokkaAstePattern(luokkaAste)) =>
        toListModel(PakollisetOppiaineet(application.koodistoViitePalvelu).pakollistenOppiaineidenTaiToimintaAlueidenSuoritukset(luokkaAste.toInt, toimintaAlueittain))
      case ("koulutus", "201101") =>
        toListModel(PakollisetOppiaineet(application.koodistoViitePalvelu).päättötodistuksenSuoritukset(params("tyyppi"), toimintaAlueittain))
      case _ =>
        println((params("koodistoUri") + "/" + params("koodiarvo")))
        haltWithStatus(KoskiErrorCategory.notFound())
    }
  }

  get("/prototype/:key") {
    val c = ModelBuilderContext(EditorSchema.deserializationContext.rootSchema, EditorSchema.deserializationContext, true)(koskiSession, application.koodistoViitePalvelu, application.localizationRepository)
    val className = params("key")
    val prototype = EditorModelBuilder.buildPrototype(className)(c)
    renderOption(KoskiErrorCategory.notFound)(prototype)
  }

  def buildModel(obj: AnyRef, editable: Boolean): EditorModel = {
    EditorModelBuilder.buildModel(EditorSchema.deserializationContext, obj, editable)(koskiSession, application.koodistoViitePalvelu, application.localizationRepository)
  }

  get("/preferences/:organisaatioOid/:type") {
    val organisaatioOid = params("organisaatioOid")
    val `type` = params("type")
    renderEither(preferencesService.get(organisaatioOid, `type`)(koskiSession).right.map(_.map(buildModel(_, true))))
  }

  private def getKoodit() = {
    val koodistoUri = params("koodistoUri")
    val koodit: List[Koodistokoodiviite] = context.koodistoPalvelu.getLatestVersion(koodistoUri).toList.flatMap(application.koodistoViitePalvelu.getKoodistoKoodiViitteet(_)).flatten

    koodit.map(KoodistoEnumModelBuilder.koodistoEnumValue(localization)(_)).sortBy(_.title)
  }

  private val context: ValidationAndResolvingContext = ValidationAndResolvingContext(application.koodistoViitePalvelu, application.organisaatioRepository)

  private def findByOid(oid: String, user: KoskiSession): Either[HttpStatus, EditorModel] = {
    HenkilöOid.validateHenkilöOid(oid).right.flatMap { oid =>
      toEditorModel(application.oppijaFacade.findOppija(oid)(user), editable = true) // <- note: editability will be checked based on organizational access
    }
  }

  private def findVersion(oid: String, opiskeluoikeusId: Int, versionumero: Int, user: KoskiSession): Either[HttpStatus, EditorModel] = {
    HenkilöOid.validateHenkilöOid(oid).right.flatMap { oid =>
      toEditorModel(application.oppijaFacade.findVersion(oid, opiskeluoikeusId, versionumero)(user), editable = false)
    }
  }

  private def findByUserOppija(user: KoskiSession): Either[HttpStatus, EditorModel] = {
    toEditorModel(application.oppijaFacade.findUserOppija(user), editable = false)
  }

  private def toEditorModel(oppija: Either[HttpStatus, Oppija], editable: Boolean): Either[HttpStatus, EditorModel] = {
    implicit val opiskeluoikeusOrdering = new Ordering[Option[LocalDate]] {
      override def compare(x: Option[LocalDate], y: Option[LocalDate]) = (x, y) match {
        case (None, Some(_)) => 1
        case (Some(_), None) => -1
        case (None, None) => 0
        case (Some(x), Some(y)) => if (x.isBefore(y)) { -1 } else { 1 }
      }
    }

    oppija.right.map{oppija =>
      val tyypit = oppija.opiskeluoikeudet.groupBy(_.tyyppi).map {
        case (tyyppi, opiskeluoikeudet) =>
          val oppilaitokset = opiskeluoikeudet.groupBy(_.getOppilaitos).map {
            case (oppilaitos, opiskeluoikeudet) =>
              OppilaitoksenOpiskeluoikeudet(oppilaitos, opiskeluoikeudet.toList.sortBy(_.alkamispäivä).map {
                case oo: PerusopetuksenOpiskeluoikeus => oo.copy(suoritukset = oo.suoritukset.sortBy(perusopetuksenSuoritustenJärjestysKriteeri))
                case oo: AmmatillinenOpiskeluoikeus => oo.copy(suoritukset = oo.suoritukset.sortBy(_.alkamispäivä).reverse)
                case oo: Any => oo
              })
          }.toList.sortBy(_.opiskeluoikeudet(0).alkamispäivä)
          OpiskeluoikeudetTyypeittäin(tyyppi, oppilaitokset)
      }.toList.sortBy(_.opiskeluoikeudet(0).opiskeluoikeudet(0).alkamispäivä).reverse
      val editorView = OppijaEditorView(oppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot], tyypit)
      buildModel(editorView, editable)
    }
  }

  def perusopetuksenSuoritustenJärjestysKriteeri(s: PerusopetuksenPäätasonSuoritus) = {
    val primary = s match {
      case s: PerusopetuksenOppimääränSuoritus => -100 // ensin oppimäärän suoritus
      case s: PerusopetuksenOppiaineenOppimääränSuoritus => 0 // oppiaineiden oppimäärien suoritukset
      case s: PerusopetuksenVuosiluokanSuoritus => - s.koulutusmoduuli.tunniste.koodiarvo.toInt // sitten luokka-asteet
      case _ => 100
    }
    val secondary = s.valmis
    (primary, secondary)
  }
}


object EditorSchema {
  lazy val schema = KoskiSchema.schemaFactory.createSchema(classOf[OppijaEditorView].getName).asInstanceOf[ClassSchema].moveDefinitionsToTopLevel
  lazy val deserializationContext = ExtractionContext(schema, validate = false)
}

case class OppijaEditorView(
  @Hidden
  henkilö: TäydellisetHenkilötiedot,
  opiskeluoikeudet: List[OpiskeluoikeudetTyypeittäin]
)

case class OpiskeluoikeudetTyypeittäin(@KoodistoUri("opiskeluoikeudentyyppi") tyyppi: Koodistokoodiviite, opiskeluoikeudet: List[OppilaitoksenOpiskeluoikeudet])
case class OppilaitoksenOpiskeluoikeudet(oppilaitos: Oppilaitos, opiskeluoikeudet: List[Opiskeluoikeus])

