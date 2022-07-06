package fi.oph.koski.validation

import fi.oph.koski.eperusteet.EPerusteetRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeus, AmmatillisenTutkinnonOsittainenSuoritus, AmmatillisenTutkinnonOsittainenTaiKokoSuoritus, AmmatillisenTutkinnonSuoritus, DiaarinumerollinenKoulutus, KoskeenTallennettavaOpiskeluoikeus, NäyttötutkintoonValmistavanKoulutuksenSuoritus}

import java.time.LocalDate
import com.typesafe.config.Config

object AmmatillinenValidation {
  def validateAmmatillinenOpiskeluoikeus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
                                         ePerusteet: EPerusteetRepository,
                                         config: Config): HttpStatus = {
    opiskeluoikeus match {
      case ammatillinen: AmmatillinenOpiskeluoikeus =>
        HttpStatus.fold(
          validatePerusteVoimassa(ammatillinen, ePerusteet, config),
          validateUseaPäätasonSuoritus(ammatillinen),
          validateViestintäJaVuorovaikutusÄidinkielellä2022(ammatillinen)
        )
      case _ => HttpStatus.ok
    }
  }

  private def validateUseaPäätasonSuoritus(opiskeluoikeus: AmmatillinenOpiskeluoikeus): HttpStatus = {
    opiskeluoikeus.suoritukset.length match {
      case 1 => HttpStatus.ok
      case 2 if näyttötutkintoJaNäyttöönValmistavaLöytyvät(opiskeluoikeus) => HttpStatus.ok
      case _ => KoskiErrorCategory.badRequest.validation.ammatillinen.useampiPäätasonSuoritus()
    }
  }

  private def näyttötutkintoJaNäyttöönValmistavaLöytyvät(opiskeluoikeus: AmmatillinenOpiskeluoikeus) = {
    opiskeluoikeus.suoritukset.exists {
      case tutkintoSuoritus: AmmatillisenTutkinnonOsittainenTaiKokoSuoritus if tutkintoSuoritus.suoritustapa.koodiarvo == "naytto" => true
      case _ => false
    } && opiskeluoikeus.suoritukset.exists {
      case _: NäyttötutkintoonValmistavanKoulutuksenSuoritus => true
      case _ => false
    }
  }

  private def validatePerusteVoimassa(opiskeluoikeus: AmmatillinenOpiskeluoikeus, ePerusteet: EPerusteetRepository, config: Config): HttpStatus = {
    val validaatioViimeinenPäiväEnnenVoimassaoloa = LocalDate.parse(config.getString("validaatiot.ammatillisenPerusteidenVoimassaoloTarkastusAstuuVoimaan")).minusDays(1)
    val voimassaolotarkastusAstunutVoimaan = LocalDate.now().isAfter(validaatioViimeinenPäiväEnnenVoimassaoloa)

    if (opiskeluoikeus.aktiivinen && voimassaolotarkastusAstunutVoimaan) {
      opiskeluoikeus.suoritukset.head.koulutusmoduuli match {
        case diaarillinen: DiaarinumerollinenKoulutus if diaarillinen.perusteenDiaarinumero.isDefined =>
          ePerusteet.findUusinRakenne(diaarillinen.perusteenDiaarinumero.get) match {
            case Some(peruste) =>
              if (peruste.siirtymäTaiVoimassaoloPäättynyt()) {
                KoskiErrorCategory.badRequest.validation.rakenne.perusteenVoimassaoloPäättynyt()
              } else {
                HttpStatus.ok
              }
            case _ => HttpStatus.ok
          }
        case _ => HttpStatus.ok
      }
    } else {
      HttpStatus.ok
    }
  }

  def validateVanhanOpiskeluoikeudenTapaukset(vanhaOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
                                                      uusiOpiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
                                                      ePerusteet: EPerusteetRepository): HttpStatus = {
    (vanhaOpiskeluoikeus, uusiOpiskeluoikeus) match {
      case (vanha: AmmatillinenOpiskeluoikeus, uusi: AmmatillinenOpiskeluoikeus) =>
        validateTutkintokoodinTaiSuoritustavanMuutos(vanha, uusi, ePerusteet)
      case _ => HttpStatus.ok
    }
  }

  private def validateTutkintokoodinTaiSuoritustavanMuutos(vanhaOpiskeluoikeus: AmmatillinenOpiskeluoikeus,
                                                           uusiOpiskeluoikeus: AmmatillinenOpiskeluoikeus,
                                                           ePerusteet: EPerusteetRepository): HttpStatus = {
    val vanhanSuoritustavat = suoritustavat(vanhaOpiskeluoikeus)
    val uudenSuoritustavat = suoritustavat(uusiOpiskeluoikeus)

    // Päätason suorituksia on enemmän kuin 1 vain näyttötutkinto + näyttöön valmistavan tapauksessa,
    // joka on hiljalleen poistumassa käytöstä. Jotta voidaan tukea päätason suorituksen poistoa,
    // kun halutaan poistaa virheellisesti lisätty näyttöön valmistavan suoritus, riittää, että
    // 1 tutkintokoodi mätsää.
    val suoritusTavatLöytyvät = vanhanSuoritustavat.isEmpty || vanhanSuoritustavat.exists(koodi => uudenSuoritustavat.contains(koodi))
    val tutkintokooditLöytyvät = checkTutkintokooditLöytyvät(vanhaOpiskeluoikeus, uusiOpiskeluoikeus, ePerusteet)

    if (suoritusTavatLöytyvät && tutkintokooditLöytyvät) {
      HttpStatus.ok
    } else {
      KoskiErrorCategory.badRequest.validation.ammatillinen.muutettuSuoritustapaaTaiTutkintokoodia()
    }
  }

  private def checkTutkintokooditLöytyvät(vanhaOpiskeluoikeus: AmmatillinenOpiskeluoikeus,
                                          uusiOpiskeluoikeus: AmmatillinenOpiskeluoikeus,
                                          ePerusteet: EPerusteetRepository) = {
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
      val vanhanTutkintokooditEperusteettomat = tutkintokooditPoislukienPerusteestaLöytymättömät(vanhaOpiskeluoikeus, ePerusteet)
      val uudenTutkintokooditEperusteeettomat = tutkintokooditPoislukienPerusteestaLöytymättömät(uusiOpiskeluoikeus, ePerusteet)

      vanhanTutkintokooditEperusteettomat.count(koodi => uudenTutkintokooditEperusteeettomat.contains(koodi)) == vanhanTutkintokooditEperusteettomat.length
    }
  }

  private def tutkintokooditPoislukienPerusteestaLöytymättömät(oo: AmmatillinenOpiskeluoikeus, ePerusteet: EPerusteetRepository): List[String] = {
    oo.suoritukset.filter(suoritus =>
      suoritus.koulutusmoduuli match {
        case diaarillinen: DiaarinumerollinenKoulutus =>
          diaarillinen.perusteenDiaarinumero.flatMap(diaari => ePerusteet.findRakenne(diaari)) match {
            case Some(rakenne) =>
              rakenne.koulutukset.exists(_.koulutuskoodiArvo == diaarillinen.tunniste.koodiarvo)
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

  private def validateViestintäJaVuorovaikutusÄidinkielellä2022(oo: AmmatillinenOpiskeluoikeus): HttpStatus = {
    val rajapäivä = LocalDate.of(2022, 8, 1)

    def löytyyVVAI22(suoritus: AmmatillisenTutkinnonSuoritus): Boolean = suoritus
      .osasuoritukset
      .getOrElse(List.empty)
      .flatMap(_.osasuoritusLista)
      .map(_.koulutusmoduuli)
      .exists(k => k.tunniste.koodiarvo == "VVAI22")

    HttpStatus.fold(
      oo.suoritukset.map {
        case a: AmmatillisenTutkinnonSuoritus if oo.alkamispäivä.exists(ap => ap.isBefore(rajapäivä)) =>
          HttpStatus.validate(!löytyyVVAI22(a)) {
            KoskiErrorCategory.badRequest.validation.ammatillinen.yhteinenTutkinnonOsaVVAI22()
          }
        case _ => HttpStatus.ok
      }
    )
  }
}
