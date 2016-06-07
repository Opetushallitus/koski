package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.PerusopetusExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.schema._

object ExamplesPerusopetus {
  private val ysiluokanSuoritus = PerusopetuksenVuosiluokanSuoritus(
    luokkaAste = 9, luokka = "9C", alkamispäivä = Some(date(2008, 8, 15)),
    paikallinenId = None, tila = tilaKesken, toimipiste = jyväskylänNormaalikoulu, suorituskieli = suomenKieli,
    koulutusmoduuli = perusopetus
  )

  val ysiluokkalainen = Oppija(
    exampleHenkilö,
    List(PerusopetuksenOpiskeluoikeus(
      alkamispäivä = Some(date(2008, 8, 15)),
      päättymispäivä = None,
      oppilaitos = jyväskylänNormaalikoulu,
      koulutustoimija = None,
      suoritukset = List(ysiluokanSuoritus),
      tila = Some(YleissivistäväOpiskeluoikeudenTila(
        List(
          YleissivistäväOpiskeluoikeusjakso(date(2008, 8, 15), None, opiskeluoikeusAktiivinen)
        )
      )),
      tavoite = tavoiteKokoOppimäärä,
      läsnäolotiedot = None
    ))
  )
  val päättötodistus = Oppija(
    exampleHenkilö,
    List(PerusopetuksenOpiskeluoikeus(
      alkamispäivä = Some(date(2008, 8, 15)),
      päättymispäivä = Some(date(2016, 6, 4)),
      oppilaitos = jyväskylänNormaalikoulu,
      koulutustoimija = None,
      suoritukset = List(
        PerusopetuksenOppimääränSuoritus(
          koulutusmoduuli = perusopetus,
          tila = tilaValmis,
          toimipiste = jyväskylänNormaalikoulu,
          vahvistus = vahvistus,
          suoritustapa = suoritustapaKoulutus,
          oppimäärä = perusopetuksenOppimäärä,
          osasuoritukset = kaikkiAineet
        )),
      tila = Some(YleissivistäväOpiskeluoikeudenTila(
        List(
          YleissivistäväOpiskeluoikeusjakso(date(2008, 8, 15), Some(date(2016, 6, 3)), opiskeluoikeusAktiivinen),
          YleissivistäväOpiskeluoikeusjakso(date(2016, 6, 4), None, opiskeluoikeusPäättynyt)
        )
      )),
      tavoite = tavoiteKokoOppimäärä,
      läsnäolotiedot = None
    ))
  )

  val aineopiskelija = Oppija(
    MockOppijat.eero.vainHenkilötiedot,
    List(PerusopetuksenOpiskeluoikeus(
      alkamispäivä = Some(date(2008, 8, 15)),
      oppilaitos = jyväskylänNormaalikoulu,
      koulutustoimija = None,
      suoritukset = List(
        PerusopetuksenOppiaineenOppimääränSuoritus(
          koulutusmoduuli = äidinkieli("AI1"),
          tila = tilaValmis,
          toimipiste = jyväskylänNormaalikoulu,
          arviointi = arviointi(9),
          vahvistus = vahvistus
        )),
      tila = Some(YleissivistäväOpiskeluoikeudenTila(
        List(
          YleissivistäväOpiskeluoikeusjakso(date(2008, 8, 15), Some(date(2016, 6, 3)), opiskeluoikeusAktiivinen),
          YleissivistäväOpiskeluoikeusjakso(date(2016, 6, 4), None, opiskeluoikeusPäättynyt)
        )
      )),
      tavoite = tavoiteAine,
      läsnäolotiedot = None
    ))
  )

