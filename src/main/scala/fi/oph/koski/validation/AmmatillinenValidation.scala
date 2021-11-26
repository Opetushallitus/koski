package fi.oph.koski.validation

import fi.oph.koski.eperusteet.EPerusteetRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeus, AmmatillisenTutkinnonOsittainenSuoritus, AmmatillisenTutkinnonOsittainenTaiKokoSuoritus, AmmatillisenTutkinnonSuoritus, DiaarinumerollinenKoulutus, KoskeenTallennettavaOpiskeluoikeus, NäyttötutkintoonValmistavanKoulutuksenSuoritus}

import java.time.LocalDate
import com.typesafe.config.Config

object AmmatillinenValidation {
  def validateAmmatillinenOpiskeluoikeus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
                                         vanhaOpiskeluoikeus: Option[KoskeenTallennettavaOpiskeluoikeus],
                                         ePerusteet: EPerusteetRepository,
                                         config: Config): HttpStatus = {
    opiskeluoikeus match {
      case ammatillinen: AmmatillinenOpiskeluoikeus =>
        HttpStatus.fold(
          validatePerusteVoimassa(ammatillinen, ePerusteet, config),
          validateUseaPäätasonSuoritus(ammatillinen),
          vanhaOpiskeluoikeus match {
            case Some(vanha) if vanha.isInstanceOf[AmmatillinenOpiskeluoikeus] =>
              validateTutkintokoodinTaiSuoritustavanMuutos(ammatillinen, vanha.asInstanceOf[AmmatillinenOpiskeluoikeus])
            case _ => HttpStatus.ok
          })
      case _ => HttpStatus.ok
    }
  }

  private def validateUseaPäätasonSuoritus(opiskeluoikeus: AmmatillinenOpiskeluoikeus): HttpStatus = {
    opiskeluoikeus.suoritukset.length match {
      case 1 => HttpStatus.ok
      case 2 =>
        val näyttötutkintoLöytyy = opiskeluoikeus.suoritukset.exists {
          case osittainen: AmmatillisenTutkinnonOsittainenSuoritus if osittainen.suoritustapa.koodiarvo == "naytto" => true
          case kokonainen: AmmatillisenTutkinnonSuoritus if kokonainen.suoritustapa.koodiarvo == "naytto" => true
          case _ => false
        }
        val näyttöönValmistavaLöytyy = opiskeluoikeus.suoritukset.exists {
          case _: NäyttötutkintoonValmistavanKoulutuksenSuoritus => true
          case _ => false
        }
        if (näyttötutkintoLöytyy && näyttöönValmistavaLöytyy) {
          HttpStatus.ok
        } else {
          KoskiErrorCategory.badRequest.validation.ammatillinen.useampiPäätasonSuoritus()
        }
      case _ => KoskiErrorCategory.badRequest.validation.ammatillinen.useampiPäätasonSuoritus()
    }
  }

  private def validateTutkintokoodinTaiSuoritustavanMuutos(uusiOpiskeluoikeus: AmmatillinenOpiskeluoikeus,
                                                           vanhaOpiskeluoikeus: AmmatillinenOpiskeluoikeus): HttpStatus = {
    val vanhanSuoritustavat = suoritustavat(vanhaOpiskeluoikeus)
    val uudenSuoritustavat = suoritustavat(uusiOpiskeluoikeus)

    val vanhanTutkintokoodit = tutkintokoodit(vanhaOpiskeluoikeus)
    val uudenTutkintokoodit = tutkintokoodit(uusiOpiskeluoikeus)

    val suoritustapaLöytyy = vanhanSuoritustavat.count(tapa => uudenSuoritustavat.contains(tapa)) == vanhanSuoritustavat.length
    val tutkintokoodiLöytyy = vanhanTutkintokoodit.count(koodi => uudenTutkintokoodit.contains(koodi)) == vanhanTutkintokoodit.length

    if (suoritustapaLöytyy && tutkintokoodiLöytyy) {
      HttpStatus.ok
    } else {
      KoskiErrorCategory.badRequest.validation.ammatillinen.muutettuSuoritustapaaTaiTutkintokoodia()
    }
  }

  private def suoritustavat(oo: AmmatillinenOpiskeluoikeus): List[String] = {
    oo.suoritukset.collect {
      case osittainenTaiKokonainen: AmmatillisenTutkinnonOsittainenTaiKokoSuoritus => osittainenTaiKokonainen.suoritustapa.koodiarvo
    }
  }

  private def tutkintokoodit(oo: AmmatillinenOpiskeluoikeus): List[String] = {
    oo.suoritukset.map(_.koulutusmoduuli.tunniste.koodiarvo)
  }

  private def validatePerusteVoimassa(opiskeluoikeus: AmmatillinenOpiskeluoikeus, ePerusteet: EPerusteetRepository, config: Config): HttpStatus = {
    val validaatioViimeinenPäiväEnnenVoimassaoloa = LocalDate.parse(config.getString("validaatiot.ammatillisenPerusteidenVoimassaoloTarkastusAstuuVoimaan")).minusDays(1)

    if (!opiskeluoikeus.tila.opiskeluoikeusjaksot.exists(_.opiskeluoikeusPäättynyt) && LocalDate.now().isAfter(validaatioViimeinenPäiväEnnenVoimassaoloa)) {
      opiskeluoikeus.suoritukset.head.koulutusmoduuli match {
        case diaarillinen: DiaarinumerollinenKoulutus if diaarillinen.perusteenDiaarinumero.isDefined =>
          ePerusteet.findRakenne(diaarillinen.perusteenDiaarinumero.get) match {
            case Some(peruste) =>
              if (peruste.päättynyt()) {
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
}
