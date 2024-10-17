package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.PerusopetusExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema.{Finnish, _}

import java.time.LocalDate

object ExamplesPerusopetus {
  val ysiluokkalainen = Oppija(
    exampleHenkilö,
    List(ysiluokkalaisenOpiskeluoikeus)
  )

  lazy val aineopiskelija = Oppija(
    asUusiOppija(KoskiSpecificMockOppijat.eero),
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
      lisätiedot = Some(PerusopetuksenOpiskeluoikeudenLisätiedot(
        pidennettyOppivelvollisuus = Some(Aikajakso(date(2014, 6, 6), None)),
        vaikeastiVammainen = Some(List(Aikajakso(date(2014, 6, 6), None))),
        erityisenTuenPäätökset = Some(List(ErityisenTuenPäätös(
          alku = Some(date(2014, 6, 6)),
          loppu = None,
          erityisryhmässä = Some(false),
          tukimuodot = None,
          opiskeleeToimintaAlueittain = true
        )))
      ))
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
      kuudennenLuokanOsaAikainenErityisopetusSuoritus,
      seitsemännenLuokanLuokallejääntiSuoritus.copy(toimipiste = YleissivistavakoulutusExampleData.kulosaarenAlaAste)
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2012, 6, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2015, 1, 1), opiskeluoikeusEronnut)
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

  lazy val osaAikainenErityisopetus = Koodistokoodiviite("1", Some("Osa-aikainen erityisopetus"), "perusopetuksentukimuoto")
  lazy val tukiopetus = Koodistokoodiviite("2", Some("Tukiopetus"), "perusopetuksentukimuoto")
  lazy val tehostetunTuenPäätös = TehostetunTuenPäätös(date(2008, 8, 15), Some(date(2016, 6, 4)), Some(List(tukiopetus)))
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
          vahvistus = vahvistusPaikkakunnalla(LocalDate.of(2024, 10, 18)),
          suoritustapa = suoritustapaErityinenTutkinto,
          osasuoritukset = Some(List(
            toimintaAlueenSuoritus("1").copy(arviointi = arviointi("S", Some(Finnish("Motoriset taidot kehittyneet hyvin perusopetuksen aikana")))),
            toimintaAlueenSuoritus("2").copy(arviointi = arviointi("S", kuvaus = None)),
            toimintaAlueenSuoritus("3").copy(arviointi = arviointi("S", kuvaus = None)),
            toimintaAlueenSuoritus("4").copy(arviointi = arviointi("S", kuvaus = None)),
            toimintaAlueenSuoritus("5").copy(arviointi = arviointi("S", kuvaus = None))
          ))
        )),
      tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
        List(
          NuortenPerusopetuksenOpiskeluoikeusjakso(date(2017, 8, 15), opiskeluoikeusLäsnä),
          NuortenPerusopetuksenOpiskeluoikeusjakso(date(2024, 10, 18), opiskeluoikeusValmistunut)
        )
      ),
      lisätiedot = Some(PerusopetuksenOpiskeluoikeudenLisätiedot(
        erityisenTuenPäätös = Some(ErityisenTuenPäätös(
          alku = Some(date(2017, 8, 15)),
          loppu = Some(date(2024, 10, 18)),
          opiskeleeToimintaAlueittain = true,
          erityisryhmässä = Some(true)
        )),
        erityisenTuenPäätökset = Some(List(ErityisenTuenPäätös(
          alku = Some(date(2017, 8, 15)),
          loppu = Some(date(2024, 10, 18)),
          opiskeleeToimintaAlueittain = true,
          erityisryhmässä = Some(true)
        ))),
        perusopetuksenAloittamistaLykätty = None,
        aloittanutEnnenOppivelvollisuutta = false,
        pidennettyOppivelvollisuus = Some(Aikajakso(date(2019, 8, 15), Some(date(2024, 10, 18)))),
        joustavaPerusopetus = Some(Aikajakso(date(2017, 8, 15), Some(date(2024, 10, 18)))),
        kotiopetusjaksot = Some(List(Aikajakso(date(2017, 8, 15), Some(date(2024, 10, 18))), Aikajakso(date(2026, 7, 14), Some(date(2026, 10, 18))))),
        ulkomaanjaksot = Some(List(Aikajakso(date(2017, 8, 15), Some(date(2024, 10, 18))), Aikajakso(date(2027, 9, 16), Some(date(2028, 10, 2))))),
        vuosiluokkiinSitoutumatonOpetus = true,
        vammainen = Some(List(Aikajakso(date(2019, 8, 15), Some(date(2019, 9, 1))))),
        vaikeastiVammainen = Some(List(Aikajakso(date(2019, 9, 2), Some(date(2024, 10, 18))))),
        majoitusetu = Some(Aikajakso(date(2017, 8, 15), Some(date(2024, 10, 18)))),
        kuljetusetu = Some(Aikajakso(date(2017, 8, 15), Some(date(2024, 10, 18)))),
        sisäoppilaitosmainenMajoitus = Some(List(Aikajakso(date(2021, 9, 1), Some(date(2022, 9, 1))))),
        koulukoti = Some(List(Aikajakso(date(2022, 9, 1), Some(date(2023, 9, 1)))))
      ))
    ))
  )

  def toimintaAlueenSuoritus(toimintaAlue: String, laajuus: Option[Double] = Some(5.0)): PerusopetuksenToiminta_AlueenSuoritus = {
    PerusopetuksenToiminta_AlueenSuoritus(
      koulutusmoduuli = new PerusopetuksenToiminta_Alue(
        tunniste = Koodistokoodiviite(toimintaAlue, "perusopetuksentoimintaalue"),
        laajuus = laajuus.map(n => LaajuusVuosiviikkotunneissa(n)),
      )
    )
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

  lazy val useampiNuortenPerusopetuksenOppiaineenOppimääränSuoritusSamassaOppiaineessaEriLuokkaAsteella = PerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = List(
      NuortenPerusopetuksenOppiaineenOppimääränSuoritus(
        koulutusmoduuli = PerusopetusExampleData.äidinkieli("AI1", diaarinumero = Some(perusopetuksenDiaarinumero)),
        toimipiste = jyväskylänNormaalikoulu,
        arviointi = arviointi(9),
        suoritustapa = suoritustapaErityinenTutkinto,
        luokkaAste = Some(Koodistokoodiviite("6", "perusopetuksenluokkaaste")),
        vahvistus = vahvistusPaikkakunnalla(),
        suorituskieli = suomenKieli
      ),
      NuortenPerusopetuksenOppiaineenOppimääränSuoritus(
        koulutusmoduuli = PerusopetusExampleData.äidinkieli("AI1", diaarinumero = Some(perusopetuksenDiaarinumero)),
        toimipiste = jyväskylänNormaalikoulu,
        arviointi = arviointi(9),
        suoritustapa = suoritustapaErityinenTutkinto,
        luokkaAste = Some(Koodistokoodiviite("7", "perusopetuksenluokkaaste")),
        vahvistus = vahvistusPaikkakunnalla(),
        suorituskieli = suomenKieli
      )),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2016, 6, 4), opiskeluoikeusValmistunut)
      )
    )
  )
}
