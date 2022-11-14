package fi.oph.koski.documentation

import fi.oph.koski.documentation.EuropeanSchoolOfHelsinkiExampleData._
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.schema._

import java.time.LocalDate
import java.time.LocalDate.{of => date}

object ExamplesEuropeanSchoolOfHelsinki {
  val alkamispäivä = date(2004, 8, 1)
  val päättymispäivä = alkamispäivä.plusYears(20).withMonth(5).withDayOfMonth(31)
  val lisätiedot = EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot(
    ulkomaanjaksot = Some(List(ExamplesLukio.ulkomaanjakso)),
  )

  val n1 = nurserySuoritus("N1", alkamispäivä.plusYears(0))
  val n2 = nurserySuoritus("N2", alkamispäivä.plusYears(1))

  val p1 = primarySuoritus12("P1", alkamispäivä.plusYears(2))
  val p2JääLuokalle = primarySuoritus12(
    luokkaaste = "P2",
    alkamispäivä = alkamispäivä.plusYears(3),
    jääLuokalle = true,
    todistuksellaNäkyvätLisätiedot = Some(LocalizedString.finnish("Vähän liikaa poissaoloja, muista tulla kouluun paremmin ensi vuonna!"))
  )
  val p2 = primarySuoritus12("P2", alkamispäivä.plusYears(4))
  val p3 = primarySuoritus345("P3", alkamispäivä.plusYears(5))
  val p4 = primarySuoritus345("P4", alkamispäivä.plusYears(6))
  val p5 = primarySuoritus345("P5", alkamispäivä.plusYears(7))

  val s1 = secondaryLowerSuoritus1("S1", alkamispäivä.plusYears(8))
  val s2 = secondaryLowerSuoritus2("S2", alkamispäivä.plusYears(9))
  val s3 = secondaryLowerSuoritus3("S3", alkamispäivä.plusYears(10))
  val s4 = secondaryLowerSuoritus45("S4", alkamispäivä.plusYears(11))
  val s5 = secondaryLowerSuoritus45("S5", alkamispäivä.plusYears(12))
  val s6 = secondaryUpperSuoritusS6("S6", alkamispäivä.plusYears(13))
  val s7 = secondaryUpperSuoritusS7("S7", alkamispäivä.plusYears(14))

  val opiskeluoikeus = EuropeanSchoolOfHelsinkiOpiskeluoikeus(
    oppilaitos = Some(europeanSchoolOfHelsinki),
    lisätiedot = Some(lisätiedot),
    tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(
      List(
        EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(alkamispäivä, LukioExampleData.opiskeluoikeusAktiivinen),
        EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(päättymispäivä, LukioExampleData.opiskeluoikeusPäättynyt)
      )
    ),
    suoritukset = List(
      n1,
      n2,
      p1,
      p2JääLuokalle,
      p2,
      p3,
      p4,
      p5,
      s1,
      s2,
      s3,
      s4,
      s5,
      s6,
      s7,
    )
  )

  val examples = List(
    Example("European School of Helsinki", "European School of Helsinki", Oppija(asUusiOppija(KoskiSpecificMockOppijat.europeanSchoolOfHelsinki), List(opiskeluoikeus)))
  )

}
