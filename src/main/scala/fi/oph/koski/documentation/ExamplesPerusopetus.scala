package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.PerusopetusExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema.{Finnish, _}

object ExamplesPerusopetus {
  val ysiluokkalainen = Oppija(
    exampleHenkilö,
    List(ysiluokkalaisenOpiskeluoikeus)
  )

  lazy val aineopiskelija = Oppija(
    asUusiOppija(MockOppijat.eero),
    List(PerusopetuksenOpiskeluoikeus(
      oppilaitos = Some(jyväskylänNormaalikoulu),
      suoritukset = List(
        NuortenPerusopetuksenOppiaineenOppimääränSuoritus(
          koulutusmoduuli = PerusopetusExampleData.äidinkieli("AI1", diaarinumero = Some(perusopetuksenDiaarinumero)),
          toimipiste = jyväskylänNormaalikoulu,
          arviointi = arviointi(9),
          suoritustapa = suoritustapaErityinenTutkinto,
          vahvistus = vahvistusPaikkakunnalla(),
          suorituskieli = suomenKieli
        )),
      tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
        List(
          NuortenPerusopetuksenOpiskeluoikeusjakso(date(2015, 8, 15), opiskeluoikeusLäsnä),
          NuortenPerusopetuksenOpiskeluoikeusjakso(date(2016, 6, 4), opiskeluoikeusValmistunut)
        )
      ),
      lisätiedot = Some(PerusopetuksenOpiskeluoikeudenLisätiedot(vaikeastiVammainen = Some(List(Aikajakso(date(2014, 6, 6), None)))))
    ))
  )

  lazy val ysiluokkalaisenOpiskeluoikeus = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(
      perusopetuksenOppimääränSuoritusKesken,
      kahdeksannenLuokanSuoritus,
      yhdeksännenLuokanSuoritus
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), opiskeluoikeusLäsnä)
      )
    )
  )

  lazy val ysinOpiskeluoikeusKesken: PerusopetuksenOpiskeluoikeus = ysiluokkalaisenOpiskeluoikeus.copy(
    suoritukset = List(
      perusopetuksenOppimääränSuoritusKesken,
      kahdeksannenLuokanSuoritus,
      yhdeksännenLuokanSuoritus.copy(vahvistus = None)
    )
  )

  lazy val seiskaTuplattuOpiskeluoikeus: PerusopetuksenOpiskeluoikeus = ysiluokkalaisenOpiskeluoikeus.copy(
    oppilaitos = Some(YleissivistavakoulutusExampleData.kulosaarenAlaAste),
    suoritukset = List(
      perusopetuksenOppimääränSuoritusKesken.copy(toimipiste = YleissivistavakoulutusExampleData.kulosaarenAlaAste),
      kuudennenLuokanSuoritus,
      seitsemännenLuokanLuokallejääntiSuoritus.copy(toimipiste = YleissivistavakoulutusExampleData.kulosaarenAlaAste)
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 6, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2014, 5, 30), opiskeluoikeusEronnut)
      )
    )
  )

  lazy val päättötodistus = oppija(opiskeluoikeus = päättötodistusOpiskeluoikeus())

  lazy val erityisenTuenPäätös = ErityisenTuenPäätös(
    alku = Some(date(2008, 8, 15)),
    loppu = Some(date(2016, 6, 4)),
    opiskeleeToimintaAlueittain = true,
    erityisryhmässä = Some(true)
  )

  lazy val toimintaAlueittainOpiskelija = Oppija(
    exampleHenkilö,
    List(PerusopetuksenOpiskeluoikeus(
      oppilaitos = Some(jyväskylänNormaalikoulu),
      koulutustoimija = None,
      suoritukset = List(
        NuortenPerusopetuksenOppimääränSuoritus(
          koulutusmoduuli = perusopetus,
          suorituskieli = suomenKieli,
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
      tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
        List(
          NuortenPerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), opiskeluoikeusLäsnä),
          NuortenPerusopetuksenOpiskeluoikeusjakso(date(2016, 6, 4), opiskeluoikeusValmistunut)
        )
      ),
      lisätiedot = Some(PerusopetuksenOpiskeluoikeudenLisätiedot(
        erityisenTuenPäätös = Some(erityisenTuenPäätös),
        erityisenTuenPäätökset = Some(List(erityisenTuenPäätös)),
        perusopetuksenAloittamistaLykätty = true,
        aloittanutEnnenOppivelvollisuutta = false,
        pidennettyOppivelvollisuus = Some(Aikajakso(date(2008, 8, 15), Some(date(2016, 6, 4)))),
        tukimuodot = Some(List(Koodistokoodiviite("1", Some("Osa-aikainen erityisopetus"), "perusopetuksentukimuoto"))),
        tehostetunTuenPäätökset = Some(List(Aikajakso(date(2008, 8, 15), Some(date(2016, 6, 4))))),
        joustavaPerusopetus = Some(Aikajakso(date(2008, 8, 15), Some(date(2016, 6, 4)))),
        kotiopetus = Some(Aikajakso(date(2008, 8, 15), Some(date(2016, 6, 4)))),
        ulkomailla = Some(Aikajakso(date(2008, 8, 15), Some(date(2016, 6, 4)))),
        vuosiluokkiinSitoutumatonOpetus = true,
        vammainen = Some(List(Aikajakso(date(2010, 8, 14), None))),
        vaikeastiVammainen = Some(List(Aikajakso(date(2010, 8, 14), None))),
        majoitusetu = Some(Aikajakso(date(2008, 8, 15), Some(date(2016, 6, 4)))),
        kuljetusetu = Some(Aikajakso(date(2008, 8, 15), Some(date(2016, 6, 4)))),
        oikeusMaksuttomaanAsuntolapaikkaan = Some(Aikajakso(date(2008, 8, 15), Some(date(2016, 6, 4)))),
        sisäoppilaitosmainenMajoitus = Some(List(Aikajakso(date(2012, 9, 1), Some(date(2013, 9, 1))))),
        koulukoti = Some(List(Aikajakso(date(2013, 9, 1), Some(date(2014, 9, 1)))))
      ))
    ))
  )

  def toimintaAlueenSuoritus(toimintaAlue: String): PerusopetuksenToiminta_AlueenSuoritus = {
    PerusopetuksenToiminta_AlueenSuoritus(koulutusmoduuli = new PerusopetuksenToiminta_Alue(Koodistokoodiviite(toimintaAlue, "perusopetuksentoimintaalue")))
  }

  val examples = List(
    Example("perusopetuksen oppimäärä - ysiluokkalainen", "Oppija on suorittamassa 9. luokkaa", ysiluokkalainen),
    Example("perusopetuksen oppimäärä - päättötodistus", "Oppija on saanut perusopetuksen päättötodistuksen", päättötodistus),
    Example("perusopetuksen oppimäärä - toiminta-alueittain opiskelija", "Oppija on suorittanut peruskoulun opiskellen toiminta-alueittain", toimintaAlueittainOpiskelija),
    Example("nuorten perusopetuksen oppiaineen oppimäärä - päättötodistus", "Oppija on suorittanut peruskoulun äidinkielen oppimäärän", aineopiskelija)
  )

  lazy val nuortenPerusopetuksenOppiaineenOppimääränSuoritus = NuortenPerusopetuksenOppiaineenOppimääränSuoritus(
    koulutusmoduuli = PerusopetusExampleData.äidinkieli("AI1", diaarinumero = Some(perusopetuksenDiaarinumero)),
    toimipiste = jyväskylänNormaalikoulu,
    arviointi = arviointi(9),
    suoritustapa = suoritustapaErityinenTutkinto,
    vahvistus = vahvistusPaikkakunnalla(),
    suorituskieli = suomenKieli
  )
}
