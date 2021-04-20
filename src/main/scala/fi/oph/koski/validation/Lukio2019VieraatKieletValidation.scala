package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koodisto.MockKoodistoViitePalvelu
import fi.oph.koski.schema._

object Lukio2019VieraatKieletValidation {

  def fillVieraatKielet(suoritus: PäätasonSuoritus): PäätasonSuoritus =
    suoritus.withOsasuoritukset(suoritus.osasuoritukset.map(_.map(fillOsasuoritus)))

  def validate(suoritus: Suoritus, parents: List[Suoritus]): HttpStatus = {
    HttpStatus.fold(List(
      validateVKModuulitMuissaOpinnoissa(suoritus),
      validateMuutModuulitMuissaOpinnoissa(suoritus),
      validateDeprekoituKielikoodi(suoritus),
      validateÄidinkielenOmainenKieliÄidinkielessä(suoritus),
      validateÄidinkielenOmainenKieli(suoritus),
      validateSuullisenKielitaidonKokeet(suoritus, parents),
      validateOmanÄidinkielenOpinnotModuuleina(suoritus)
    ))
  }

  private def fillOsasuoritus(osasuoritus: Suoritus) = (osasuoritus, osasuoritus.koulutusmoduuli) match {
    case (l: LukionOppiaineenSuoritus2019, k:VierasTaiToinenKotimainenKieli2019) => l.withOsasuoritukset(l.osasuoritukset.map(_.map(fillModuulinSuoritusOppiaineissa(k.kieli))))
    case (l: LukionOppiaineenPreIBSuoritus2019, k:VierasTaiToinenKotimainenKieli2019) => l.withOsasuoritukset(l.osasuoritukset.map(_.map(fillModuulinSuoritusOppiaineissa(k.kieli))))
    case (m: MuidenLukioOpintojenSuoritus2019, _) => m.withOsasuoritukset(m.osasuoritukset.map(_.map(fillModuulinSuoritusMuissaOpinnoissa)))
    case (m: MuidenLukioOpintojenPreIBSuoritus2019, _) => m.withOsasuoritukset(m.osasuoritukset.map(_.map(fillModuulinSuoritusMuissaOpinnoissa)))
    case _ => osasuoritus
  }

  private def fillModuulinSuoritusOppiaineissa(kieli:Koodistokoodiviite)(osasuoritus: Suoritus) =
    osasuoritus match {
      case m: LukionModuulinSuoritusOppiaineissa2019 => m.withKoulutusmoduuli(
        LukionVieraanKielenModuuliOppiaineissa2019(
          m.koulutusmoduuli.tunniste,
          m.koulutusmoduuli.laajuus,
          m.koulutusmoduuli.pakollinen,
          Some(kieli)
        )
      )
      case m: PreIBLukionModuulinSuoritusOppiaineissa2019 => m.withKoulutusmoduuli(
        LukionVieraanKielenModuuliOppiaineissa2019(
          m.koulutusmoduuli.tunniste,
          m.koulutusmoduuli.laajuus,
          m.koulutusmoduuli.pakollinen,
          Some(kieli)
        )
      )
      case _ => osasuoritus
    }

  private def fillModuulinSuoritusMuissaOpinnoissa(osasuoritus: Suoritus) = osasuoritus match {
    case m: LukionModuulinSuoritusMuissaOpinnoissa2019 => m.withKoulutusmoduuli(fillKoulutusmoduuliMuissaOpinnoissa(m.koulutusmoduuli))
    case m: PreIBLukionModuulinSuoritusMuissaOpinnoissa2019 => m.withKoulutusmoduuli(fillKoulutusmoduuliMuissaOpinnoissa(m.koulutusmoduuli))
    case _ => osasuoritus
  }

  private def fillKoulutusmoduuliMuissaOpinnoissa[T <: KoodistostaLöytyväKoulutusmoduuli with KoulutusmoduuliPakollinenLaajuusOpintopisteissä with Valinnaisuus](koulutusmoduuli: T) = {
    moduulikoodiPrefixienKielet.find(t => koulutusmoduuli.tunniste.koodiarvo.startsWith(t._1)) match {
      case Some((_, kieli)) =>
        LukionVieraanKielenModuuliMuissaOpinnoissa2019(
          koulutusmoduuli.tunniste,
          koulutusmoduuli.laajuus,
          koulutusmoduuli.pakollinen,
          kieli
        )
      case None => koulutusmoduuli
    }
  }

