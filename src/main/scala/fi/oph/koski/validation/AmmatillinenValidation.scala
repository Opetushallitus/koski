package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.opiskeluoikeus.KoskiOpiskeluoikeusRepository
import fi.oph.koski.schema
import fi.oph.koski.schema._
import fi.oph.koski.tutkinto.Koulutustyyppi

import java.time.LocalDate

object AmmatillinenValidation {
  def validateAmmatillinenOpiskeluoikeus(
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    koskiOpiskeluoikeudet: KoskiOpiskeluoikeusRepository
  ): HttpStatus = {
    opiskeluoikeus match {
      case ammatillinen: AmmatillinenOpiskeluoikeus =>
        lazy val isKuoriopiskeluoikeus = koskiOpiskeluoikeudet.isKuoriOpiskeluoikeus(ammatillinen)
        HttpStatus.fold(
          validateUseaPäätasonSuoritus(ammatillinen),
          validateKeskeneräiselläSuorituksellaEiSaaOllaKeskiarvoa(ammatillinen),
          validateKeskiarvoOlemassaJosSuoritusOnValmis(ammatillinen, isKuoriopiskeluoikeus)
        )
      case _ => HttpStatus.ok
    }
  }

  private def validateKeskeneräiselläSuorituksellaEiSaaOllaKeskiarvoa(ammatillinen: AmmatillinenOpiskeluoikeus) = {
    def katsotaanEronneeksi(a: AmmatillinenOpiskeluoikeus) = a.tila.opiskeluoikeusjaksot.last.tila.koodiarvo == "katsotaaneronneeksi"
    def tutkinnonOsaOlemassa(a: AmmatillisenTutkinnonOsittainenTaiKokoSuoritus) = if (a.osasuoritukset.isDefined) a.osasuoritukset.get.exists(os => os.valmis) else false

    val keskiarvotVainKunSallittu = ammatillinen.suoritukset.forall {
      case s: AmmatillisenTutkinnonOsittainenTaiKokoSuoritus =>
        if (s.keskiarvo.isDefined) s.valmis || (katsotaanEronneeksi(ammatillinen) && tutkinnonOsaOlemassa(s)) else true
      case _ => true
    }

    if (keskiarvotVainKunSallittu) HttpStatus.ok else KoskiErrorCategory.badRequest.validation.ammatillinen.keskiarvoaEiSallitaKeskeneräiselleSuoritukselle()
  }

  private def validateKeskiarvoOlemassaJosSuoritusOnValmis(ammatillinen: AmmatillinenOpiskeluoikeus, isKuoriopiskeluoikeus: Boolean) = {
    val keskiarvotOlemassa = ammatillinen.suoritukset.forall {
      case s: AmmatillisenTutkinnonOsittainenTaiKokoSuoritus =>
        if (keskiarvoPakollinenVahvistuspäivänä(s, isKuoriopiskeluoikeus)) s.keskiarvo.isDefined else true
      case _ => true
    }
    if (keskiarvotOlemassa) HttpStatus.ok else KoskiErrorCategory.badRequest.validation.ammatillinen.valmiillaSuorituksellaPitääOllaKeskiarvo()
  }

  private def keskiarvoPakollinenVahvistuspäivänä(s: AmmatillisenTutkinnonOsittainenTaiKokoSuoritus, isKuoriopiskeluoikeus: Boolean) = {
    def valmistunutReforminTaiOpsinMukaan(a: AmmatillisenTutkinnonOsittainenTaiKokoSuoritus) = {
      a.koulutusmoduuli.koulutustyyppi.contains(Koulutustyyppi.ammatillinenPerustutkinto) &&
        List("ops", "reformi").contains(a.suoritustapa.koodiarvo)
    }

    def valmisJaVahvistettuAikaisintaan(a: AmmatillisenTutkinnonOsittainenTaiKokoSuoritus, earliestDate: LocalDate): Boolean =
      a.valmis && a.vahvistus.exists(it => it.päivä.isAfter(earliestDate))

    val keskiarvoPakollinenAlkaen = s match {
      case _: AmmatillisenTutkinnonSuoritus => LocalDate.of(2018, 1, 15)
      case _: AmmatillisenTutkinnonOsittainenSuoritus => LocalDate.of(2022, 1, 1)
    }

    valmistunutReforminTaiOpsinMukaan(s) &&
      valmisJaVahvistettuAikaisintaan(s, keskiarvoPakollinenAlkaen) &&
      !isKuoriopiskeluoikeus
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
}
