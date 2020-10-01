package fi.oph.koski.validation

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._

object Lukio2019VieraatKieletValidation {

  def fillVieraatKielet(suoritus: LukionPäätasonSuoritus2019): PäätasonSuoritus =
    suoritus.withOsasuoritukset(suoritus.osasuoritukset.map(_.map(fillOsasuoritus)))

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

  lazy val moduulikoodiPrefixienKielet = List(
    ("RU",  Koodistokoodiviite("SV", "kielivalikoima")),
    ("FIN", Koodistokoodiviite("FI", "kielivalikoima")),
    ("FIM", Koodistokoodiviite("FI", "kielivalikoima")),
    ("LA",  Koodistokoodiviite("LA", "kielivalikoima")),
    ("SM",  Koodistokoodiviite("SE", "kielivalikoima")),
    ("EN",  Koodistokoodiviite("EN", "kielivalikoima"))
  )
}
