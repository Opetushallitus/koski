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
          validateUseaPäätasonSuoritus(ammatillinen)
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

    if (!opiskeluoikeus.tila.opiskeluoikeusjaksot.exists(_.opiskeluoikeusPäättynyt) && LocalDate.now().isAfter(validaatioViimeinenPäiväEnnenVoimassaoloa)) {
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

    // Pieni oikominen; jos suoritustapoja/tutkintokoodeja olisi kolmesta tai useammasta päätason suorituksesta, tämä ei välttämättä
    // nappaisi kaikkia muutoksia. Mutta päätason suorituksia ei pitäisi voida olla kahta enempää, eikä samantyyppisiä
    // päätason suorituksia yhtä enempää.
    val suoritustapaLöytyy = vanhanSuoritustavat.count(tapa => uudenSuoritustavat.contains(tapa)) == vanhanSuoritustavat.length
    val tutkintokoodiLöytyy = checkTutkintokooditLöytyvät(vanhaOpiskeluoikeus, uusiOpiskeluoikeus, ePerusteet)

    val salliNäytönJaValmistavanPoikkeusPoistettaessaSuoritusta = näyttötutkintoJaNäyttöönValmistavaLöytyvät(vanhaOpiskeluoikeus)

    if (suoritustapaLöytyy && (tutkintokoodiLöytyy || salliNäytönJaValmistavanPoikkeusPoistettaessaSuoritusta)) {
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

    if (vanhanTutkintokoodit.count(koodi => uudenTutkintokoodit.contains(koodi)) != vanhanTutkintokoodit.length) {
      val vanhanTutkintokooditEperusteettomat = tutkintokooditPoislukienPerusteestaLöytymättömät(vanhaOpiskeluoikeus, ePerusteet)
      val uudenTutkintokooditEperusteeettomat = tutkintokooditPoislukienPerusteestaLöytymättömät(uusiOpiskeluoikeus, ePerusteet)

      vanhanTutkintokooditEperusteettomat.count(koodi => uudenTutkintokooditEperusteeettomat.contains(koodi)) == vanhanTutkintokooditEperusteettomat.length
    } else {
      true
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
    }
  }
}
