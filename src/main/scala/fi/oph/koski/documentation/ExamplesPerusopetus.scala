package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.PerusopetuksenExampleData.{opiskeluoikeusLäsnä, opiskeluoikeusValmistunut}
import fi.oph.koski.documentation.PerusopetusExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.localization.Finnish
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.schema._
object ExamplesPerusopetus {
  val ysiluokkalainen = Oppija(
    exampleHenkilö,
    List(PerusopetuksenOpiskeluoikeus(
      alkamispäivä = Some(date(2008, 8, 15)),
      päättymispäivä = None,
      oppilaitos = jyväskylänNormaalikoulu,
      koulutustoimija = None,
      suoritukset = List(
          PerusopetuksenVuosiluokanSuoritus(
            koulutusmoduuli = PerusopetuksenLuokkaAste(8), luokka = "8C", alkamispäivä = Some(date(2014, 8, 15)),
            tila = tilaValmis,
            toimipiste = jyväskylänNormaalikoulu, suorituskieli = suomenKieli,
            osasuoritukset = kaikkiAineet,
            vahvistus = vahvistus(date(2015, 5, 30))
          ),

          PerusopetuksenVuosiluokanSuoritus(
            koulutusmoduuli = PerusopetuksenLuokkaAste(9), luokka = "9C", alkamispäivä = Some(date(2015, 8, 15)),
            tila = tilaKesken,
            toimipiste = jyväskylänNormaalikoulu, suorituskieli = suomenKieli
          )
      ),
      tila = Some(PerusopetuksenOpiskeluoikeudenTila(
        List(
          PerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), None, opiskeluoikeusLäsnä)
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
        PerusopetuksenVuosiluokanSuoritus(
          koulutusmoduuli = PerusopetuksenLuokkaAste(8), luokka = "8C", alkamispäivä = Some(date(2014, 8, 15)),
          tila = tilaValmis,
          toimipiste = jyväskylänNormaalikoulu, suorituskieli = suomenKieli,
          osasuoritukset = kaikkiAineet,
          vahvistus = vahvistus(date(2015, 5, 30))
        ),

        PerusopetuksenVuosiluokanSuoritus(
          koulutusmoduuli = PerusopetuksenLuokkaAste(9), luokka = "9C", alkamispäivä = Some(date(2015, 8, 15)),
          tila = tilaValmis,
          toimipiste = jyväskylänNormaalikoulu, suorituskieli = suomenKieli,
          vahvistus = vahvistus(date(2016, 5, 30))
        ),

        PerusopetuksenOppimääränSuoritus(
          koulutusmoduuli = perusopetus,
          tila = tilaValmis,
          toimipiste = jyväskylänNormaalikoulu,
          vahvistus = vahvistus(date(2016, 6, 4)),
          suoritustapa = suoritustapaKoulutus,
          oppimäärä = perusopetuksenOppimäärä,
          osasuoritukset = kaikkiAineet
        )),
      tila = Some(PerusopetuksenOpiskeluoikeudenTila(
        List(
          PerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), Some(date(2016, 6, 3)), opiskeluoikeusLäsnä),
          PerusopetuksenOpiskeluoikeusjakso(date(2016, 6, 4), None, opiskeluoikeusValmistunut)
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
          vahvistus = vahvistus()
        )),
      tila = Some(PerusopetuksenOpiskeluoikeudenTila(
        List(
          PerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), Some(date(2016, 6, 3)), opiskeluoikeusLäsnä),
          PerusopetuksenOpiskeluoikeusjakso(date(2016, 6, 4), None, opiskeluoikeusValmistunut)
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
          suorituskieli = None,
          tila = tilaValmis,
          toimipiste = jyväskylänNormaalikoulu,
          vahvistus = vahvistus(),
          suoritustapa = suoritustapaErityinenTutkinto,
          oppimäärä = aikuistenOppimäärä,
          osasuoritukset = kaikkiAineet
        )),
      tila = Some(PerusopetuksenOpiskeluoikeudenTila(
        List(
          PerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), Some(date(2016, 6, 3)), opiskeluoikeusLäsnä),
          PerusopetuksenOpiskeluoikeusjakso(date(2016, 6, 4), None, opiskeluoikeusValmistunut)
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
          suorituskieli = None,
          tila = tilaValmis,
          toimipiste = jyväskylänNormaalikoulu,
          vahvistus = vahvistus(),
          suoritustapa = suoritustapaErityinenTutkinto,
          oppimäärä = aikuistenOppimäärä,
          osasuoritukset = Some(List(
            toimintaAlueenSuoritus("1").copy(arviointi = arviointi("S", Some(Finnish("Motoriset taidot kehittyneet hyvin perusopetuksen aikana")))),
            toimintaAlueenSuoritus("2").copy(arviointi = arviointi("S")),
            toimintaAlueenSuoritus("3").copy(arviointi = arviointi("S")),
            toimintaAlueenSuoritus("4").copy(arviointi = arviointi("S")),
            toimintaAlueenSuoritus("5").copy(arviointi = arviointi("S"))
          ))
        )),
      tila = Some(PerusopetuksenOpiskeluoikeudenTila(
        List(
          PerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), Some(date(2016, 6, 3)), opiskeluoikeusLäsnä),
          PerusopetuksenOpiskeluoikeusjakso(date(2016, 6, 4), None, opiskeluoikeusValmistunut)
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

  def toimintaAlueenSuoritus(toimintaAlue: String): PerusopetuksenToiminta_AlueenSuoritus = {
    PerusopetuksenToiminta_AlueenSuoritus(koulutusmoduuli = new PerusopetuksenToiminta_Alue(Koodistokoodiviite(toimintaAlue, "perusopetuksentoimintaalue")), tila = tilaValmis)
  }

  val examples = List(
    Example("perusopetuksen oppimäärä - ysiluokkalainen", "Oppija on suorittamassa 9. luokkaa", ysiluokkalainen),
    Example("perusopetuksen oppimäärä - päättötodistus", "Oppija on saanut perusopetuksen päättötodistuksen", päättötodistus),
    Example("perusopetuksen oppiaineen oppimäärä - päättötodistus", "Aikuisopiskelija on suorittanut peruskoulun äidinkielen oppimäärän", aineopiskelija),
    Example("aikuisten perusopetuksen oppimäärä - erityinen tutkinto", "Aikuisopiskelija on suorittanut peruskoulun oppimäärän erityisenä tutkintona", erityinenTutkintoAikuinen),
    Example("perusopetuksen oppimäärä - toiminta-alueittain opiskelija", "Oppija on suorittanut peruskoulun opiskellen toiminta-alueittain", toimintaAlueittainOpiskelija)
  )
}

object PerusopetuksenExampleData {
  val opiskeluoikeusLäsnä = Koodistokoodiviite("lasna", Some("Läsnä"), "perusopetuksenopiskeluoikeudentila", Some(1))
  val opiskeluoikeusValmistunut = Koodistokoodiviite("valmistunut", Some("Valmistunut"), "perusopetuksenopiskeluoikeudentila", Some(1))
}
