package fi.oph.koski.editor

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.OppijaEditorModel.toEditorModel
import fi.oph.koski.henkilo.HenkilöOid
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{AccessType, RequiresVirkailijaOrPalvelukäyttäjä}
import fi.oph.koski.organisaatio.OrganisaatioOid
import fi.oph.koski.preferences.PreferencesService
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.util.WithWarnings
import fi.oph.koski.validation.ValidationAndResolvingContext

/**
  *  Endpoints for the Koski UI
  */
class EditorServlet(implicit val application: KoskiApplication) extends EditorApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with NoCache {
  private val preferencesService = PreferencesService(application.masterDatabase.db)
  private def localization = LocalizedHtml.get(session, application.koskiLocalizationRepository)
  get("/:oid") {
    renderEither[EditorModel]((params.get("opiskeluoikeus"), getOptionalIntegerParam("versionumero")) match {
      case (Some(opiskeluoikeusOid), Some(versionumero)) =>
        findVersion(params("oid"), opiskeluoikeusOid, versionumero)
      case _ =>
        findByHenkilöOid(params("oid"))
    })
  }

  get[List[EnumValue]]("/organisaatiot") {
    def organisaatiot = session.organisationOids(AccessType.write).flatMap(context.organisaatioRepository.getOrganisaatio).toList
    organisaatiot.map(EditorModelBuilder.organisaatioEnumValue(localization)(_))
  }

  get[List[EnumValue]]("/oppilaitokset") {
    def organisaatiot = session.organisationOids(AccessType.write).flatMap(context.organisaatioRepository.getOrganisaatio).toList
    organisaatiot.flatMap(_.toOppilaitos).map(EditorModelBuilder.organisaatioEnumValue(localization)(_))
  }

  get("/organisaatio/:oid/kotipaikka") {
    renderEither[EnumValue](OrganisaatioOid.validateOrganisaatioOid(params("oid")).right.flatMap { oid =>
      application.organisaatioRepository.getOrganisaatio(oid).flatMap(_.kotipaikka) match {
        case None => Left(KoskiErrorCategory.notFound())
        case Some(kotipaikka) => Right(KoodistoEnumModelBuilder.koodistoEnumValue(kotipaikka)(localization, application.koodistoViitePalvelu))
      }
    })
  }

  get("/prototype/:key") {
    val c = ModelBuilderContext(EditorSchema.deserializationContext, editable = true, invalidatable = true)(session, application.koodistoViitePalvelu, application.koskiLocalizationRepository)
    val className = params("key")
    try {
      renderObject[EditorModel](EditorModelBuilder.buildPrototype(className)(c))
    } catch {
      case e: RuntimeException => haltWithStatus(KoskiErrorCategory.notFound())
    }
  }

  get("/preferences/:organisaatioOid/:type") {
    val organisaatioOid = params("organisaatioOid")
    val `type` = params("type")
    renderEither[List[EditorModel]](preferencesService.get(organisaatioOid, params.get("koulutustoimijaOid"), `type`).right.map(_.map(OppijaEditorModel.buildModel(_, true))))
  }

  private val context: ValidationAndResolvingContext = ValidationAndResolvingContext(application.koodistoViitePalvelu, application.organisaatioRepository)

  private def findByHenkilöOid(oid: String): Either[HttpStatus, EditorModel] = {
    for {
      oid <- HenkilöOid.validateHenkilöOid(oid).right
      oppija <- application.oppijaFacade.findOppijaHenkilö(oid, findMasterIfSlaveOid = true)
    } yield {
      toEditorModel(oppija, editable = true)
    }
  }

  private def findVersion(henkilöOid: String, opiskeluoikeusOid: String, versionumero: Int): Either[HttpStatus, EditorModel] = {
    for {
      oid <- HenkilöOid.validateHenkilöOid(henkilöOid).right
      oppija <- application.oppijaFacade.findVersion(oid, opiskeluoikeusOid, versionumero)
    } yield {
      toEditorModel(WithWarnings(oppija, Nil), editable = false)
    }
  }
}
