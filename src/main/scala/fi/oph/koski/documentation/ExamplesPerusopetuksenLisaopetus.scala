package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.PerusopetuksenExampleData.{opiskeluoikeusLäsnä, opiskeluoikeusValmistunut}
import fi.oph.koski.documentation.PerusopetusExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

object ExamplesPerusopetuksenLisaopetus {
  private def suoritus(aine: PerusopetuksenOppiaine) = PerusopetuksenLisäopetuksenOppiaineenSuoritus(
    koulutusmoduuli = aine,
    suorituskieli = None,
    tila = tilaValmis,
    arviointi = None,
    korotus = false
  )

  val lisäopetuksenSuoritus = PerusopetuksenLisäopetuksenSuoritus(
    koulutusmoduuli = PerusopetuksenLisäopetus(),
    tila = tilaValmis,
    toimipiste = jyväskylänNormaalikoulu,
    vahvistus = vahvistus(),
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
        suoritus(oppiaine("LI")).copy(arviointi = arviointi(7), korotus = true),
        MuuPerusopetuksenLisäopetuksenSuoritus(
          MuuPerusopetuksenLisäopetuksenKoulutusmoduuli(PaikallinenKoodi("xxx", "Monialainen oppimiskokonaisuus"), "Tehtiin ryhmätyönä webbisivusto, jossa kerrotaan tupakoinnin haitoista"),
          tilaValmis,
          arviointi("S")
        )
      )
    ),
    liitetiedot = Some(List(PerusopetuksenLisäopetuksenSuorituksenLiitetiedot(
      tunniste = Koodistokoodiviite("kayttaytyminen", "perusopetuksenlisaopetuksensuorituksenliitetieto"),
      kuvaus = "Liitteenä käyttäytymisen sanallinen arvio"
    )))
  )

  val lisäopetuksenSuoritusToimintaAlueittain = PerusopetuksenLisäopetuksenSuoritus(
    koulutusmoduuli = PerusopetuksenLisäopetus(),
    tila = tilaValmis,
    toimipiste = jyväskylänNormaalikoulu,
    vahvistus = vahvistus(),
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
      tila = tilaValmis,
      arviointi = arviointi("S")
    )
  }


  val lisäopetuksenPäättötodistus = Oppija(
    exampleHenkilö,
    List(PerusopetuksenLisäopetuksenOpiskeluoikeus(
      alkamispäivä = Some(date(2008, 8, 15)),
      päättymispäivä = Some(date(2016, 6, 4)),
      oppilaitos = jyväskylänNormaalikoulu,
      koulutustoimija = None,
      lisätiedot = Some(PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(
        pidennettyOppivelvollisuus = Some(Päätösjakso(Some(date(2008, 8, 15)), Some(date(2016, 6, 4))))
      )),
      suoritukset = List(
        lisäopetuksenSuoritus
      ),
      tila = PerusopetuksenOpiskeluoikeudenTila(
        List(
          PerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), opiskeluoikeusLäsnä),
          PerusopetuksenOpiskeluoikeusjakso(date(2016, 6, 4), opiskeluoikeusValmistunut)
        )
      ),
      läsnäolotiedot = None
    ))
  )

  val lisäopetuksenPäättötodistusToimintaAlueittain = Oppija(
    exampleHenkilö,
    List(PerusopetuksenLisäopetuksenOpiskeluoikeus(
      alkamispäivä = Some(date(2008, 8, 15)),
      päättymispäivä = Some(date(2016, 6, 4)),
      oppilaitos = jyväskylänNormaalikoulu,
      koulutustoimija = None,
      lisätiedot = Some(PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot(
        pidennettyOppivelvollisuus = Some(Päätösjakso(Some(date(2008, 8, 15)), Some(date(2016, 6, 4))))
      )),
      suoritukset = List(
        lisäopetuksenSuoritusToimintaAlueittain
      ),
      tila = PerusopetuksenOpiskeluoikeudenTila(
        List(
          PerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), opiskeluoikeusLäsnä),
          PerusopetuksenOpiskeluoikeusjakso(date(2016, 6, 4), opiskeluoikeusValmistunut)
        )
      ),
      läsnäolotiedot = None
    ))
  )

  val examples = List(
    Example("perusopetuksen lisäopetus", "Opiskelija on suorittanut perusopetuksen lisäopetuksen (kymppiluokka)", lisäopetuksenPäättötodistus),
    Example("perusopetuksen lisäopetus toiminta-alueittain", "Opiskelija on suorittanut perusopetuksen lisäopetuksen toiminta-alueittain", lisäopetuksenPäättötodistusToimintaAlueittain)
  )
}