  private def validateVKModuulitMuissaOpinnoissa(suoritus: Suoritus): HttpStatus = (suoritus, suoritus.koulutusmoduuli) match {
    case (_: LukionModuulinSuoritusMuissaOpinnoissa2019 | _: PreIBLukionModuulinSuoritusMuissaOpinnoissa2019, k: LukionMuuModuuliMuissaOpinnoissa2019)
      if (k.tunniste.koodiarvo.startsWith("VK")) =>
      KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia(s"Muissa suorituksissa olevalta vieraan kielen moduulilta ${k.tunniste} puuttuu kieli")
    case _ =>
      HttpStatus.ok
  }

  private def validateMuutModuulitMuissaOpinnoissa(suoritus: Suoritus): HttpStatus = (suoritus, suoritus.koulutusmoduuli) match {
    case (_: LukionModuulinSuoritusMuissaOpinnoissa2019 | _: PreIBLukionModuulinSuoritusMuissaOpinnoissa2019, k: LukionVieraanKielenModuuliMuissaOpinnoissa2019)
      if !vieraanKielenModuuliPrefixit.exists(k.tunniste.koodiarvo.startsWith(_)) =>
      KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia(s"Suoritukselle ${k.tunniste} on määritelty kieli, vaikka se ei ole vieraan kielen moduuli")
    case _ =>
      HttpStatus.ok
  }

  private def validateDeprekoituKielikoodi(suoritus: Suoritus): HttpStatus = {
    val kiellettyKielikoodi = "97"

    suoritus.koulutusmoduuli match {
      case s: VierasTaiToinenKotimainenKieli2019 if s.kieli.koodiarvo == kiellettyKielikoodi =>
        KoskiErrorCategory.badRequest.validation.rakenne.deprekoituKielikoodi(s"Suorituksessa ${s.tunniste} käytettyä kielikoodia ${kiellettyKielikoodi} ei sallita")
      case s: LukionVieraanKielenModuuliMuissaOpinnoissa2019 if s.kieli.koodiarvo == kiellettyKielikoodi =>
        KoskiErrorCategory.badRequest.validation.rakenne.deprekoituKielikoodi(s"Suorituksessa ${s.tunniste} käytettyä kielikoodia ${kiellettyKielikoodi} ei sallita")
      case _ => HttpStatus.ok
    }
  }

  private def validateÄidinkielenOmainenKieliÄidinkielessä(suoritus: Suoritus): HttpStatus = {
    val kiellettyKielikoodi = "AIAI"

    suoritus.koulutusmoduuli match {
      case s: LukionÄidinkieliJaKirjallisuus2019 if s.kieli.koodiarvo == kiellettyKielikoodi =>
        KoskiErrorCategory.badRequest.validation.rakenne.deprekoituOppimäärä(s"Suorituksessa ${s.tunniste} käytettyä kieltä ${kiellettyKielikoodi} ei sallita. Oman äidinkielen opinnot kuuluu siirtää vieraan kielen opintoina.")
      case _ => HttpStatus.ok
    }
  }

  private def validateÄidinkielenOmainenKieli(suoritus: Suoritus) = suoritus.koulutusmoduuli match {
    case k: VierasTaiToinenKotimainenKieli2019 if k.tunniste.koodiarvo == "AOM" =>
      HttpStatus.validate(List("SV", "FI").contains(k.kieli.koodiarvo)) { KoskiErrorCategory.badRequest.validation.rakenne.deprekoituKielikoodi("Äidinkielen omaisen oppiaineen kieli tulee olla suomi tai ruotsi")}
    case _ => HttpStatus.ok
  }

