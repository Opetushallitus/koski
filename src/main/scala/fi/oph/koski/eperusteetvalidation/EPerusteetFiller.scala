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
        case np: NuortenPerusopetus =>
          np.perusteenDiaarinumero.flatMap(haeKoulutustyyppi)
        case _ =>
          // 30.9.2022: voisiko koulutustyypit hakea EPerusteista myös lopuille tapauksille?
          val koulutustyyppiKoodisto = koodistoViitePalvelu.koodistoPalvelu.getLatestVersionRequired("koulutustyyppi")
          val koulutusTyypit = koodistoViitePalvelu.getSisältyvätKoodiViitteet(koulutustyyppiKoodisto, koulutus.tunniste).toList.flatten
          koulutusTyypit.filterNot(koodi => List(ammatillinenPerustutkintoErityisopetuksena.koodiarvo, valmaErityisopetuksena.koodiarvo).contains(koodi.koodiarvo)).headOption
      }
      lens[Koulutus].field[Option[Koodistokoodiviite]]("koulutustyyppi").set(koulutus)(koulutustyyppi)
    }
  }

  private def koulutustyyppiTraversal =
    traversal[KoskeenTallennettavaOpiskeluoikeus]
      .field[List[PäätasonSuoritus]]("suoritukset")
      .items
      .field[Koulutusmoduuli]("koulutusmoduuli")
      .ifInstanceOf[Koulutus]


  def fillPerusteenNimi(oo: KoskeenTallennettavaOpiskeluoikeus): KoskeenTallennettavaOpiskeluoikeus = oo match {
    case a: AmmatillinenOpiskeluoikeus => a.withSuoritukset(
      a.suoritukset.map {
        case s: AmmatillisenTutkinnonSuoritus =>
          s.copy(koulutusmoduuli = s.koulutusmoduuli.copy(perusteenNimi = s.koulutusmoduuli.perusteenDiaarinumero.flatMap(diaarinumero => perusteenNimi(diaarinumero, oo.päättymispäivä))))
        case s: NäyttötutkintoonValmistavanKoulutuksenSuoritus =>
          s.copy(tutkinto = s.tutkinto.copy(perusteenNimi = s.tutkinto.perusteenDiaarinumero.flatMap(diaarinumero => perusteenNimi(diaarinumero, oo.päättymispäivä))))
        case s: AmmatillisenTutkinnonOsittainenSuoritus =>
          s.copy(koulutusmoduuli = s.koulutusmoduuli.copy(perusteenNimi = s.koulutusmoduuli.perusteenDiaarinumero.flatMap(diaarinumero => perusteenNimi(diaarinumero, oo.päättymispäivä))))
        case o => o
      })
    case x => x
  }

  private def perusteenNimi(diaariNumero: String, päivä: Option[LocalDate]): Option[LocalizedString] = {
    ePerusteet.findPerusteenYksilöintitiedot(diaariNumero, päivä)
      .headOption
      .map(_.nimi)
      .flatMap(LocalizedString.sanitize)
  }
}
