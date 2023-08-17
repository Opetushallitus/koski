package fi.oph.koski.editor

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.OppijaEditorModel.toEditorModel
import fi.oph.koski.henkilo.HenkilöOid
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{AccessType, RequiresVirkailijaOrPalvelukäyttäjä}
import fi.oph.koski.organisaatio.OrganisaatioOid
import fi.oph.koski.preferences.PreferencesService
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema.{PäätasonSuoritus, SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta}
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.util.WithWarnings

/**
  *  Endpoints for the Koski UI
  */
class   EditorServlet(implicit val application: KoskiApplication)
  extends EditorApiServlet
    with RequiresVirkailijaOrPalvelukäyttäjä
    with NoCache {

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

  get[Set[EnumValue]]("/organisaatiot") {
    val localizationResult = localization
    val organisaatiot = session.organisationOids(AccessType.write)
      .flatMap(application.organisaatioRepository.getOrganisaatio)
    organisaatiot.map(EditorModelBuilder.organisaatioEnumValue(localizationResult)(_))
  }

  get[Set[EnumValue]]("/oppilaitokset") {
    val localizationResult = localization
    val organisaatiot = session.organisationOids(AccessType.write)
      .flatMap(application.organisaatioRepository.getOrganisaatio)
    organisaatiot.flatMap(_.toOppilaitos).map(EditorModelBuilder.organisaatioEnumValue(localizationResult)(_))
  }

  get("/organisaatio/:oid/kotipaikka") {
    val localizationResult = localization
    renderEither[EnumValue](OrganisaatioOid.validateOrganisaatioOid(params("oid")).right.flatMap { oid =>
      application.organisaatioRepository.getOrganisaatio(oid).flatMap(_.kotipaikka) match {
        case None => Left(KoskiErrorCategory.notFound())
        case Some(kotipaikka) => Right(
          KoodistoEnumModelBuilder.koodistoEnumValue(kotipaikka)(localizationResult, application.koodistoViitePalvelu)
        )
      }
    })
  }

  get("/prototype/:key") {
    val c = ModelBuilderContext(
      EditorSchema.deserializationContext,
      editable = true,
      invalidatable = true
    )(
      session,
      application.koodistoViitePalvelu,
      application.koskiLocalizationRepository
    )
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
    renderEither[Seq[EditorModel]](preferencesService.get(organisaatioOid, params.get("koulutustoimijaOid"), `type`)
      .right.map(_.map(OppijaEditorModel.buildModel(_, true))))
  }

  post("/check-vaatiiko-suoritus-maksuttomuus-tiedon") {
    withJsonBody { body =>
      render[Boolean](
        application.validatingAndResolvingExtractor.extract[PäätasonSuoritus](strictDeserialization)(body)
        .map(_.isInstanceOf[SuoritusVaatiiMahdollisestiMaksuttomuusTiedonOpiskeluoikeudelta])
        .getOrElse(false)
      )
    }()
  }

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
