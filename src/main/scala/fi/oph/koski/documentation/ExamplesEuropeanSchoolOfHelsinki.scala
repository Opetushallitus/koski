package fi.oph.koski.documentation

import fi.oph.koski.documentation.EuropeanSchoolOfHelsinkiExampleData._
import fi.oph.koski.documentation.ExampleData.helsinki
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.schema._

import java.time.LocalDate.{of => date}

object ExamplesEuropeanSchoolOfHelsinki {
  val alkamispaiva = date(2022, 8, 1)
  val paattymispaiva = date(2024, 5, 31)
  val lisätiedot = EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot(
    erityisenKoulutustehtävänJaksot = Some(List(ExamplesLukio.erityisenKoulutustehtävänJakso)),
    ulkomaanjaksot = Some(List(ExamplesLukio.ulkomaanjakso)),
    /*maksuttomuus = Some(
      List(
        Maksuttomuus(
          alku = alkamispaiva,
          loppu = None,
          maksuton = true
        )
      )
    ),
    oikeuttaMaksuttomuuteenPidennetty = Some(List(
      OikeuttaMaksuttomuuteenPidennetty(alkamispaiva, alkamispaiva.plusDays(9)),
      OikeuttaMaksuttomuuteenPidennetty(alkamispaiva.plusDays(19), alkamispaiva.plusDays(30))
    ))*/
  )

  val suoritusVahvistus = ExampleData.vahvistusPaikkakunnalla(paattymispaiva, europeanSchoolOfHelsinki, helsinki)

  val suoritus = DummyVuosiluokanSuoritus(
    koulutusmoduuli = EuropeanSchoolOfHelsinkiLuokkaAste(PaikallinenKoodi("foo.bar", Finnish("bar.baz"))), luokka = Some("1A"), alkamispäivä = Some(alkamispaiva), toimipiste = europeanSchoolOfHelsinki, vahvistus = None, suorituskieli = ExampleData.englanti)

  val opiskeluoikeus = EuropeanSchoolOfHelsinkiOpiskeluoikeus(
    oppilaitos = Some(europeanSchoolOfHelsinki),
    lisätiedot = Some(lisätiedot),
    tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(
      List(
        EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(alkamispaiva, LukioExampleData.opiskeluoikeusAktiivinen)
      )
    ),
    suoritukset = List(suoritus)
  )

  val examples = List(
    Example("europeanschoolofhelsinki", "European School of Helsinki", Oppija(asUusiOppija(KoskiSpecificMockOppijat.europeanSchoolOfHelsinki), List(opiskeluoikeus)))
  )
}
