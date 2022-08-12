package fi.oph.koski.fixture

import java.time.LocalDate

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.PerusopetusExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.{helsinki, _}
import fi.oph.koski.schema._

object PerusopetusOppijaMaaratRaporttiFixtures {
  private val date = LocalDate.of(2012, 1, 1)

  private val tilaLäsnä = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
    NuortenPerusopetuksenOpiskeluoikeusjakso(alku = date.minusYears(6), tila = opiskeluoikeusLäsnä)
  ))
  private val aikajakso = Aikajakso(date, Some(date.plusMonths(1)))
  private val erityisenTuenPäätös = ErityisenTuenPäätös(alku = Some(date), loppu = Some(date.plusMonths(1)), erityisryhmässä = None)
  private val lisäopetuksenSuoritukset = List(
    PerusopetuksenLisäopetuksenSuoritus(
      koulutusmoduuli = PerusopetuksenLisäopetus(perusteenDiaarinumero = Some("105/011/2014")),
      luokka = Some("10A"),
      toimipiste = jyväskylänNormaalikoulu,
      suorituskieli = suomenKieli,
      osasuoritukset = None
    )
  )

  val tavallinen = PerusopetuksenOpiskeluoikeus(
    tila = tilaLäsnä,
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(
      PerusopetuksenVuosiluokanSuoritus(
        koulutusmoduuli = PerusopetuksenLuokkaAste(6, perusopetuksenDiaarinumero),
        luokka = "6-7C",
        toimipiste = jyväskylänNormaalikoulu,
        suorituskieli = suomenKieli,
        alkamispäivä = Some(date)
      ),
      PerusopetuksenVuosiluokanSuoritus(
        koulutusmoduuli = PerusopetuksenLuokkaAste(5, perusopetuksenDiaarinumero),
        luokka = "5C",
        toimipiste = jyväskylänNormaalikoulu,
        suorituskieli = suomenKieli,
        alkamispäivä = Some(date.minusYears(1)),
        vahvistus = vahvistusPaikkakunnalla(date),
        osasuoritukset = kaikkiAineet
      )
    ),
    lisätiedot = None
  )

  val tavallinenLisäopetus = PerusopetuksenLisäopetuksenOpiskeluoikeus(
    tila = tilaLäsnä,
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = lisäopetuksenSuoritukset,
    lisätiedot = None
  )

  private val erikoisLisätiedot = PerusopetuksenOpiskeluoikeudenLisätiedot(
    pidennettyOppivelvollisuus = Some(aikajakso),
    vaikeastiVammainen = Some(List(aikajakso)),
    erityisenTuenPäätös = Some(erityisenTuenPäätös),
    majoitusetu = Some(aikajakso),
    kuljetusetu = Some(aikajakso),
    sisäoppilaitosmainenMajoitus = Some(List(aikajakso)),
    koulukoti = Some(List(aikajakso)),
    joustavaPerusopetus = Some(aikajakso)
  )

  private val erikoisLisätiedotPerusopetuksenLisäopetus = PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(
    pidennettyOppivelvollisuus = Some(aikajakso),
    vaikeastiVammainen = Some(List(aikajakso)),
    erityisenTuenPäätös = Some(erityisenTuenPäätös),
    majoitusetu = Some(aikajakso),
    kuljetusetu = Some(aikajakso),
    sisäoppilaitosmainenMajoitus = Some(List(aikajakso)),
    koulukoti = Some(List(aikajakso)),
    joustavaPerusopetus = Some(aikajakso)
  )

  val erikois = PerusopetuksenOpiskeluoikeus(
    tila = tilaLäsnä,
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(
      PerusopetuksenVuosiluokanSuoritus(
        koulutusmoduuli = PerusopetuksenLuokkaAste(8, perusopetuksenDiaarinumero),
        luokka = "8C",
        toimipiste = jyväskylänNormaalikoulu,
        suorituskieli = suomenKieli,
        alkamispäivä = Some(date)
      )
    ),
    lisätiedot = Some(erikoisLisätiedot)
  )

  val erikoisLisäopetus = PerusopetuksenLisäopetuksenOpiskeluoikeus(
    tila = tilaLäsnä,
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = lisäopetuksenSuoritukset,
    lisätiedot = Some(erikoisLisätiedotPerusopetuksenLisäopetus)
  )

  private val virheellisestiSiirrettyVaikeastiVammainenLisätiedot = PerusopetuksenOpiskeluoikeudenLisätiedot(
    pidennettyOppivelvollisuus = Some(aikajakso),
    vaikeastiVammainen = Some(List(aikajakso)),
    majoitusetu = Some(aikajakso),
    kuljetusetu = Some(aikajakso),
    sisäoppilaitosmainenMajoitus = Some(List(aikajakso)),
    koulukoti = Some(List(aikajakso)),
    joustavaPerusopetus = Some(aikajakso),
    erityisenTuenPäätökset = Some(List(ErityisenTuenPäätös(
      alku = Some(aikajakso.alku),
      loppu = aikajakso.loppu,
      erityisryhmässä = Some(false),
      tukimuodot = None
    )))
  )

  private val virheellisestiSiirrettyVaikeastiVammainenLisätiedotPerusopetuksenLisäopetus = PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(
    pidennettyOppivelvollisuus = Some(aikajakso),
    vaikeastiVammainen = Some(List(aikajakso)),
    majoitusetu = Some(aikajakso),
    kuljetusetu = Some(aikajakso),
    sisäoppilaitosmainenMajoitus = Some(List(aikajakso)),
    koulukoti = Some(List(aikajakso)),
    joustavaPerusopetus = Some(aikajakso),
    erityisenTuenPäätökset = Some(List(ErityisenTuenPäätös(
      alku = Some(aikajakso.alku),
      loppu = aikajakso.loppu,
      erityisryhmässä = Some(false),
      tukimuodot = None
    )))
  )

  val virheellisestiSiirrettyVaikeastiVammainen = PerusopetuksenOpiskeluoikeus(
    tila = tilaLäsnä,
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(
      PerusopetuksenVuosiluokanSuoritus(
        koulutusmoduuli = PerusopetuksenLuokkaAste(7, perusopetuksenDiaarinumero),
        luokka = "7C",
        toimipiste = jyväskylänNormaalikoulu,
        suorituskieli = suomenKieli,
        alkamispäivä = Some(date)
      )
    ),
    lisätiedot = Some(virheellisestiSiirrettyVaikeastiVammainenLisätiedot)
  )

  val virheellisestiSiirrettyVaikeastiVammainenLisäopetus = PerusopetuksenLisäopetuksenOpiskeluoikeus(
    tila = tilaLäsnä,
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = lisäopetuksenSuoritukset,
    lisätiedot = Some(virheellisestiSiirrettyVaikeastiVammainenLisätiedotPerusopetuksenLisäopetus)
  )

  private val virheellisestiSiirrettyVammainenLisätiedot: PerusopetuksenOpiskeluoikeudenLisätiedot = PerusopetuksenOpiskeluoikeudenLisätiedot(
    pidennettyOppivelvollisuus = Some(aikajakso),
    vammainen = Some(List(aikajakso)),
    erityisenTuenPäätökset = Some(List(ErityisenTuenPäätös(
      alku = Some(aikajakso.alku),
      loppu = aikajakso.loppu,
      erityisryhmässä = Some(false),
      tukimuodot = None
    )))
  )

  private val virheellisestiSiirrettyVammainenLisätiedotPerusopetuksenLisäopetus = PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(
    pidennettyOppivelvollisuus = Some(aikajakso),
    vammainen = Some(List(aikajakso)),
    erityisenTuenPäätökset = Some(List(ErityisenTuenPäätös(
      alku = Some(aikajakso.alku),
      loppu = aikajakso.loppu,
      erityisryhmässä = Some(false),
      tukimuodot = None
    )))
  )

  val virheellisestiSiirrettyVammainen = PerusopetuksenOpiskeluoikeus(
    tila = tilaLäsnä,
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(
      PerusopetuksenVuosiluokanSuoritus(
        koulutusmoduuli = PerusopetuksenLuokkaAste(7, perusopetuksenDiaarinumero),
        luokka = "7C",
        toimipiste = jyväskylänNormaalikoulu,
        suorituskieli = suomenKieli,
        alkamispäivä = Some(date)
      )
    ),
    lisätiedot = Some(virheellisestiSiirrettyVammainenLisätiedot)
  )

  val virheellisestiSiirrettyVammainenLisäopetus = PerusopetuksenLisäopetuksenOpiskeluoikeus(
    tila = tilaLäsnä,
    oppilaitos = Some(jyväskylänNormaalikoulu),
    koulutustoimija = None,
    suoritukset = lisäopetuksenSuoritukset,
    lisätiedot = Some(virheellisestiSiirrettyVammainenLisätiedotPerusopetuksenLisäopetus)
  )

  val eriOppilaitoksessa = PerusopetuksenOpiskeluoikeus(
    tila = tilaLäsnä,
    oppilaitos = Some(kulosaarenAlaAste),
    suoritukset = List(
      PerusopetuksenVuosiluokanSuoritus(
        koulutusmoduuli = PerusopetuksenLuokkaAste(6, perusopetuksenDiaarinumero),
        luokka = "6C",
        toimipiste = kulosaarenAlaAste,
        suorituskieli = suomenKieli,
        alkamispäivä = Some(date)
      )
    )
  )

  val eriOppilaitoksessaLisäopetus = PerusopetuksenLisäopetuksenOpiskeluoikeus(
    tila = tilaLäsnä,
    oppilaitos = Some(kulosaarenAlaAste),
    koulutustoimija = None,
    suoritukset = lisäopetuksenSuoritukset,
    lisätiedot = None
  )

  val kotiopetus = PerusopetuksenOpiskeluoikeus(
    tila = tilaLäsnä,
    oppilaitos = Some(jyväskylänNormaalikoulu),
    suoritukset = List(
      PerusopetuksenVuosiluokanSuoritus(
        koulutusmoduuli = PerusopetuksenLuokkaAste(6, perusopetuksenDiaarinumero),
        luokka = "6-7C",
        toimipiste = jyväskylänNormaalikoulu,
        suorituskieli = suomenKieli,
        alkamispäivä = Some(date)
      ),
      PerusopetuksenVuosiluokanSuoritus(
        koulutusmoduuli = PerusopetuksenLuokkaAste(5, perusopetuksenDiaarinumero),
        luokka = "5C",
        toimipiste = jyväskylänNormaalikoulu,
        suorituskieli = suomenKieli,
        alkamispäivä = Some(date.minusYears(1)),
        vahvistus = vahvistusPaikkakunnalla(date),
        osasuoritukset = kaikkiAineet
      )
    ),
    lisätiedot = Some(PerusopetuksenOpiskeluoikeudenLisätiedot(
      kotiopetusjaksot = Some(List(aikajakso))
    ))
  )

  val organisaatiohistoria = Some(List(
    OpiskeluoikeudenOrganisaatiohistoria(
      muutospäivä = date.plusYears(1),
      oppilaitos = Some(jyväskylänNormaalikoulu),
      koulutustoimija = Some(helsinki)
    )
  ))
}
