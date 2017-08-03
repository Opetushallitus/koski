package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.PerusopetusExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.localization.Finnish
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

object ExamplesPerusopetus {
  val ysiluokkalainen = Oppija(
    exampleHenkilö,
    List(ysiluokkalaisenOpiskeluoikeus)
  )

  lazy val ysiluokkalaisenOpiskeluoikeus = PerusopetuksenOpiskeluoikeus(
    alkamispäivä = Some(date(2008, 8, 15)),
    päättymispäivä = None,
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritusKesken,
      kahdeksannenLuokanSuoritus,
      yhdeksännenLuokanSuoritus
    ),
    tila = PerusopetuksenOpiskeluoikeudenTila(
      List(
        PerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), opiskeluoikeusLäsnä)
      )
    )
  )

  lazy val ysinOpiskeluoikeusKesken: PerusopetuksenOpiskeluoikeus = ysiluokkalaisenOpiskeluoikeus.copy(
    suoritukset = List(
      perusopetuksenOppimääränSuoritusKesken,
      kahdeksannenLuokanSuoritus,
      yhdeksännenLuokanSuoritus.copy(tila = tilaKesken, vahvistus = None)
    )
  )

  lazy val seiskaTuplattuOpiskeluoikeus: PerusopetuksenOpiskeluoikeus = ysiluokkalaisenOpiskeluoikeus.copy(
    alkamispäivä = Some(date(2012, 6, 15)),
    oppilaitos = Some(YleissivistavakoulutusExampleData.kulosaarenAlaAste),
    suoritukset = List(
      perusopetuksenOppimääränSuoritusKesken.copy(tila = tilaKeskeytynyt).copy(toimipiste = YleissivistavakoulutusExampleData.kulosaarenAlaAste),
      kuudennenLuokanSuoritus,
      seitsemännenLuokanTuplaus.copy(toimipiste = YleissivistavakoulutusExampleData.kulosaarenAlaAste)
    ),
    tila = PerusopetuksenOpiskeluoikeudenTila(
      List(
        PerusopetuksenOpiskeluoikeusjakso(date(2012, 6, 15), opiskeluoikeusLäsnä),
        PerusopetuksenOpiskeluoikeusjakso(date(2014, 5, 30), opiskeluoikeusEronnut)
      )
    )
  )

  lazy val päättötodistus = oppija(opiskeluoikeus = päättötodistusOpiskeluoikeus())

  lazy val toimintaAlueittainOpiskelija = Oppija(
    exampleHenkilö,
    List(PerusopetuksenOpiskeluoikeus(
      alkamispäivä = Some(date(2008, 8, 15)),
      päättymispäivä = Some(date(2016, 6, 4)),
      oppilaitos = Some(jyväskylänNormaalikoulu),
      koulutustoimija = None,
      suoritukset = List(
        NuortenPerusopetuksenOppimääränSuoritus(
          koulutusmoduuli = perusopetus,
          suorituskieli = suomenKieli,
          tila = tilaValmis,
          toimipiste = jyväskylänNormaalikoulu,
          vahvistus = vahvistusPaikkakunnalla(),
          suoritustapa = suoritustapaErityinenTutkinto,
          osasuoritukset = Some(List(
            toimintaAlueenSuoritus("1").copy(arviointi = arviointi("S", Some(Finnish("Motoriset taidot kehittyneet hyvin perusopetuksen aikana")))),
            toimintaAlueenSuoritus("2").copy(arviointi = arviointi("S")),
            toimintaAlueenSuoritus("3").copy(arviointi = arviointi("S")),
            toimintaAlueenSuoritus("4").copy(arviointi = arviointi("S")),
            toimintaAlueenSuoritus("5").copy(arviointi = arviointi("S"))
          ))
        )),
      tila = PerusopetuksenOpiskeluoikeudenTila(
        List(
          PerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), opiskeluoikeusLäsnä),
          PerusopetuksenOpiskeluoikeusjakso(date(2016, 6, 4), opiskeluoikeusValmistunut)
        )
      ),
      lisätiedot = Some(PerusopetuksenOpiskeluoikeudenLisätiedot(
        erityisenTuenPäätös = Some(ErityisenTuenPäätös(
          alku = Some(date(2008, 8, 15)),
          loppu = Some(date(2016, 6, 4)),
          opiskeleeToimintaAlueittain = true,
          erityisryhmässä = true
        )),
        perusopetuksenAloittamistaLykätty = true,
        aloittanutEnnenOppivelvollisuutta = false,
        pidennettyOppivelvollisuus = Some(Päätösjakso(Some(date(2008, 8, 15)), Some(date(2016, 6, 4)))),
        tukimuodot = Some(List(Koodistokoodiviite("1", Some("Osa-aikainen erityisopetus"), "perusopetuksentukimuoto"))),
        tehostetunTuenPäätös = Some(Päätösjakso(Some(date(2008, 8, 15)), Some(date(2016, 6, 4)))),
        joustavaPerusopetus = Some(Päätösjakso(Some(date(2008, 8, 15)), Some(date(2016, 6, 4)))),
        kotiopetus = Some(Päätösjakso(Some(date(2008, 8, 15)), Some(date(2016, 6, 4)))),
        ulkomailla = Some(Päätösjakso(Some(date(2008, 8, 15)), Some(date(2016, 6, 4)))),
        vuosiluokkiinSitoutumatonOpetus = true,
        vammainen = true,
        vaikeastiVammainen = true,
        majoitusetu = Some(Päätösjakso(Some(date(2008, 8, 15)), Some(date(2016, 6, 4)))),
        kuljetusetu = Some(Päätösjakso(Some(date(2008, 8, 15)), Some(date(2016, 6, 4)))),
        oikeusMaksuttomaanAsuntolapaikkaan = Some(Päätösjakso(Some(date(2008, 8, 15)), Some(date(2016, 6, 4)))),
        sisäoppilaitosmainenMajoitus = Some(List(Majoitusjakso(date(2012, 9, 1), Some(date(2013, 9, 1)))))
      ))
    ))
  )

  def toimintaAlueenSuoritus(toimintaAlue: String): PerusopetuksenToiminta_AlueenSuoritus = {
    PerusopetuksenToiminta_AlueenSuoritus(koulutusmoduuli = new PerusopetuksenToiminta_Alue(Koodistokoodiviite(toimintaAlue, "perusopetuksentoimintaalue")), tila = tilaValmis)
  }

  val examples = List(
    Example("perusopetuksen oppimäärä - ysiluokkalainen", "Oppija on suorittamassa 9. luokkaa", ysiluokkalainen),
    Example("perusopetuksen oppimäärä - päättötodistus", "Oppija on saanut perusopetuksen päättötodistuksen", päättötodistus),
    Example("perusopetuksen oppimäärä - toiminta-alueittain opiskelija", "Oppija on suorittanut peruskoulun opiskellen toiminta-alueittain", toimintaAlueittainOpiskelija)
  )
}