package fi.oph.koski.eperusteetvalidation

import fi.oph.koski.eperusteet.EPerusteetRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.log.Logging
import fi.oph.koski.schema._
import fi.oph.koski.tutkinto.TutkintoRepository

import java.time.LocalDate

class EPerusteetOpiskeluoikeusChangeValidator(
  ePerusteet: EPerusteetRepository,
  tutkintoRepository: TutkintoRepository,
  koodistoViitePalvelu: KoodistoViitePalvelu
) extends EPerusteetValidationUtils(tutkintoRepository, koodistoViitePalvelu) with Logging {

  def validateVanhanOpiskeluoikeudenTapaukset(
    vanhaOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus
  ): HttpStatus = {
    (vanhaOpiskeluoikeus, uusiOpiskeluoikeus) match {
      case (vanha: AmmatillinenOpiskeluoikeus, uusi: AmmatillinenOpiskeluoikeus) =>
        validateTutkintokoodinTaiSuoritustavanMuutos(vanha, uusi)
      case _ => HttpStatus.ok
    }
  }

  private def validateTutkintokoodinTaiSuoritustavanMuutos(
    vanhaOpiskeluoikeus: AmmatillinenOpiskeluoikeus,
    uusiOpiskeluoikeus: AmmatillinenOpiskeluoikeus
  ): HttpStatus = {
    val vanhanSuoritustavat = suoritustavat(vanhaOpiskeluoikeus)
    val uudenSuoritustavat = suoritustavat(uusiOpiskeluoikeus)

    // Päätason suorituksia on enemmän kuin 1 vain näyttötutkinto + näyttöön valmistavan tapauksessa,
    // joka on hiljalleen poistumassa käytöstä. Jotta voidaan tukea päätason suorituksen poistoa,
    // kun halutaan poistaa virheellisesti lisätty näyttöön valmistavan suoritus, riittää, että
    // 1 tutkintokoodi mätsää.
    val suoritusTavatLöytyvät = vanhanSuoritustavat.isEmpty || vanhanSuoritustavat.exists(koodi => uudenSuoritustavat.contains(koodi))
    val tutkintokooditLöytyvät = checkTutkintokooditLöytyvät(vanhaOpiskeluoikeus, uusiOpiskeluoikeus)

    if (suoritusTavatLöytyvät && tutkintokooditLöytyvät) {
      HttpStatus.ok
    } else {
      KoskiErrorCategory.badRequest.validation.ammatillinen.muutettuSuoritustapaaTaiTutkintokoodia()
    }
  }

  private def checkTutkintokooditLöytyvät(
    vanhaOpiskeluoikeus: AmmatillinenOpiskeluoikeus,
    uusiOpiskeluoikeus: AmmatillinenOpiskeluoikeus
  ): Boolean = {
    // Optimointi: Tarkistetaan eperusteista löytyvyys vain jos tarpeen eli jos tutkintokoodit eivät alustavasti mätsää
    val vanhanTutkintokoodit = tutkintokoodit(vanhaOpiskeluoikeus)
    val uudenTutkintokoodit = tutkintokoodit(uusiOpiskeluoikeus)

    // Päätason suorituksia on enemmän kuin 1 vain näyttötutkinto + näyttöön valmistavan tapauksessa,
    // joka on hiljalleen poistumassa käytöstä. Jotta voidaan tukea päätason suorituksen poistoa,
    // kun halutaan poistaa virheellisesti lisätty näyttöön valmistavan suoritus, riittää, että
    // 1 tutkintokoodi mätsää.
    if (vanhanTutkintokoodit.isEmpty || vanhanTutkintokoodit.exists(koodi => uudenTutkintokoodit.contains(koodi))) {
      true
    } else {
      val vanhanTutkintokooditEperusteettomat = tutkintokooditPoislukienPerusteestaLöytymättömät(vanhaOpiskeluoikeus, vanhaOpiskeluoikeus.päättymispäivä)
      val uudenTutkintokooditEperusteettomat = tutkintokooditPoislukienPerusteestaLöytymättömät(uusiOpiskeluoikeus, uusiOpiskeluoikeus.päättymispäivä)

      vanhanTutkintokooditEperusteettomat.count(koodi => uudenTutkintokooditEperusteettomat.contains(koodi)) == vanhanTutkintokooditEperusteettomat.length
    }
  }

  private def tutkintokooditPoislukienPerusteestaLöytymättömät(oo: AmmatillinenOpiskeluoikeus, päivä: Option[LocalDate]): List[String] = {
    oo.suoritukset.filter(suoritus =>
      suoritus.koulutusmoduuli match {
        case diaarillinen: DiaarinumerollinenKoulutus =>
          diaarillinen.perusteenDiaarinumero match {
            case Some(diaarinumero) if !onKoodistossa(diaarinumero) =>
              ePerusteet
                .findTarkatRakenteet(diaarinumero, päivä)
                .exists(_.koulutukset.exists(_.koulutuskoodiArvo == diaarillinen.tunniste.koodiarvo))
            case _ => true
          }
        case _ => true
      }
    ).map(_.koulutusmoduuli.tunniste.koodiarvo)
  }

  private def tutkintokoodit(oo: AmmatillinenOpiskeluoikeus): List[String] = {
    oo.suoritukset.map(
      _.koulutusmoduuli.tunniste.koodiarvo
    )
  }

  private def suoritustavat(oo: AmmatillinenOpiskeluoikeus): List[String] = {
    oo.suoritukset.collect {
      case osittainenTaiKokonainen: AmmatillisenTutkinnonOsittainenTaiKokoSuoritus => osittainenTaiKokonainen.suoritustapa.koodiarvo
      case _: NäyttötutkintoonValmistavanKoulutuksenSuoritus => "valmentava"
    }
  }
}
