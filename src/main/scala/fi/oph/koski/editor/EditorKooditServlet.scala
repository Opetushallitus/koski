package fi.oph.koski.editor

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.http.KoskiErrorCategory.badRequest.validation.koodisto.tuntematonKoodi
import fi.oph.koski.koodisto.KoodistoViite
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.schema._
import fi.oph.koski.servlet.NoCache

/**
  *  Endpoints for the Koski UI, related to koodistot/koodit, returns editor models
  */
class EditorKooditServlet(implicit val application: KoskiApplication) extends EditorApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with NoCache {

  private def localization = LocalizedHtml.get(session, application.koskiLocalizationRepository)

  get[List[EnumValue]]("/:koodistoUri") {
    toKoodistoEnumValues(getKooditFromRequestParams())
  }

  get[List[EnumValue]]("/osaamisalat/osaamisala/:diaari") {
    val osaamisalat = application.tutkintoRepository.findPerusteRakenne(params("diaari"))
      .map(_.osaamisalat)
      .getOrElse(koodistojenKoodit(koodistotByString("osaamisala")))

    toKoodistoEnumValues(osaamisalat)
  }

  get[List[EnumValue]]("/koulutukset/koulutus/:diaari") {
    val koulutukset = application.tutkintoRepository.findPerusteRakenne(params("diaari"))
      .map(_.koulutukset).toList.flatten

    toKoodistoEnumValues(koulutukset)
  }

  get[List[EnumValue]]("/:koodistoUri/:koodiarvot") {
    val koodiarvot = params("koodiarvot").split(",").toSet
    toKoodistoEnumValues(getKooditFromRequestParams().filter(k => koodiarvot.contains(k.koodiarvo)))
  }

  get[ListModel]("/:koodistoUri/:koodiarvo/suoritukset/prefill") {
    def toListModel(suoritukset: List[Suoritus]) = {
      val models = suoritukset.map { suoritus => OppijaEditorModel.buildModel(suoritus, true)}
      ListModel(models, None, Nil)
    }
    val luokkaAstePattern = """(\d)""".r
    val toimintaAlueittain = params.get("toimintaAlueittain").map(_.toBoolean).getOrElse(false)

    (params("koodistoUri"), params("koodiarvo")) match {
      case ("perusopetuksenluokkaaste", luokkaAstePattern(luokkaAste)) =>
        toListModel(NuortenPerusopetusPakollisetOppiaineet(application.koodistoViitePalvelu).pakollistenOppiaineidenTaiToimintaAlueidenSuoritukset(luokkaAste.toInt, toimintaAlueittain))
      case ("koulutus", "201101") =>
        toListModel(NuortenPerusopetusPakollisetOppiaineet(application.koodistoViitePalvelu).päättötodistuksenSuoritukset(params("tyyppi"), toimintaAlueittain))
      case _ =>
        logger.error(s"Prefill failed for unexpected code ${params("koodistoUri")}/${params("koodiarvo")}")
        haltWithStatus(KoskiErrorCategory.notFound())
    }
  }



  get[List[EnumValue]]("/:oppiaineKoodistoUri/:oppiaineKoodiarvo/kurssit/:kurssiKoodistot") {
    val kurssiKoodistot: List[KoodistoViite] = koodistotByString(params("kurssiKoodistot"))
    def sisältyvätKurssit(parentKoodistoUri: String, parentKoodiarvo: String) = {
      val parent = application.koodistoViitePalvelu.validate(parentKoodistoUri, parentKoodiarvo).getOrElse(haltWithStatus(tuntematonKoodi(s"Koodistosta ${parentKoodistoUri} ei löydy koodia ${parentKoodiarvo}")))
      for {
        kurssiKoodisto <- kurssiKoodistot
        kurssiKoodi <- application.koodistoViitePalvelu.getSisältyvätKoodiViitteet(kurssiKoodisto, parent).toList.flatten
      } yield {
        kurssiKoodi
      }
    }

    val oppiaineKoodistoUri = params("oppiaineKoodistoUri")
    val oppiaineKoodiarvo = params("oppiaineKoodiarvo")

    val oppimaaraKoodisto = params.get("oppimaaraKoodisto") // vieraan kielen, äidinkielen ja matematiikan kursseille
    val oppimaaraKoodiarvo = params.get("oppimaaraKoodiarvo")
    val oppimääränDiaarinumero = params.get("oppimaaraDiaarinumero")

    val ePerusteetRakenne = oppimääränDiaarinumero.flatMap(application.ePerusteet.findRakenne)

    val ePerusteidenMukaisetKurssit = {
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

    val oppiaineeseenSisältyvätKurssit = sisältyvätKurssit(oppiaineKoodistoUri, oppiaineKoodiarvo)
    val oppiaineeseenJaKieleenSisältyvätKurssit = (oppimaaraKoodisto, oppimaaraKoodiarvo) match {
      case (Some(kieliKoodisto), Some(kieliKoodiarvo)) =>
        sisältyvätKurssit(kieliKoodisto, kieliKoodiarvo) match {
          case Nil => oppiaineeseenSisältyvätKurssit
          case kieleensisältyvätKurssit => kieleensisältyvätKurssit.intersect(oppiaineeseenSisältyvätKurssit)
        }
      case _ => oppiaineeseenSisältyvätKurssit
    }
    toKoodistoEnumValues(oppiaineeseenJaKieleenSisältyvätKurssit match {
      case Nil if ePerusteidenMukaisetKurssit.nonEmpty => koodistojenKoodit(kurssiKoodistot)
        .filter(k => ePerusteidenMukaisetKurssit.map(_.koodiArvo).contains(k.koodiarvo))
      case Nil => koodistojenKoodit(kurssiKoodistot)
      case _ => oppiaineeseenJaKieleenSisältyvätKurssit
    })
  }

  private def getKooditFromRequestParams() = koodistojenKoodit(koodistotByString(params("koodistoUri")))

  private def koodistojenKoodit(koodistot: List[KoodistoViite]) = koodistot.flatMap(application.koodistoViitePalvelu.getKoodistoKoodiViitteet(_))

  private def toKoodistoEnumValues(koodit: List[Koodistokoodiviite]) = koodit.map(KoodistoEnumModelBuilder.koodistoEnumValue(_)(localization, application.koodistoViitePalvelu)).sortBy(_.title)

  private def koodistotByString(str: String): List[KoodistoViite] = {
    // note: silently omits non-existing koodistot from result
    val koodistoUriParts = str.split(",").toList
    koodistoUriParts flatMap {part: String =>
      application.koodistoViitePalvelu.getLatestVersionOptional(part)
    }
  }
}