  val erityinenTutkintoAikuinen = Oppija(
    exampleHenkilö,
    List(PerusopetuksenOpiskeluoikeus(
      alkamispäivä = Some(date(2008, 8, 15)),
      päättymispäivä = Some(date(2016, 6, 4)),
      oppilaitos = jyväskylänNormaalikoulu,
      koulutustoimija = None,
      suoritukset = List(
        PerusopetuksenOppimääränSuoritus(
          koulutusmoduuli = perusopetus,
          paikallinenId = None,
          suorituskieli = None,
          tila = tilaValmis,
          toimipiste = jyväskylänNormaalikoulu,
          vahvistus = vahvistus,
          suoritustapa = suoritustapaErityinenTutkinto,
          oppimäärä = aikuistenOppimäärä,
          osasuoritukset = kaikkiAineet
        )),
      tila = Some(YleissivistäväOpiskeluoikeudenTila(
        List(
          YleissivistäväOpiskeluoikeusjakso(date(2008, 8, 15), Some(date(2016, 6, 3)), opiskeluoikeusAktiivinen),
          YleissivistäväOpiskeluoikeusjakso(date(2016, 6, 4), None, opiskeluoikeusPäättynyt)
        )
      )),
      tavoite = tavoiteKokoOppimäärä,
      läsnäolotiedot = None
    ))
  )

  val toimintaAlueittainOpiskelija = Oppija(
    exampleHenkilö,
    List(PerusopetuksenOpiskeluoikeus(
      alkamispäivä = Some(date(2008, 8, 15)),
      päättymispäivä = Some(date(2016, 6, 4)),
      oppilaitos = jyväskylänNormaalikoulu,
      koulutustoimija = None,
      suoritukset = List(
        PerusopetuksenOppimääränSuoritus(
          koulutusmoduuli = perusopetus,
          paikallinenId = None,
          suorituskieli = None,
          tila = tilaValmis,
          toimipiste = jyväskylänNormaalikoulu,
          vahvistus = vahvistus,
          suoritustapa = suoritustapaErityinenTutkinto,
          oppimäärä = aikuistenOppimäärä,
          osasuoritukset = Some(List(
            toimintaAlueenSuoritus("1").copy(arviointi = arviointi("S")),
            toimintaAlueenSuoritus("2").copy(arviointi = arviointi("S")),
            toimintaAlueenSuoritus("3").copy(arviointi = arviointi("S")),
            toimintaAlueenSuoritus("4").copy(arviointi = arviointi("S")),
            toimintaAlueenSuoritus("5").copy(arviointi = arviointi("S"))
          ))
        )),
      tila = Some(YleissivistäväOpiskeluoikeudenTila(
        List(
          YleissivistäväOpiskeluoikeusjakso(date(2008, 8, 15), Some(date(2016, 6, 3)), opiskeluoikeusAktiivinen),
          YleissivistäväOpiskeluoikeusjakso(date(2016, 6, 4), None, opiskeluoikeusPäättynyt)
        )
      )),
      tavoite = tavoiteKokoOppimäärä,
      läsnäolotiedot = None,
      lisätiedot = Some(PerusopetuksenOpiskeluoikeudenLisätiedot(erityisenTuenPäätös = Some(ErityisenTuenPäätös(
        alku = Some(date(2008, 8, 15)),
        loppu = Some(date(2016, 6, 4)),
        opiskeleeToimintaAlueittain = true,
        erityisryhmässä = true
      ))))
    ))
  )

  def toimintaAlueenSuoritus(toimintaAlue: String): PerusopetuksenToimintaAlueenSuoritus = {
    PerusopetuksenToimintaAlueenSuoritus(koulutusmoduuli = new PerusopetuksenToimintaAlue(toimintaAlue), tila = tilaValmis)
  }

  val examples = List(
    Example("perusopetuksen oppimäärä - ysiluokkalainen", "Oppija on suorittamassa 9. luokkaa", ysiluokkalainen),
    Example("perusopetuksen oppimäärä - päättötodistus", "Oppija on saanut perusopetuksen päättötodistuksen", päättötodistus),
    Example("perusopetuksen oppiaineen oppimäärä - päättötodistus", "Aikuisopiskelija on suorittanut peruskoulun äidinkielen oppimäärän", aineopiskelija),
    Example("aikuisten perusopetuksen oppimäärä - erityinen tutkinto", "Aikuisopiskelija on suorittanut peruskoulun oppimäärän erityisenä tutkintona", erityinenTutkintoAikuinen),
    Example("perusopetuksen oppimäärä - toiminta-alueittain opiskelija", "Oppija on suorittanut peruskoulun opiskellen toiminta-alueittain", toimintaAlueittainOpiskelija)
  )
}
