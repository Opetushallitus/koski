package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.http.KoskiErrorCategory.badRequest.validation.laajuudet._
import fi.oph.koski.schema._

object TutkintokoulutukseenValmentavaKoulutusValidation {

  def validateTuvaSuoritus(suoritus: Suoritus, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): HttpStatus = {
    suoritus match {
      case suoritus: TutkintokoulutukseenValmentavanKoulutuksenSuoritus =>
        validateTuvaSuorituksenLaajuusJaRakenne(suoritus, opiskeluoikeus)
      case _ =>
        HttpStatus.ok
    }
  }

  /*
    Valmistunut-tilan saa merkitä, kun opiskelija on suorittanut vähintään neljä (4) viikkoa suorituksia,
      joista kaksi (2) viikkoa oltava suoritettu yhteistä koulutuksen osaa Opiskelu- ja urasuunnittelutaidot
      ja lisäksi kaksi vähintään á yhden viikon (1) laajuista muuta koulutuksen osaa.
    Suorituksia tulee siis olla kolmesta eri koulutuksen osasta (2 + 1 + 1 viikkoa) yhteensä neljän (4) viikon edestä,
      joista kaksi (2) viikkoa tule olla pakollisesta yhteisestä koulutuksen osasta.
   */
  private def validateTuvaSuorituksenLaajuusJaRakenne(
    suoritus: TutkintokoulutukseenValmentavanKoulutuksenSuoritus,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus
  ): HttpStatus = {
    val opiskeluOikeudenTilaValmistunut =
      opiskeluoikeus.tila.opiskeluoikeusjaksot.lastOption.map(_.tila.koodiarvo).contains("valmistunut")
    val päätasonSuorituksenLaajuusViikkoina = suoritus.koulutusmoduuli.laajuusArvo(default = 0.0)

    if (opiskeluOikeudenTilaValmistunut && päätasonSuorituksenLaajuusViikkoina >= 4.0) {
      HttpStatus.fold(
        validateOsasuoritustenLaajuus(suoritus),
        validateTuvaOpiskeluJaUrasuunnittelutaidotOsasuoritusOlemassa(suoritus),
        validateTuvaMuidenKuinOpiskeluJaUrasuunnittelutaidotOsasuorituksenLaajuus(suoritus)
      )
    } else {
      tuvaPäätasonSuoritusVääräLaajuus()
    }
  }

  private def validateOsasuoritustenLaajuus(
    suoritus: TutkintokoulutukseenValmentavanKoulutuksenSuoritus
  ): HttpStatus = {

    def laajuusVälillä(min: Double, max: Double, k: Koulutusmoduuli, virheIlmoitus: HttpStatus): HttpStatus =
      HttpStatus.validate(min <= k.laajuusArvo(default = 0.0) && k.laajuusArvo(default = 0.0) <= max) {
        virheIlmoitus
      }

    val validointiTulokset = suoritus.osasuoritukset.getOrElse(List.empty).map(_.koulutusmoduuli).map {
      case t: TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot =>
        laajuusVälillä(min = 2, max = 10, t, tuvaOpiskeluJaUrasuunnittelutaidotVääräLaajuus())
      case t: TutkintokoulutukseenValmentavaPerustaitojenVahvistaminen =>
        laajuusVälillä(min = 1, max = 30, t, tuvaPerustaitojenVahvistaminenVääräLaajuus())
      case t: TutkintokoulutukseenValmentavatLukiokoulutuksenOpinnot =>
        laajuusVälillä(min = 1, max = 30, t, tuvaLukiokoulutuksenOpinnotVääräLaajuus())
      case t: TutkintokoulutukseenValmentavatAmmatillisenKoulutuksenOpinnot =>
        laajuusVälillä(min = 1, max = 30, t, tuvaAmmatillisenKoulutuksenOpinnotVääräLaajuus())
      case t: TutkintokoulutukseenValmentavatTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminen =>
        laajuusVälillä(min = 1, max = 20, t, tuvaTyöelämätaidotJaTyöpaikallaTapahtuvaOppiminenVääräLaajuus())
      case t: TutkintokoulutukseenValmentavatArjenJaYhteiskunnallisenOsallisuudenTaidot =>
        laajuusVälillä(min = 1, max = 20, t, tuvaArjenJaYhteiskunnallisenOsallisuudenTaidotVääräLaajuus())
      case t: TutkintokoulutukseenValmentavanKoulutuksenValinnaisenKoulutusosa =>
        laajuusVälillä(min = 1, max = 10, t, tuvaValinnaisenKoulutusosanVääräLaajuus())
      case _ => HttpStatus.ok
    }

    HttpStatus.fold(validointiTulokset)
  }

  private def validateTuvaOpiskeluJaUrasuunnittelutaidotOsasuoritusOlemassa(
    suoritus: TutkintokoulutukseenValmentavanKoulutuksenSuoritus
  ): HttpStatus = {
    HttpStatus.validate(
      suoritus.osasuoritukset.getOrElse(List.empty).map(_.koulutusmoduuli).exists {
        case _: TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot => true
        case _ => false
      }
    ) {
      KoskiErrorCategory.badRequest.validation.rakenne.tuvaOpiskeluJaUrasuunnittelutaitojenOsasuoritusPuuttuu()
    }
  }

  private def validateTuvaMuidenKuinOpiskeluJaUrasuunnittelutaidotOsasuorituksenLaajuus(
    suoritus: TutkintokoulutukseenValmentavanKoulutuksenSuoritus
  ): HttpStatus = {
    val muutVähintäänViikonLaajuisetOsasuoritukset =
      suoritus.osasuoritukset.getOrElse(List.empty).groupBy(_.koulutusmoduuli).filter {
        case (_: TutkintokoulutukseenValmentavatOpiskeluJaUrasuunnittelutaidot, _) => false
        case (_, _) => true
      }

    HttpStatus.validate(muutVähintäänViikonLaajuisetOsasuoritukset.size >= 2) {
      KoskiErrorCategory.badRequest.validation.rakenne.tuvaOsasuorituksiaLiianVähän()
    }
  }
}
