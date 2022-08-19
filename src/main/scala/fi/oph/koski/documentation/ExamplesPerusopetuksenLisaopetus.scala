package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.PerusopetusExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

object ExamplesPerusopetuksenLisaopetus {
  def suoritus(aine: NuortenPerusopetuksenOppiaine) = PerusopetuksenLisäopetuksenOppiaineenSuoritus(
    koulutusmoduuli = aine,
    suorituskieli = None,
    arviointi = None,
    korotus = false
  )

  val lisäopetuksenSuoritus = PerusopetuksenLisäopetuksenSuoritus(
    koulutusmoduuli = PerusopetuksenLisäopetus(perusteenDiaarinumero = Some("105/011/2014")),
    luokka = Some("10A"),
    toimipiste = jyväskylänNormaalikoulu,
    vahvistus = vahvistusPaikkakunnalla(),
    suorituskieli = suomenKieli,
    osasuoritukset = Some(
      List(
        suoritus(äidinkieli("AI1")).copy(arviointi = arviointi(7), korotus = true),
        suoritus(kieli("A1", "EN")).copy(arviointi = arviointi(10), korotus = true),
        suoritus(kieli("B1", "SV")).copy(arviointi = arviointi(6), korotus = true),
        suoritus(oppiaine("MA")).copy(arviointi = arviointi(6), korotus = true),
        suoritus(oppiaine("BI")).copy(arviointi = arviointi(10), korotus = true),
        suoritus(oppiaine("GE")).copy(arviointi = arviointi(9), korotus = true),
        suoritus(oppiaine("FY")).copy(arviointi = arviointi(8), korotus = true),
        suoritus(oppiaine("KE")).copy(arviointi = arviointi(9), korotus = true),
        suoritus(oppiaine("TE")).copy(arviointi = arviointi(8), korotus = true),
        suoritus(oppiaine("HI")).copy(arviointi = arviointi(7), korotus = false),
        suoritus(oppiaine("YH")).copy(arviointi = arviointi(8), korotus = true),
        suoritus(oppiaine("KU")).copy(arviointi = arviointi(8), korotus = false),
        suoritus(oppiaine("LI")).copy(arviointi = arviointi(7), korotus = true, yksilöllistettyOppimäärä = true),
        MuuPerusopetuksenLisäopetuksenSuoritus(
          MuuPerusopetuksenLisäopetuksenKoulutusmoduuli(PaikallinenKoodi("xxx", "Monialainen oppimiskokonaisuus"), "Tehtiin ryhmätyönä webbisivusto, jossa kerrotaan tupakoinnin haitoista"),
          arviointi("S", kuvaus = None)
        )
      )
    )
  )

  val lisäopetuksenSuoritusToimintaAlueittain = PerusopetuksenLisäopetuksenSuoritus(
    koulutusmoduuli = PerusopetuksenLisäopetus(perusteenDiaarinumero = Some("105/011/2014")),
    toimipiste = jyväskylänNormaalikoulu,
    vahvistus = vahvistusPaikkakunnalla(),
    suorituskieli = suomenKieli,
    osaAikainenErityisopetus = Some(true),
    osasuoritukset = Some(
      List(
        toimintaAlueenSuoritus("1"),
        toimintaAlueenSuoritus("2"),
        toimintaAlueenSuoritus("3"),
        toimintaAlueenSuoritus("4"),
        toimintaAlueenSuoritus("5")
      )
    )
  )

  private def toimintaAlueenSuoritus(alue: String) = {
    PerusopetuksenLisäopetuksenToiminta_AlueenSuoritus(
      koulutusmoduuli = new PerusopetuksenToiminta_Alue(Koodistokoodiviite(alue, "perusopetuksentoimintaalue")),
      arviointi = arviointi("S", kuvaus = None)
    )
  }


  val lisäopetuksenOpiskeluoikeus = PerusopetuksenLisäopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    lisätiedot = Some(PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(
      pidennettyOppivelvollisuus = Some(Aikajakso(date(2008, 8, 15), Some(date(2016, 6, 4)))),
      vammainen = Some(List(Aikajakso(date(2008, 8, 15), Some(date(2016, 6, 4))))),
      erityisenTuenPäätökset = Some(List(ErityisenTuenPäätös(
        alku = Some(date(2008,8, 15)),
        loppu = Some(date(2016, 6, 4)),
        erityisryhmässä = Some(false),
        tukimuodot = None
      )))
    )),
    suoritukset = List(
      lisäopetuksenSuoritus
    ),
    tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
      List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(date(2016, 6, 4), opiskeluoikeusValmistunut)
      )
    )
  )

  val lisäopetuksenPäättötodistus = Oppija(
    exampleHenkilö,
    List(lisäopetuksenOpiskeluoikeus)
  )

  val lisäopetuksenPäättötodistusToimintaAlueittain = Oppija(
    exampleHenkilö,
    List(PerusopetuksenLisäopetuksenOpiskeluoikeus(
      oppilaitos = Some(jyväskylänNormaalikoulu),
      koulutustoimija = None,
      lisätiedot = Some(PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(
        pidennettyOppivelvollisuus = Some(Aikajakso(date(2008, 8, 15), Some(date(2016, 6, 4)))),
        vammainen = Some(List(Aikajakso(date(2008, 8, 15), Some(date(2016, 6, 4))))),
        erityisenTuenPäätökset = Some(List(ErityisenTuenPäätös(
          alku = Some(date(2008, 8, 15)),
          loppu = Some(date(2016, 6, 4)),
          opiskeleeToimintaAlueittain = true,
          erityisryhmässä = Some(false)
        )))
      )),
      suoritukset = List(
        lisäopetuksenSuoritusToimintaAlueittain
      ),
      tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
        List(
          NuortenPerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), opiskeluoikeusLäsnä),
          NuortenPerusopetuksenOpiskeluoikeusjakso(date(2016, 6, 4), opiskeluoikeusValmistunut)
        )
      )
    ))
  )

  val examples = List(
    Example("perusopetuksen lisäopetus", "Opiskelija on suorittanut perusopetuksen lisäopetuksen (kymppiluokka)", lisäopetuksenPäättötodistus),
    Example("perusopetuksen lisäopetus toiminta-alueittain", "Opiskelija on suorittanut perusopetuksen lisäopetuksen toiminta-alueittain", lisäopetuksenPäättötodistusToimintaAlueittain)
  )
}
