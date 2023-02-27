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
          validateKeskiarvoOlemassaJosSuoritusOnValmis(ammatillinen, isKuoriopiskeluoikeus),
          validateAmmatillisenKorotus(ammatillinen)
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
      a.valmis && a.vahvistus.exists(it => it.päivä.isAfter(earliestDate) || it.päivä.isEqual(earliestDate))

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
      case tutkintoSuoritus: AmmatillisenTutkinnonOsittainenSuoritus if tutkintoSuoritus.korotettuOpiskeluoikeusOid.isDefined => false
      case tutkintoSuoritus: AmmatillisenTutkinnonOsittainenTaiKokoSuoritus if tutkintoSuoritus.suoritustapa.koodiarvo == "naytto" => true
      case _ => false
    } && opiskeluoikeus.suoritukset.exists {
      case _: NäyttötutkintoonValmistavanKoulutuksenSuoritus => true
      case _ => false
    }
  }

  private def validateAmmatillisenKorotus(ammatillinen: AmmatillinenOpiskeluoikeus): HttpStatus = {
    ammatillinen.suoritukset.headOption.map {
      case os: AmmatillisenTutkinnonOsittainenSuoritus if os.korotettuOpiskeluoikeusOid.isDefined =>
        validateKorotettuSuoritus(ammatillinen, os)
      case os: AmmatillisenTutkinnonOsittainenSuoritus if os.korotettuKeskiarvo.isDefined =>
        KoskiErrorCategory.badRequest.validation.ammatillinen.korotettuKeskiarvo()
      case os: AmmatillisenTutkinnonOsittainenSuoritus if validateKorotettuSuoritus(os, k => k.korotettu.isDefined) =>
        KoskiErrorCategory.badRequest.validation.ammatillinen.eiKorotuksenSuoritus()
      case _ => HttpStatus.ok
    }.getOrElse(HttpStatus.ok)
  }

  private def validateKorotettuSuoritus(oo: AmmatillinenOpiskeluoikeus, korotettuSuoritus: AmmatillisenTutkinnonOsittainenSuoritus): HttpStatus = {
    val oss = korotettuSuoritus.osasuoritukset.getOrElse(List.empty)

    def osasuorituksetKorotettuTaiTunnustettu: HttpStatus = HttpStatus.validate(
      (!oo.onValmistunut && korotettuSuoritus.osasuoritusLista.isEmpty) || validateKorotettuSuoritus(korotettuSuoritus, k => k.korotettu.isDefined || k.tunnustettu.isDefined)
    )(KoskiErrorCategory.badRequest.validation.ammatillinen.korotettuOsasuoritus())

    def eiKorotuksiaEikäKorotettuaKeskiarvoa: HttpStatus = HttpStatus.validate(
      if(oo.onValmistunut && validateKorotettuSuoritus(korotettuSuoritus, k => k.korotettu.map(_.koodiarvo).contains("korotuksenyritys") || k.tunnustettu.isDefined)) {
        korotettuSuoritus.korotettuKeskiarvo.isEmpty
      } else { true }
    )(KoskiErrorCategory.badRequest.validation.ammatillinen.korotettuKeskiarvo("Korotettua keskiarvoa ei voi siirtää jos kaikki korotuksen yritykset epäonnistuivat"))

    def valmistunutJaKorotettuKeskiarvo: HttpStatus = HttpStatus.validate(
      !oo.onValmistunut || korotettuSuoritus.korotettuKeskiarvo.isDefined
    )(KoskiErrorCategory.badRequest.validation.ammatillinen.korotettuKeskiarvo("Valmistuneella korotuksen suorituksella on oltava korotettu keskiarvo"))

    def katsotaanEronneeksiIlmanOsasuorituksia: HttpStatus = HttpStatus.validate(
      !oo.onKatsotaanEronneeksi || oss.isEmpty
    )(KoskiErrorCategory.badRequest.validation.ammatillinen.korotettuOsasuoritus("Jos korotuksen suoritus on katsotaan eronneeksi -tilassa, ei suoritukselle voi siirtää osasuorituksia"))

    def katsotaanEronneeksiJaKorotettuKeskiarvo: HttpStatus = HttpStatus.validate(
      !oo.onKatsotaanEronneeksi || korotettuSuoritus.korotettuKeskiarvo.isEmpty
    )(KoskiErrorCategory.badRequest.validation.ammatillinen.korotettuKeskiarvo("Jos korotuksen opiskeluoikeus on katsotaan eronneeksi -tilassa, ei suoritukselle voi siirtää korotettua keskiarvoa"))

    HttpStatus.fold(
      osasuorituksetKorotettuTaiTunnustettu,
      eiKorotuksiaEikäKorotettuaKeskiarvoa,
      valmistunutJaKorotettuKeskiarvo,
      katsotaanEronneeksiIlmanOsasuorituksia,
      katsotaanEronneeksiJaKorotettuKeskiarvo
    )
  }

  private def validateKorotettuSuoritus(
    korotettuSuoritus: AmmatillisenTutkinnonOsittainenSuoritus,
    validateFun: Korotuksellinen => Boolean
  ): Boolean = {
    val rekursiivisetOsasuoritukset = korotettuSuoritus.rekursiivisetOsasuoritukset
    rekursiivisetOsasuoritukset.nonEmpty && rekursiivisetOsasuoritukset.forall{
      case k: Korotuksellinen => validateFun(k)
      case _ => true
    }
  }

}
