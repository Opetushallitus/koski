package fi.oph.koski.ytr

import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema.{Koodistokoodiviite, Koulutustoimija, Organisaatiovahvistus, YlioppilaskokeenArviointi, YlioppilastutkinnonTutkintokerta}

import java.time.LocalDate

class YtrConversionUtils(
  localizations: LocalizationRepository,
  koodistoViitePalvelu: KoodistoViitePalvelu,
  organisaatioRepository: OrganisaatioRepository
) {
  def ytl: Koulutustoimija = organisaatioRepository.getOrganisaatio("1.2.246.562.10.43628088406")
    .flatMap(_.toKoulutustoimija)
    .getOrElse(throw new IllegalStateException(("Ylioppilastutkintolautakuntaorganisaatiota ei löytynyt organisaatiopalvelusta")))

  def helsinki: Koodistokoodiviite = koodistoViitePalvelu.validate("kunta", "091")
    .getOrElse(throw new IllegalStateException("Helsingin kaupunkia ei löytynyt koodistopalvelusta"))

  def convertVahvistus(graduationDate: LocalDate): Organisaatiovahvistus = {
    Organisaatiovahvistus(graduationDate, helsinki, ytl.toOidOrganisaatio)
  }

  def convertTutkintokerta(period: String): YlioppilastutkinnonTutkintokerta = {
    val Pattern = "(\\d\\d\\d\\d)(K|S)".r
    val tutkintokerta = period match {
      case Pattern(year, season) =>
        val seasonName = season match {
          case "K" => localizations.get("kevät")
          case "S" => localizations.get("syksy")
        }
        YlioppilastutkinnonTutkintokerta(period, year.toInt, seasonName)
    }
    tutkintokerta
  }

  def convertArviointi(grade: String, points: Option[Int]) = {
    YlioppilaskokeenArviointi(requiredKoodi("koskiyoarvosanat", grade), points)
  }

  def requiredKoodi(uri: String, koodi: String): Koodistokoodiviite = {
    koodistoViitePalvelu.validateRequired(uri, koodi)
  }
}
