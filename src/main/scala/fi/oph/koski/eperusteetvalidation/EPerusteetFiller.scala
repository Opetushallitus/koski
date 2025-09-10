package fi.oph.koski.eperusteetvalidation

import fi.oph.koski.eperusteet.EPerusteetRepository
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.log.Logging
import fi.oph.koski.schema._
import fi.oph.koski.tutkinto.Koulutustyyppi.{ammatillinenPerustutkintoErityisopetuksena, valmaErityisopetuksena}
import fi.oph.koski.tutkinto.TutkintoRepository
import mojave._

import java.time.LocalDate

class EPerusteetFiller(
  ePerusteet: EPerusteetRepository,
  tutkintoRepository: TutkintoRepository,
  koodistoViitePalvelu: KoodistoViitePalvelu
) extends EPerusteetValidationUtils(tutkintoRepository, koodistoViitePalvelu) with Logging {

  def addKoulutustyyppi(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = {
    koulutustyyppiTraversal.modify(oo) { koulutus =>
      val koulutustyyppi = koulutus match {
        case d: Diaarinumerollinen if !d.perusteenDiaarinumero.exists(onKoodistossa) =>
          d.perusteenDiaarinumero.flatMap(haeKoulutustyyppi)
        case _ =>
          val koulutustyyppiKoodisto = koodistoViitePalvelu.koodistoPalvelu.getLatestVersionRequired("koulutustyyppi")
          val koulutusTyypit = koodistoViitePalvelu.getSisältyvätKoodiViitteet(koulutustyyppiKoodisto, koulutus.tunniste).toList.flatten
          koulutusTyypit.filterNot(koodi => List(ammatillinenPerustutkintoErityisopetuksena.koodiarvo, valmaErityisopetuksena.koodiarvo).contains(koodi.koodiarvo)).headOption
      }
      lens[Koulutus].field[Option[Koodistokoodiviite]]("koulutustyyppi").set(koulutus)(koulutustyyppi)
    }
  }

  def fillPerusteenNimi(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = oo match {
    case a: AmmatillinenOpiskeluoikeus => a.withSuoritukset(
      a.suoritukset.map {
        case s: AmmatillisenTutkinnonSuoritus =>
          s.copy(koulutusmoduuli = s.koulutusmoduuli.copy(perusteenNimi = s.koulutusmoduuli.perusteenDiaarinumero.flatMap(diaarinumero => perusteenNimi(diaarinumero, Some(oo.getVaadittuPerusteenVoimassaolopäivä)))))
        case s: NäyttötutkintoonValmistavanKoulutuksenSuoritus =>
          s.copy(tutkinto = s.tutkinto.copy(perusteenNimi = s.tutkinto.perusteenDiaarinumero.flatMap(diaarinumero => perusteenNimi(diaarinumero, Some(oo.getVaadittuPerusteenVoimassaolopäivä)))))
        case s: AmmatillisenTutkinnonOsittainenSuoritus =>
          s.copy(koulutusmoduuli = s.koulutusmoduuli.copy(perusteenNimi = s.koulutusmoduuli.perusteenDiaarinumero.flatMap(diaarinumero => perusteenNimi(diaarinumero, Some(oo.getVaadittuPerusteenVoimassaolopäivä)))))
        case s: AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus =>
          s.withOsasuoritukset(s.osasuoritukset.map(oss => oss.map {
            case os: YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus => os.copy(tutkinto = os.tutkinto.copy(perusteenNimi = os.tutkinto.perusteenDiaarinumero.flatMap(diaarinumero => perusteenNimi(diaarinumero, if(os.tunnustettu.isDefined) None else Some(oo.getVaadittuPerusteenVoimassaolopäivä)))))
            case os: MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus => os.copy(tutkinto = os.tutkinto.copy(perusteenNimi = os.tutkinto.perusteenDiaarinumero.flatMap(diaarinumero => perusteenNimi(diaarinumero, if(os.tunnustettu.isDefined) None else Some(oo.getVaadittuPerusteenVoimassaolopäivä)))))
            case os => os
          }))
        case s: TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus =>
          s.withOsasuoritukset(s.osasuoritukset.map(_.map {
            case os: PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus =>
              os.copy(
                liittyyTutkintoon = os.liittyyTutkintoon.copy(
                  perusteenNimi = os.liittyyTutkintoon.perusteenDiaarinumero.flatMap(diaarinumero => perusteenNimi(diaarinumero, Some(oo.getVaadittuPerusteenVoimassaolopäivä)))
                )
              )
            case os => os
          }))
        case s: MuunAmmatillisenKoulutuksenSuoritus =>
          s.withOsasuoritukset(s.osasuoritukset.map(_.map {
            case os: PaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus =>
              os.copy(
                liittyyTutkintoon = os.liittyyTutkintoon.copy(
                  perusteenNimi = os.liittyyTutkintoon.perusteenDiaarinumero.flatMap(diaarinumero => perusteenNimi(diaarinumero, Some(oo.getVaadittuPerusteenVoimassaolopäivä)))
                )
              )
            case os => os
          }))
        case o => o
      })
    case x => x
  }

  private def perusteenNimi(diaariNumero: String, päivä: Option[LocalDate]): Option[LocalizedString] = {
    ePerusteet.findPerusteenYksilöintitiedot(diaariNumero, Some(päivä.getOrElse(LocalDate.now)))
      .headOption
      .map(_.nimi)
      .flatMap(LocalizedString.sanitize)
  }

  def fillTutkinnonOsanRyhmät(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = opiskeluoikeus match {
    case oo: AmmatillinenOpiskeluoikeus => oo.withSuoritukset(
      oo.suoritukset.map {
        case s: AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus => s.withOsasuoritukset(
          s.osasuoritukset.map(oss => oss.map {
            case os: YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanUseastaTutkinnostaSuoritus => os.copy(
              tutkinnonOsanRyhmä = os.tutkinnonOsanRyhmä.orElse(Some(yhteinenTutkinnonOsanRyhmä))
            )
            case os => os
          })
        )
        case s => s
      }
    )
    case oo => oo
  }
}
