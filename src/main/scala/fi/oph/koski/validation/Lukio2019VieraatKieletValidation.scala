package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._

object Lukio2019VieraatKieletValidation {

  def fillVieraatKielet(suoritus: LukionPäätasonSuoritus2019): PäätasonSuoritus =
    suoritus.withOsasuoritukset(suoritus.osasuoritukset.map(_.map(fillOsasuoritus)))

  def validate(suoritus: Suoritus, parents: List[Suoritus]): HttpStatus = {
    HttpStatus.fold(List(
      validateVKModuulitMuissaOpinnoissa(suoritus),
      validateMuutModuulitMuissaOpinnoissa(suoritus),
      validateDeprekoituKielikoodi(suoritus)
    ))
  }

  private def fillOsasuoritus(osasuoritus: LukionOppimääränOsasuoritus2019) = (osasuoritus, osasuoritus.koulutusmoduuli) match {
    case (l: LukionOppiaineenSuoritus2019, k:VierasTaiToinenKotimainenKieli2019) => l.withOsasuoritukset(l.osasuoritukset.map(_.map(fillModuulinSuoritusOppiaineissa(k.kieli))))
    case (m: MuidenLukioOpintojenSuoritus2019, _) => m.withOsasuoritukset(m.osasuoritukset.map(_.map(fillModuulinSuoritusMuissaOpinnoissa)))
    case _ => osasuoritus
  }

  private def fillModuulinSuoritusOppiaineissa(kieli:Koodistokoodiviite)(osasuoritus: LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019) =
    osasuoritus match {
      case m: LukionModuulinSuoritusOppiaineissa2019 => m.withKoulutusmoduuli(
        LukionVieraanKielenModuuliOppiaineissa2019(
          m.koulutusmoduuli.tunniste,
          m.koulutusmoduuli.laajuus,
          m.koulutusmoduuli.pakollinen,
          Some(kieli)
        )
      )
      case _ => osasuoritus
    }

  private def fillModuulinSuoritusMuissaOpinnoissa(osasuoritus: LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019) = osasuoritus match {
    case m: LukionModuulinSuoritusMuissaOpinnoissa2019 => m.withKoulutusmoduuli(fillKoulutusmoduuliMuissaOpinnoissa(m.koulutusmoduuli))
    case _ => osasuoritus
  }

  private def fillKoulutusmoduuliMuissaOpinnoissa(koulutusmoduuli: LukionModuuliMuissaOpinnoissa2019) = {
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
    case (_: LukionModuulinSuoritusMuissaOpinnoissa2019, k: LukionMuuModuuliMuissaOpinnoissa2019) if (k.tunniste.koodiarvo.startsWith("VK")) =>
      KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaOsasuorituksia(s"Muissa suorituksissa olevalta vieraan kielen moduulilta ${k.tunniste} puuttuu kieli")
    case _ =>
      HttpStatus.ok
  }

  private def validateMuutModuulitMuissaOpinnoissa(suoritus: Suoritus): HttpStatus = (suoritus, suoritus.koulutusmoduuli) match {
    case (_: LukionModuulinSuoritusMuissaOpinnoissa2019, k: LukionVieraanKielenModuuliMuissaOpinnoissa2019)
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

  lazy val moduulikoodiPrefixienKielet = List(
    ("RU",  Koodistokoodiviite("SV", "kielivalikoima")),
    ("FIN", Koodistokoodiviite("FI", "kielivalikoima")),
    ("FIM", Koodistokoodiviite("FI", "kielivalikoima")),
    ("LA",  Koodistokoodiviite("LA", "kielivalikoima")),
    ("SM",  Koodistokoodiviite("SE", "kielivalikoima")),
    ("EN",  Koodistokoodiviite("EN", "kielivalikoima"))
  )

  lazy val vieraanKielenModuuliPrefixit = List(
    "RU",
    "FIN",
    "FIM",
    "LA",
    "SM",
    "EN",
    "VK"
  )
}
