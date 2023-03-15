package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.opiskeluoikeus.KoskiOpiskeluoikeusRepository
import fi.oph.koski.schema
import fi.oph.koski.schema._
import fi.oph.koski.tutkinto.Koulutustyyppi

import java.time.LocalDate

object AmmatillinenValidation {
  def validateAmmatillinenOpiskeluoikeus(
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    henkilö: Option[Henkilö],
    koskiOpiskeluoikeudet: KoskiOpiskeluoikeusRepository
  )(implicit user: KoskiSpecificSession): HttpStatus = {
    opiskeluoikeus match {
      case ammatillinen: AmmatillinenOpiskeluoikeus =>
        lazy val isKuoriopiskeluoikeus = koskiOpiskeluoikeudet.isKuoriOpiskeluoikeus(ammatillinen)
        HttpStatus.fold(
          validateUseaPäätasonSuoritus(ammatillinen),
          validateKeskeneräiselläSuorituksellaEiSaaOllaKeskiarvoa(ammatillinen),
          validateKeskiarvoOlemassaJosSuoritusOnValmis(ammatillinen, isKuoriopiskeluoikeus),
          validateAmmatillisenKorotus(ammatillinen),
          validateKorotuksenAlkuperäinenOpiskeluoikeus(ammatillinen, henkilö, koskiOpiskeluoikeudet)
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
      case os: AmmatillisenTutkinnonOsittainenSuoritus if os.rekursiivisetOsasuoritukset.exists {
        case s: Korotuksellinen => s.korotettu.isDefined
        case _ => false
      } => KoskiErrorCategory.badRequest.validation.ammatillinen.eiKorotuksenSuoritus()
      case _ => HttpStatus.ok
    }.getOrElse(HttpStatus.ok)
  }

  private def validateKorotettuSuoritus(oo: AmmatillinenOpiskeluoikeus, korotettuSuoritus: AmmatillisenTutkinnonOsittainenSuoritus): HttpStatus = {
    val oss = korotettuSuoritus.osasuoritukset.getOrElse(List.empty)
    val kaikkiKorotuksetEpäonnistuivatTaiTunnustettu = validateKaikkiKorotukselliset(
      korotettuSuoritus,
      k => k.korotettu.map(_.koodiarvo).contains("korotuksenyritys") || k.tunnustettu.isDefined
    )

    def validateOpiskeluoikeudenAlkamispäivä: HttpStatus = HttpStatus.validate(
      oo.alkamispäivä.exists(d => d.isAfter(LocalDate.of(2023, 6, 30)))
    )(KoskiErrorCategory.badRequest.validation.date.alkamispäivä("Ammatillisen korotuksen suorituksen opiskeluoikeus voi alkaa aikaisintaan 1.7.2023"))

    def osasuorituksetKorotettuTaiTunnustettu: HttpStatus = HttpStatus.validate(
      (!oo.onValmistunut && oss.isEmpty) || validateKaikkiKorotukselliset(korotettuSuoritus, k => k.korotettu.isDefined || k.tunnustettu.isDefined)
    )(KoskiErrorCategory.badRequest.validation.ammatillinen.korotettuOsasuoritus())

    def eiTunnustettujaTutkinnonOsanSuorituksia: HttpStatus = HttpStatus.validate(
      oss.forall {
        case os: MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus => os.tunnustettu.isEmpty
        case os: YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus => os.tunnustettu.isEmpty
        case _ => true
      }
    )(KoskiErrorCategory.badRequest.validation.ammatillinen.korotettuOsasuoritus("Muun tutkinnon osan tai yhteisen tutkinnon osan suoritus ei voi olla tunnustettu korotuksen opiskeluoikeudella"))

    def eiKorotuksiaEikäKorotettuaKeskiarvoa: HttpStatus = HttpStatus.validate(
      if(oo.onValmistunut && kaikkiKorotuksetEpäonnistuivatTaiTunnustettu) {
        korotettuSuoritus.korotettuKeskiarvo.isEmpty
      } else { true }
    )(KoskiErrorCategory.badRequest.validation.ammatillinen.korotettuKeskiarvo("Korotettua keskiarvoa ei voi siirtää jos kaikki korotuksen yritykset epäonnistuivat"))

    def valmistunutJaKorotettuKeskiarvo: HttpStatus = HttpStatus.validate(
      !oo.onValmistunut || (kaikkiKorotuksetEpäonnistuivatTaiTunnustettu && korotettuSuoritus.korotettuKeskiarvo.isEmpty) || korotettuSuoritus.korotettuKeskiarvo.isDefined
    )(KoskiErrorCategory.badRequest.validation.ammatillinen.korotettuKeskiarvo("Valmistuneella korotuksen suorituksella on oltava korotettu keskiarvo, kun sillä on onnistuneita korotuksia"))

    def katsotaanEronneeksiIlmanOsasuorituksia: HttpStatus = HttpStatus.validate(
      !oo.onKatsotaanEronneeksi || oss.isEmpty
    )(KoskiErrorCategory.badRequest.validation.ammatillinen.korotettuOsasuoritus("Jos korotuksen suoritus on katsotaan eronneeksi -tilassa, ei suoritukselle voi siirtää osasuorituksia"))

    def katsotaanEronneeksiJaKorotettuKeskiarvo: HttpStatus = HttpStatus.validate(
      !oo.onKatsotaanEronneeksi || korotettuSuoritus.korotettuKeskiarvo.isEmpty
    )(KoskiErrorCategory.badRequest.validation.ammatillinen.korotettuKeskiarvo("Jos korotuksen opiskeluoikeus on katsotaan eronneeksi -tilassa, ei suoritukselle voi siirtää korotettua keskiarvoa"))

    HttpStatus.fold(
      validateOpiskeluoikeudenAlkamispäivä,
      osasuorituksetKorotettuTaiTunnustettu,
      eiTunnustettujaTutkinnonOsanSuorituksia,
      eiKorotuksiaEikäKorotettuaKeskiarvoa,
      valmistunutJaKorotettuKeskiarvo,
      katsotaanEronneeksiIlmanOsasuorituksia,
      katsotaanEronneeksiJaKorotettuKeskiarvo
    )
  }

  private def validateKaikkiKorotukselliset(
    korotettuSuoritus: AmmatillisenTutkinnonOsittainenSuoritus,
    validateFun: Korotuksellinen => Boolean
  ): Boolean = {
    val rekursiivisetOsasuoritukset = korotettuSuoritus.rekursiivisetOsasuoritukset
    rekursiivisetOsasuoritukset.nonEmpty && rekursiivisetOsasuoritukset.forall{
      case k: Korotuksellinen => validateFun(k)
      case _ => true
    }
  }

  def validateKorotetunOpiskeluoikeudenLinkitysEiMuuttunut(
    oldState: KoskeenTallennettavaOpiskeluoikeus,
    newState: KoskeenTallennettavaOpiskeluoikeus
  ): HttpStatus = {
    def getKorotettuOpiskeluoikeusOid(s: AmmatillinenPäätasonSuoritus): Option[String] = s match {
      case s: AmmatillisenTutkinnonOsittainenSuoritus => s.korotettuOpiskeluoikeusOid
      case _ => None
    }

    (oldState, newState) match {
      case (oldOo: AmmatillinenOpiskeluoikeus, newOo: AmmatillinenOpiskeluoikeus) =>
        val oldLinkitykset = oldOo.suoritukset.flatMap(getKorotettuOpiskeluoikeusOid)
        val newLinkitykset = newOo.suoritukset.flatMap(getKorotettuOpiskeluoikeusOid)
        HttpStatus.validate(
          oldLinkitykset.isEmpty || oldLinkitykset.forall(oid => newLinkitykset.contains(oid))
        )(KoskiErrorCategory.badRequest.validation.ammatillinen.korotuksenLinkitys())
      case _ => HttpStatus.ok
    }
  }

  def validateKorotuksenAlkuperäinenOpiskeluoikeus(
    ammatillinen: AmmatillinenOpiskeluoikeus,
    henkilö: Option[Henkilö],
    koskiOpiskeluoikeudet: KoskiOpiskeluoikeusRepository
  )(implicit user: KoskiSpecificSession): HttpStatus = {
    val korotuksenSuoritus = ammatillinen.suoritukset.headOption.flatMap {
      case s: AmmatillisenTutkinnonOsittainenSuoritus if s.korotettuOpiskeluoikeusOid.isDefined => Some(s)
      case _ => None
    }

    if(korotuksenSuoritus.isEmpty) {
      HttpStatus.ok
    } else {
      val alkuperäinenOid = korotuksenSuoritus.flatMap(_.korotettuOpiskeluoikeusOid)
      val oppijaOids = korotuksenSuoritus
        .flatMap(_.korotettuOpiskeluoikeusOid)
        .flatMap(oid => koskiOpiskeluoikeudet.getOppijaOidsForOpiskeluoikeus(oid).toOption)
        .getOrElse(List.empty)
      val alkuperäinenOpiskeluoikeus = koskiOpiskeluoikeudet.findByOppijaOids(oppijaOids)
        .find(oo => oo.oid.isDefined && alkuperäinenOid == oo.oid)

      HttpStatus.fold(
        validateKorotuksenOppija(henkilö, oppijaOids),
        validateKorotuksenAlkuperäinenOpiskeluoikeusOnValmistunut(alkuperäinenOpiskeluoikeus),
        validateKorotuksenAlkuperäinenSuoritus(korotuksenSuoritus, alkuperäinenOpiskeluoikeus)
      )
    }
  }

  private def validateKorotuksenOppija(
    korotuksenHenkilö: Option[Henkilö],
    alkuperäisetOppijaOidit: List[Henkilö.Oid]
  ): HttpStatus = {
    val henkilöOid = korotuksenHenkilö.flatMap {
      case h: HenkilöWithOid => Some(h.oid)
      case _ => None
    }

    HttpStatus.validate(
      henkilöOid.exists(oid =>
        alkuperäisetOppijaOidit.contains(oid)
      )
    )(KoskiErrorCategory.badRequest.validation.ammatillinen.korotuksenOppija())
  }

  private def validateKorotuksenAlkuperäinenOpiskeluoikeusOnValmistunut(
    alkuperäinenOpiskeluoikeus: Option[Opiskeluoikeus]
  ): HttpStatus = {
    HttpStatus.validate(
      alkuperäinenOpiskeluoikeus.exists(_.tila.opiskeluoikeusjaksot.last.tila.koodiarvo == "valmistunut")
    )(KoskiErrorCategory.badRequest.validation.ammatillinen.alkuperäinenEiValmistunut())
  }

  private def validateKorotuksenAlkuperäinenSuoritus(
    korotuksenSuoritus: Option[AmmatillisenTutkinnonOsittainenSuoritus],
    alkuperäinenOpiskeluoikeus: Option[Opiskeluoikeus]
  ): HttpStatus = {
    val alkuperäisetSuoritukset = alkuperäinenOpiskeluoikeus.map(_.suoritukset).getOrElse(List.empty).flatMap {
      case s: AmmatillisenTutkinnonSuoritus => Some(s)
      case _ => None
    }

    val alkuperäinenSuoritus = alkuperäisetSuoritukset.find { s =>
      s.tyyppi.koodiarvo == "ammatillinentutkinto" &&
        s.koulutusmoduuli.tunniste.koodiarvo == korotuksenSuoritus.map(_.koulutusmoduuli.tunniste.koodiarvo).getOrElse("") &&
        s.koulutusmoduuli.perusteenDiaarinumero.nonEmpty && s.koulutusmoduuli.perusteenDiaarinumero == korotuksenSuoritus.flatMap(_.koulutusmoduuli.perusteenDiaarinumero) &&
        s.koulutusmoduuli.koulutustyyppi.map(_.koodiarvo).contains("1") &&
        s.koulutusmoduuli.koulutustyyppi.map(_.koodiarvo) == korotuksenSuoritus.flatMap(_.koulutusmoduuli.koulutustyyppi.map(_.koodiarvo))
    }

    val validateVastaavaAlkuperäinenSuoritusLöytyy = HttpStatus.validate(
      alkuperäinenSuoritus.isDefined
    )(KoskiErrorCategory.badRequest.validation.ammatillinen.alkuperäinenSuoritusEiVastaava())

    HttpStatus.fold(
      validateVastaavaAlkuperäinenSuoritusLöytyy,
      alkuperäinenSuoritus.map(s =>
        validateKorotetutOsasuorituksetjaAliosasuoritukset(korotuksenSuoritus, s)
      ).getOrElse(HttpStatus.ok)
    )
  }

  private def validateKorotetutOsasuorituksetjaAliosasuoritukset(
    korotuksenSuoritus: Option[AmmatillisenTutkinnonOsittainenSuoritus],
    alkuperäinenSuoritus: AmmatillisenTutkinnonSuoritus
  ): HttpStatus = {
    val korotetutOsasuoritukset = korotuksenSuoritus.flatMap(_.osasuoritukset).getOrElse(List.empty)
    HttpStatus.fold(
      validateKorotettujaVastaavatOsasuoritukset(korotetutOsasuoritukset, alkuperäinenSuoritus),
      validateKorotettujenOsasuoritustenLaajuus(korotetutOsasuoritukset),
      validateKorotettujenSamojenTutkinnonOsienMäärä(korotetutOsasuoritukset, alkuperäinenSuoritus)
    )
  }

  def validateKorotettujaVastaavatOsasuoritukset(
    korotetutOsasuoritukset: List[OsittaisenAmmatillisenTutkinnonOsanSuoritus],
    alkuperäinenSuoritus: AmmatillisenTutkinnonSuoritus
  ): HttpStatus = {
    def validateVastaavaOsasuoritusLöytyy(
      korotettuOs: OsittaisenAmmatillisenTutkinnonOsanSuoritus,
      alkuperäisetOsasuoritukset: List[AmmatillisenTutkinnonOsanSuoritus]
    ): Boolean = {
      alkuperäisetOsasuoritukset.exists(aos =>
        korotettuOs.tyyppi.koodiarvo == aos.tyyppi.koodiarvo &&
          korotettuOs.koulutusmoduuli.tunniste.koodiarvo == aos.koulutusmoduuli.tunniste.koodiarvo &&
          Math.abs(korotettuOs.koulutusmoduuli.laajuusArvo(0.00) - aos.koulutusmoduuli.laajuusArvo(0.0)) < 0.001 &&
          validateKorotuksellaAliosasuorituksia(korotettuOs) &&
          korotettuOs.osasuoritusLista.forall(s => validateAliosasuoritukset(s, aos.osasuoritusLista))
      )
    }

    def validateKorotuksellaAliosasuorituksia(
      korotettuOs: OsittaisenAmmatillisenTutkinnonOsanSuoritus
    ): Boolean = korotettuOs match {
      case os: YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus => os.osasuoritusLista.nonEmpty
      case os: OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus => os.osasuoritusLista.nonEmpty
      case _ => true
    }

    def validateAliosasuoritukset(
      korotettuAliOsasuoritus: Suoritus,
      alkuperäisetAliosasuoritukset: List[Suoritus]
    ): Boolean = korotettuAliOsasuoritus match {
      case korotettu: YhteisenTutkinnonOsanOsaAlueenSuoritus =>
        alkuperäisetAliosasuoritukset.exists(aaos =>
          validateVastaavaAliosasuoritusLöytyy(
            tutkinnonOsanKoodi = korotettu.koulutusmoduuli.tunniste.koodiarvo,
            laajuus = korotettu.koulutusmoduuli.laajuusArvo(0.0),
            pakollinen = korotettu.koulutusmoduuli.pakollinen,
            alkuperäinenAliosasuoritus = aaos
          )
        )
      case korotettu: MuidenOpintovalmiuksiaTukevienOpintojenSuoritus =>
        alkuperäisetAliosasuoritukset.exists(aaos =>
          validateVastaavaAliosasuoritusLöytyy(
            tutkinnonOsanKoodi = korotettu.koulutusmoduuli.tunniste.koodiarvo,
            laajuus = korotettu.koulutusmoduuli.laajuusArvo(0.0),
            pakollinen = false,
            alkuperäinenAliosasuoritus = aaos
          )
        )
      case _ => false
    }

    def validateVastaavaAliosasuoritusLöytyy(
      tutkinnonOsanKoodi: String,
      laajuus: Double,
      pakollinen: Boolean,
      alkuperäinenAliosasuoritus: Suoritus
    ): Boolean = alkuperäinenAliosasuoritus match {
      case aliosasuoritus: YhteisenTutkinnonOsanOsaAlueenSuoritus =>
        aliosasuoritus.koulutusmoduuli.tunniste.koodiarvo == tutkinnonOsanKoodi &&
          Math.abs(aliosasuoritus.koulutusmoduuli.laajuusArvo(0.0) - laajuus) < 0.001 &&
          aliosasuoritus.koulutusmoduuli.pakollinen == pakollinen
      case aliosasuoritus: Suoritus =>
        aliosasuoritus.koulutusmoduuli.tunniste.koodiarvo == tutkinnonOsanKoodi &&
          Math.abs(aliosasuoritus.koulutusmoduuli.laajuusArvo(0.0) - laajuus) < 0.001
    }

    HttpStatus.validate(
      korotetutOsasuoritukset.forall(os =>
        validateVastaavaOsasuoritusLöytyy(os, alkuperäinenSuoritus.osasuoritukset.getOrElse(List.empty))
      )
    )(KoskiErrorCategory.badRequest.validation.ammatillinen.alkuperäinenOsasuoritusEiVastaava())
  }

  def validateKorotettujenOsasuoritustenLaajuus(
    korotetutOsasuoritukset: List[OsittaisenAmmatillisenTutkinnonOsanSuoritus]
  ): HttpStatus = HttpStatus.validate(
    korotetutOsasuoritukset.forall(os =>
      os.osasuoritukset.isEmpty ||
        Math.abs(
          os.koulutusmoduuli.laajuusArvo(0.0) - os.osasuoritusLista.map(_.koulutusmoduuli.laajuusArvo(0.0)).sum
        ) < 0.001
    )
  )(KoskiErrorCategory.badRequest.validation.ammatillinen.korotuksenLaajuus())

  def validateKorotettujenSamojenTutkinnonOsienMäärä(
    korotetutOsasuoritukset: List[OsittaisenAmmatillisenTutkinnonOsanSuoritus],
    alkuperäinenSuoritus: AmmatillisenTutkinnonSuoritus
  ): HttpStatus = HttpStatus.validate(
    korotetutOsasuoritukset
      .groupBy(_.koulutusmoduuli.tunniste.koodiarvo)
      .forall {
        case (tutkinnonOsa, osasuoritukset) =>
          alkuperäinenSuoritus
            .osasuoritusLista
            .count(s => s.koulutusmoduuli.tunniste.koodiarvo == tutkinnonOsa) >= osasuoritukset.size
      }
  )(KoskiErrorCategory.badRequest.validation.ammatillinen.liikaaSamojaTutkinnonOsia())
}
