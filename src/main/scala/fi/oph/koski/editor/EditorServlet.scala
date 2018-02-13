package fi.oph.koski.editor

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.OppijaEditorModel.toEditorModel
import fi.oph.koski.henkilo.HenkilöOid
import fi.oph.koski.http.KoskiErrorCategory.badRequest.validation.koodisto.tuntematonKoodi
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.LegacyJsonSerialization
import fi.oph.koski.koodisto.KoodistoViite
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
        findByHenkilöOid(params("oid"))
    })
  }

  get("/omattiedot") {
    renderEither(findByUserOppija)
  }

  get("/koodit/:koodistoUri") {
    toKoodistoEnumValues(getKooditFromRequestParams())
  }

  get("/koodit/:koodistoUri/:koodiarvot") {
    val koodiarvot = params("koodiarvot").split(",").toSet
    toKoodistoEnumValues(getKooditFromRequestParams().filter(k => koodiarvot.contains(k.koodiarvo)))
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
        case Some(kotipaikka) => Right(KoodistoEnumModelBuilder.koodistoEnumValue(kotipaikka)(localization, application.koodistoViitePalvelu))
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
    val c = ModelBuilderContext(EditorSchema.deserializationContext, editable = true, invalidatable = true)(koskiSession, application.koodistoViitePalvelu, application.localizationRepository)
    val className = params("key")
    try {
      renderObject(EditorModelBuilder.buildPrototype(className)(c))
    } catch {
      case e: RuntimeException => haltWithStatus(KoskiErrorCategory.notFound())
    }
  }

  get("/kurssit/:oppiaineKoodisto/:oppiaineKoodiarvo/:kurssiKoodistot") {
    val kurssiKoodistot: List[KoodistoViite] = koodistotByString(params("kurssiKoodistot"))
    def sisältyvätKurssit(parentKoodistoUri: String, parentKoodiarvo: String) = {
      val parent = application.koodistoViitePalvelu.getKoodistoKoodiViite(parentKoodistoUri, parentKoodiarvo).getOrElse(haltWithStatus(tuntematonKoodi(s"Koodistosta ${parentKoodistoUri} ei löydy koodia ${parentKoodiarvo}")))
      for {
        kurssiKoodisto <- kurssiKoodistot
        kurssiKoodi <- application.koodistoViitePalvelu.getSisältyvätKoodiViitteet(kurssiKoodisto, parent).toList.flatten
      } yield {
        kurssiKoodi
      }
    }

    val oppiaineKoodisto = params("oppiaineKoodisto")
    val oppiaineKoodiarvo = params("oppiaineKoodiarvo")

    val oppimaaraKoodisto = params.get("oppimaaraKoodisto") // vieraan kielen, äidinkielen ja matematiikan kursseille
    val oppimaaraKoodiarvo = params.get("oppimaaraKoodiarvo")
    val oppimääränDiaarinumero = params.get("oppimaaraDiaarinumero")

    val ePerusteetRakenne = oppimääränDiaarinumero.flatMap(application.ePerusteet.findRakenne)

    def ePerusteidenMukaisetKurssit = {
      val oppiaine = for {
        rakenne <- ePerusteetRakenne
        lukiokoulutus <- rakenne.lukiokoulutus
        aine <- lukiokoulutus.rakenne.oppiaineet.find(_.koodiArvo == params("oppiaineKoodiarvo"))
      } yield aine

      val oppiaineenKurssit = oppiaine.map(_.kurssit).getOrElse(List())

      val oppimääränKurssit = for {
        aine <- oppiaine
        koodiarvo <- oppimaaraKoodiarvo
        oppimaara <- aine.oppimaarat.find(_.koodiArvo == koodiarvo)
      } yield oppimaara.kurssit

      oppiaineenKurssit ++ oppimääränKurssit.getOrElse(List())
    }

    val oppiaineeseenSisältyvätKurssit = sisältyvätKurssit(oppiaineKoodisto, oppiaineKoodiarvo)
    val oppiaineeseenJaKieleenSisältyvätKurssit = (oppimaaraKoodisto, oppimaaraKoodiarvo) match {
      case (Some(kieliKoodisto), Some(kieliKoodiarvo)) =>
        sisältyvätKurssit(kieliKoodisto, kieliKoodiarvo) match {
          case Nil => oppiaineeseenSisältyvätKurssit
          case kieleensisältyvätKurssit => kieleensisältyvätKurssit.intersect(oppiaineeseenSisältyvätKurssit)
        }
      case _ => oppiaineeseenSisältyvätKurssit
    }
    toKoodistoEnumValues(oppiaineeseenJaKieleenSisältyvätKurssit match {
      case Nil if ePerusteetRakenne.isDefined => koodistojenKoodit(kurssiKoodistot)
        .filter(k => ePerusteidenMukaisetKurssit.map(_.koodiArvo).contains(k.koodiarvo))
      case Nil => koodistojenKoodit(kurssiKoodistot)
      case _ => oppiaineeseenJaKieleenSisältyvätKurssit
    })
  }

  get("/preferences/:organisaatioOid/:type") {
    val organisaatioOid = params("organisaatioOid")
    val `type` = params("type")
    renderEither(preferencesService.get(organisaatioOid, `type`).right.map(_.map(OppijaEditorModel.buildModel(_, true))))
  }
  import reflect.runtime.universe.TypeTag

  override def toJsonString[T: TypeTag](x: T): String = Serialization.write(x.asInstanceOf[AnyRef])(LegacyJsonSerialization.jsonFormats + EditorModelSerializer)

  private def getKooditFromRequestParams() = koodistojenKoodit(koodistotByString(params("koodistoUri")))

  private def koodistojenKoodit(koodistot: List[KoodistoViite]) = koodistot.flatMap(application.koodistoViitePalvelu.getKoodistoKoodiViitteet(_).toList.flatten)

  private def toKoodistoEnumValues(koodit: List[Koodistokoodiviite]) = koodit.map(KoodistoEnumModelBuilder.koodistoEnumValue(_)(localization, application.koodistoViitePalvelu)).sortBy(_.title)

  private def koodistotByString(str: String): List[KoodistoViite] = {
    val koodistoUriParts = str.split(",").toList
    koodistoUriParts flatMap {part: String =>
      context.koodistoPalvelu.getLatestVersion(part)
    }
  }

  private val context: ValidationAndResolvingContext = ValidationAndResolvingContext(application.koodistoViitePalvelu, application.organisaatioRepository)

  private def findByHenkilöOid(oid: String): Either[HttpStatus, EditorModel] = {
    for {
      oid <- HenkilöOid.validateHenkilöOid(oid).right
      oppija <- application.oppijaFacade.findOppija(oid)
    } yield {
      toEditorModel(oppija, editable = true)
    }
  }

  private def findVersion(henkilöOid: String, opiskeluoikeusOid: String, versionumero: Int): Either[HttpStatus, EditorModel] = {
    for {
      oid <- HenkilöOid.validateHenkilöOid(henkilöOid).right
      oppija <- application.oppijaFacade.findVersion(oid, opiskeluoikeusOid, versionumero)
    } yield {
      toEditorModel(oppija, editable = false)
    }
  }

  private def findByUserOppija: Either[HttpStatus, EditorModel] = {
    val oppija: Either[HttpStatus, Oppija] = application.oppijaFacade.findUserOppija
    oppija.right.map(oppija => OmatTiedotEditorModel.toEditorModel(oppija))
  }
}
