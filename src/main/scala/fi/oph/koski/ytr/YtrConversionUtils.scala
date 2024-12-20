package fi.oph.koski.ytr

import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema.{Koodistokoodiviite, Koulutustoimija, Oppilaitos, Organisaatiovahvistus, YlioppilaskokeenArviointi, YlioppilastutkinnonTutkintokerta}

import java.time.LocalDate

class YtrConversionUtils(
  localizations: LocalizationRepository,
  koodistoViitePalvelu: KoodistoViitePalvelu,
  organisaatioRepository: OrganisaatioRepository
) {
  private val ytlOid = "1.2.246.562.10.43628088406"
  private def maybeYtl = organisaatioRepository.getOrganisaatio(ytlOid)

  def ytlOppilaitos: Option[Oppilaitos] = maybeYtl.map(ytl =>
    Oppilaitos(oid = ytl.oid, nimi = ytl.nimi)
  )
  def ytl: Koulutustoimija = maybeYtl
    .flatMap(_.toKoulutustoimija)
    .getOrElse(throw new IllegalStateException(("Ylioppilastutkintolautakuntaorganisaatiota ei löytynyt organisaatiopalvelusta")))

  def helsinki: Koodistokoodiviite = koodistoViitePalvelu.validate("kunta", "091")
    .getOrElse(throw new IllegalStateException("Helsingin kaupunkia ei löytynyt koodistopalvelusta"))

  def convertVahvistus(graduationDate: LocalDate): Organisaatiovahvistus = {
    Organisaatiovahvistus(graduationDate, helsinki, ytl.toOidOrganisaatio)
  }

  def convertTutkintokerta(period: String): YlioppilastutkinnonTutkintokerta = {
    val (year, season) = YtrConversionUtils.convertPeriodToYearAndSeason(period)
    val seasonName = season match {
      case "K" => localizations.get("kevät")
      case "S" => localizations.get("syksy")
    }
    YlioppilastutkinnonTutkintokerta(period, year, seasonName)
  }

  def convertArviointi(grade: String, points: Option[Int]) = {
    YlioppilaskokeenArviointi(requiredKoodi("koskiyoarvosanat", grade), points)
  }

  def requiredKoodi(uri: String, koodi: String): Koodistokoodiviite = {
    koodistoViitePalvelu.validateRequired(uri, koodi)
  }
}

object YtrConversionUtils {
  def convertPeriodToYearAndSeason(period: String): (Int, String) = {
    val Pattern = raw"(\d\d\d\d)(K|S)".r
    period match {
      case Pattern(year, season) => (year.toInt, season)
    }
  }

  def convertTutkintokertaToDate(period: String): LocalDate = {
    val (year, season) = convertPeriodToYearAndSeason(period)
    season match {
      case "K" => LocalDate.of(year, 3, 1)
      case "S" => LocalDate.of(year, 10, 1)
    }
  }
}