  private def validateSuullisenKielitaidonKokeet(suoritus: Suoritus, parents: List[Suoritus]): HttpStatus = {

    (suoritus, suoritus.koulutusmoduuli, parents) match {
      case (s: LukionModuulinSuoritus2019, k: LukionVieraanKielenModuuliOppiaineissa2019, _ :: (pp: LukionOppimääränSuoritus2019) :: _)
        if pp.vahvistus.isDefined => validateSuullisenKielitaidonKoe(pp, s.koulutusmoduuli.tunniste, k.kieli)
      case (s: PreIBLukionModuulinSuoritus2019, k: LukionVieraanKielenModuuliOppiaineissa2019, _ :: (pp: PreIBSuoritus2019) :: _)
        if pp.vahvistus.isDefined => validateSuullisenKielitaidonKoe(pp, s.koulutusmoduuli.tunniste, k.kieli)
      case (s: LukionModuulinSuoritus2019, k: LukionVieraanKielenModuuliMuissaOpinnoissa2019, _ :: (pp: LukionOppimääränSuoritus2019) :: _)
        if pp.vahvistus.isDefined => validateSuullisenKielitaidonKoe(pp, s.koulutusmoduuli.tunniste, Some(k.kieli))
      case (s: PreIBLukionModuulinSuoritus2019, k: LukionVieraanKielenModuuliMuissaOpinnoissa2019, _ :: (pp: PreIBSuoritus2019) :: _)
        if pp.vahvistus.isDefined => validateSuullisenKielitaidonKoe(pp, s.koulutusmoduuli.tunniste, Some(k.kieli))
      case (s: LukionModuulinSuoritus2019, k: LukionVieraanKielenModuuliOppiaineissa2019, (p: LukionOppiaineenSuoritus2019) :: (pp: LukionOppiaineidenOppimäärienSuoritus2019) :: _)
        if p.viimeisinArviointi.isDefined => validateSuullisenKielitaidonKoe(pp, s.koulutusmoduuli.tunniste, k.kieli)
      case _ =>
        HttpStatus.ok
    }
  }


  private def validateSuullisenKielitaidonKoe(päätasonSuoritus: SuullisenKielitaidonKokeellinen2019, moduuli: Koodistokoodiviite, kieli: Option[Koodistokoodiviite]) = {
    (moduuli, kieli) match {
      case (m, Some(k)) if suullisenKielitaidonKokeenVaativatModuulit.contains(m.koodiarvo) && !päätasonSuoritus.suullisenKielitaidonKokeet.exists(_.exists(_.kieli == k)) =>
        KoskiErrorCategory.badRequest.validation.rakenne.puuttuvaSuullisenKielitaidonKoe(s"Suoritus ${m} vaatii merkinnän suullisesta kielitaidon kokeesta päätason suorituksessa kielellä ${k}")
      case (m, None) =>
        KoskiErrorCategory.internalError(s"Suorituksessa ${m} pitäisi olla edellisten validaatiovaiheiden seurauksena aina kieli määritelty")
      case _ =>
        HttpStatus.ok
    }
  }

  private def validateOmanÄidinkielenOpinnotModuuleina(suoritus: Suoritus): HttpStatus = {
    suoritus match {
      case _: LukionModuulinSuoritus2019 | _: PreIBLukionModuulinSuoritus2019
        if omanÄidinkielenOpinnotPrefixit.exists(suoritus.koulutusmoduuli.tunniste.koodiarvo.startsWith(_)) =>
        KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia(
          s"Moduuli ${suoritus.koulutusmoduuli.tunniste.koodiarvo} ei ole sallittu oppiaineen osasuoritus. Lukion oppimäärää täydentävän oman äidinkielen opinnoista siirretään vain kokonaisarviointi ja tieto opiskellusta kielestä.")
      case _ => HttpStatus.ok
    }
  }

  lazy val moduulikoodiPrefixienKielet = List(
    ("RU",  Koodistokoodiviite("SV", "kielivalikoima")),
    ("FIN", Koodistokoodiviite("FI", "kielivalikoima")),
    ("FIM", Koodistokoodiviite("FI", "kielivalikoima")),
    ("LA",  Koodistokoodiviite("LA", "kielivalikoima")),
    ("SM",  Koodistokoodiviite("SE", "kielivalikoima")),
    ("EN",  Koodistokoodiviite("EN", "kielivalikoima"))
  ).map({
    case (k, kv) => (k, MockKoodistoViitePalvelu.validateRequired(kv))
  })

  lazy val vieraanKielenModuuliPrefixit = List(
    "RU",
    "FIN",
    "FIM",
    "LA",
    "SM",
    "EN",
    "VK"
  )

  lazy val suullisenKielitaidonKokeenVaativatModuulit = List(
    "RUA8",
    "RUB16",
    "RUÄ8",
    "FINA8",
    "FINB16",
    "FIM8",
    "ENA8",
    "VKA8",
    "SMA8"
  )

  lazy val omanÄidinkielenOpinnotPrefixit = List(
    "OÄI",
    "RÄI",
    "SÄI"
  )
}
