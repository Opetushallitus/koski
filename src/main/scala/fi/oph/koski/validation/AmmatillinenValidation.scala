package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.opiskeluoikeus.KoskiOpiskeluoikeusRepository
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
        HttpStatus.fold(
          validateUseaPäätasonSuoritus(ammatillinen),
          validateKeskeneräiselläSuorituksellaEiSaaOllaKeskiarvoa(ammatillinen),
          validateKeskiarvoOlemassaJosSuoritusOnValmis(ammatillinen, koskiOpiskeluoikeudet)
        )
      case _ => HttpStatus.ok
    }
  }

  private def validateKeskeneräiselläSuorituksellaEiSaaOllaKeskiarvoa(ammatillinen: AmmatillinenOpiskeluoikeus) = {
    val isValid = ammatillinen.suoritukset.forall {
      case a: AmmatillisenTutkinnonSuoritus if (a.keskiarvo.isDefined) => a.valmis | (katsotaanEronneeksi(ammatillinen) & tutkinnonOsaOlemassa(a))
      case b: AmmatillisenTutkinnonOsittainenSuoritus if (b.keskiarvo.isDefined) => b.valmis | (katsotaanEronneeksi(ammatillinen) & tutkinnonOsaOlemassa(b))
      case _ => true
    }
    if (isValid) HttpStatus.ok else KoskiErrorCategory.badRequest.validation.ammatillinen.keskiarvoaEiSallitaKeskeneräiselleSuoritukselle()
  }

  private def katsotaanEronneeksi(a: AmmatillinenOpiskeluoikeus) = a.tila.opiskeluoikeusjaksot.last.tila.koodiarvo == "katsotaaneronneeksi"

  private def tutkinnonOsaOlemassa(a: AmmatillisenTutkinnonOsittainenTaiKokoSuoritus) = if (a.osasuoritukset.isDefined) a.osasuoritukset.get.exists(os => os.valmis) else false

  private def validateKeskiarvoOlemassaJosSuoritusOnValmis(ammatillinen: AmmatillinenOpiskeluoikeus, koskiOpiskeluoikeudet: KoskiOpiskeluoikeusRepository) = {
    val kuoriopiskeluoikeus = koskiOpiskeluoikeudet.isKuoriOpiskeluoikeus(ammatillinen)

    val isValid = ammatillinen.suoritukset.forall {
      case a: AmmatillisenTutkinnonSuoritus if (valmistunutAikaisintaan2018ReforminTaiOpsinMukaan(a)) => a.keskiarvo.isDefined | kuoriopiskeluoikeus
      case b: AmmatillisenTutkinnonOsittainenSuoritus if (valmistunutAikaisintaan2018ReforminTaiOpsinMukaan(b)) => b.keskiarvo.isDefined | kuoriopiskeluoikeus
      case _ => true
    }
    if (isValid) HttpStatus.ok else KoskiErrorCategory.badRequest.validation.ammatillinen.valmiillaSuorituksellaPitääOllaKeskiarvo()
  }

  private def valmistunutAikaisintaan2018ReforminTaiOpsinMukaan(a: AmmatillisenTutkinnonOsittainenTaiKokoSuoritus) = {
    a.koulutusmoduuli.koulutustyyppi.contains(Koulutustyyppi.ammatillinenPerustutkinto) &
      a.valmis &
      a.vahvistus.exists(it => it.päivä.isAfter(LocalDate.of(2018, 1, 15))) &
      List("ops", "reformi").contains(a.suoritustapa.koodiarvo)
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
