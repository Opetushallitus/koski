package fi.oph.koski.documentation

import fi.oph.koski.documentation.ExampleData.helsinki
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

import java.time.LocalDate

object EuropeanSchoolOfHelsinkiExampleData {
  lazy val europeanSchoolOfHelsinki: Oppilaitos = Oppilaitos(MockOrganisaatiot.europeanSchoolOfHelsinki, Some(Koodistokoodiviite("03782", None, "oppilaitosnumero", None)), Some("European School of Helsinki"))
  lazy val europeanSchoolOfHelsinkiToimipiste: Toimipiste = Toimipiste("1.2.246.562.10.12798841685")

  def vahvistus(päivä: LocalDate) = ExampleData.vahvistusPaikkakunnalla(päivä, europeanSchoolOfHelsinki, ExampleData.helsinki)

  def nurserySuoritus(luokkaaste: String, alkamispäivä: LocalDate, jääLuokalle: Boolean = false) = NurseryVuosiluokanSuoritus(
    koulutusmoduuli = NurseryLuokkaAste(luokkaaste),
    luokka = Some(s"${luokkaaste}A"),
    alkamispäivä = Some(alkamispäivä),
    toimipiste = europeanSchoolOfHelsinki,
    vahvistus = suoritusVahvistus(alkamispäivä.plusYears(1).withMonth(5).withDayOfMonth(31)),
    suorituskieli = ExampleData.englanti,
    jääLuokalle = jääLuokalle
  )

  def primarySuoritus(luokkaaste: String, alkamispäivä: LocalDate, jääLuokalle: Boolean = false) = PrimaryVuosiluokanSuoritus(
    koulutusmoduuli = PrimaryLuokkaAste(luokkaaste),
    luokka = Some(s"${luokkaaste}A"),
    alkamispäivä = Some(alkamispäivä),
    toimipiste = europeanSchoolOfHelsinki,
    vahvistus = suoritusVahvistus(alkamispäivä.plusYears(1).withMonth(5).withDayOfMonth(31)),
    suorituskieli = ExampleData.englanti,
    jääLuokalle = jääLuokalle
  )

  def secondarySuoritus(luokkaaste: String, alkamispäivä: LocalDate, jääLuokalle: Boolean = false) = SecondaryVuosiluokanSuoritus(
    koulutusmoduuli = SecondaryLuokkaAste(luokkaaste),
    luokka = Some(s"${luokkaaste}A"),
    alkamispäivä = Some(alkamispäivä),
    toimipiste = europeanSchoolOfHelsinki,
    vahvistus = suoritusVahvistus(alkamispäivä.plusYears(1).withMonth(5).withDayOfMonth(31)),
    suorituskieli = ExampleData.englanti,
    jääLuokalle = jääLuokalle
  )

  def suoritusVahvistus(päivä: LocalDate) = ExampleData.vahvistusPaikkakunnalla(päivä, europeanSchoolOfHelsinki, helsinki)
}
