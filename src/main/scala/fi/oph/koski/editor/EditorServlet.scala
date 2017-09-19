package fi.oph.koski.editor

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.OppijaEditorModel.toEditorModel
import fi.oph.koski.henkilo.HenkilöOid
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.{AccessType, RequiresAuthentication}
import fi.oph.koski.preferences.PreferencesService
import fi.oph.koski.schema._
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.oph.koski.todistus.LocalizedHtml
import fi.oph.koski.validation.ValidationAndResolvingContext
import org.json4s.jackson.Serialization

/**
  *  Endpoints for the Koski UI
  */
class EditorServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with NoCache {
  private val preferencesService = PreferencesService(application.masterDatabase.db)
  private def localization = LocalizedHtml.get(koskiSession, application.localizationRepository)
  get("/:oid") {
    renderEither((params.get("opiskeluoikeus"), getOptionalIntegerParam("versionumero")) match {
      case (Some(opiskeluoikeusOid), Some(versionumero)) =>
        findVersion(params("oid"), opiskeluoikeusOid, versionumero)
      case _ =>
        findByOid(params("oid"))
    })
  }

  get("/omattiedot") {
    renderEither(findByUserOppija)
  }

  get("/koodit/:koodistoUri") {
    getKooditFromRequestParams()
  }

  get("/koodit/:koodistoUri/:koodiarvot") {
    val koodiarvot = params("koodiarvot").split(",").toSet
    getKooditFromRequestParams().filter(v => koodiarvot.contains(v.value))
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
      val models = suoritukset.map { suoritus => OppijaEditorModel.buildModel(suoritus, true)}
      ListModel(models, None, Nil)
    }
    val luokkaAstePattern = """(\d)""".r
    val toimintaAlueittain = params.get("toimintaAlueittain").map(_.toBoolean).getOrElse(false)

    (params("koodistoUri"), params("koodiarvo")) match {
      case ("perusopetuksenluokkaaste", luokkaAstePattern(luokkaAste)) =>
        toListModel(PakollisetOppiaineet(application.koodistoViitePalvelu).pakollistenOppiaineidenTaiToimintaAlueidenSuoritukset(luokkaAste.toInt, toimintaAlueittain))
      case ("koulutus", "201101") =>
        toListModel(PakollisetOppiaineet(application.koodistoViitePalvelu).päättötodistuksenSuoritukset(params("tyyppi"), toimintaAlueittain))
      case _ =>
        logger.error(s"Prefill failed for unexpected code ${params("koodistoUri")}/${params("koodiarvo")}")
        haltWithStatus(KoskiErrorCategory.notFound())
    }
  }

  get("/prototype/:key") {
    val c = ModelBuilderContext(EditorSchema.deserializationContext, true)(koskiSession, application.koodistoViitePalvelu, application.localizationRepository)
    val className = params("key")
    try {
      renderObject(EditorModelBuilder.buildPrototype(className)(c))
    } catch {
      case e: RuntimeException => haltWithStatus(KoskiErrorCategory.notFound())
    }
  }

  get("/preferences/:organisaatioOid/:type") {
    val organisaatioOid = params("organisaatioOid")
    val `type` = params("type")
    renderEither(preferencesService.get(organisaatioOid, `type`).right.map(_.map(OppijaEditorModel.buildModel(_, true))))
  }
  import reflect.runtime.universe.TypeTag

  override def toJsonString[T: TypeTag](x: T): String = Serialization.write(x.asInstanceOf[AnyRef])(Json.jsonFormats + EditorModelSerializer)

  private def getKooditFromRequestParams() = {
    val koodistoUriParts = params("koodistoUri").split(",").toList
    val koodit: List[Koodistokoodiviite] = koodistoUriParts flatMap {part: String =>
      context.koodistoPalvelu.getLatestVersion(part).toList.flatMap(application.koodistoViitePalvelu.getKoodistoKoodiViitteet(_)).flatten
    }

    koodit.map(KoodistoEnumModelBuilder.koodistoEnumValue(localization)(_)).sortBy(_.title)
  }

  private val context: ValidationAndResolvingContext = ValidationAndResolvingContext(application.koodistoViitePalvelu, application.organisaatioRepository)

  private def findByOid(oid: String): Either[HttpStatus, EditorModel] = {
    for {
      oid <- HenkilöOid.validateHenkilöOid(oid).right
      oppija <- application.oppijaFacade.findOppija(oid)
    } yield {
      toEditorModel(oppija, editable = true)
    }
  }

  private def findVersion(oid: String, opiskeluoikeusOid: String, versionumero: Int): Either[HttpStatus, EditorModel] = {
    for {
      oid <- HenkilöOid.validateHenkilöOid(oid).right
      oppija <- application.oppijaFacade.findVersion(oid, opiskeluoikeusOid, versionumero)
    } yield {
      toEditorModel(oppija, editable = false)
    }
  }

  private def findByUserOppija: Either[HttpStatus, EditorModel] = {
    val oppija: Either[HttpStatus, Oppija] = application.oppijaFacade.findUserOppija
    oppija.right.map(oppija => toEditorModel(oppija, editable = false))
  }
}