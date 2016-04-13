package fi.oph.tor.documentation

import java.time.LocalDate.{of => date}

import fi.oph.tor.documentation.ExampleData._
import fi.oph.tor.documentation.YleissivistavakoulutusExampleData._
import fi.oph.tor.documentation.PeruskoulutusExampleData._
import fi.oph.tor.oppija.MockOppijat
import fi.oph.tor.schema._
import fi.oph.tor.localization.LocalizedString._

object PeruskoulutusExampleData {

  def suoritus(aine: PeruskoulunOppiaine) = PeruskoulunOppiaineenSuoritus(
    koulutusmoduuli = aine,
    paikallinenId = None,
    suorituskieli = None,
    tila = tilaValmis,
    arviointi = None,
    vahvistus = None
  )

  def vuosiviikkotuntia(määrä: Double): Some[Laajuus] = Some(Laajuus(määrä.toFloat, Koodistokoodiviite("3", Some("Vuosiviikkotuntia"), "opintojenlaajuusyksikko")))

  val exampleHenkilö = MockOppijat.koululainen.vainHenkilötiedot
}

object ExamplesPeruskoulutus {
  val uusi = TorOppija(
    exampleHenkilö,
    List(PerusopetuksenOpiskeluoikeus(
      id = None,
      versionumero = None,
      lähdejärjestelmänId = None,
      alkamispäivä = Some(date(2016, 9, 1)),
      arvioituPäättymispäivä = Some(date(2020, 5, 1)),
      päättymispäivä = None,
      oppilaitos = jyväskylänNormaalikoulu, None,
      suoritukset = Nil,
      opiskeluoikeudenTila = Some(OpiskeluoikeudenTila(
        List(
          Opiskeluoikeusjakso(date(2012, 9, 1), Some(date(2016, 1, 9)), opiskeluoikeusAktiivinen, None),
          Opiskeluoikeusjakso(date(2016, 1, 10), None, opiskeluoikeusPäättynyt, None)
        )
      )),
      läsnäolotiedot = None
    ))
  )
  private val vahvistus: Some[Vahvistus] = Some(Vahvistus(päivä = date(2016, 6, 4), myöntäjäOrganisaatio = Some(jyväskylänNormaalikoulu), myöntäjäHenkilöt = Some(List(OrganisaatioHenkilö("Reijo Reksi", "rehtori", jyväskylänNormaalikoulu)))))

  val päättötodistus = TorOppija(
    exampleHenkilö,
    List(PerusopetuksenOpiskeluoikeus(
      id = None,
      versionumero = None,
      lähdejärjestelmänId = None,
      alkamispäivä = Some(date(2007, 8, 15)),
      arvioituPäättymispäivä = Some(date(2016, 6, 4)),
      päättymispäivä = Some(date(2016, 6, 4)),
      oppilaitos = jyväskylänNormaalikoulu, None,
      suoritukset = List(
        PeruskoulunPäättötodistus(
          koulutusmoduuli = Peruskoulutus(),
          paikallinenId = None,
          suorituskieli = None,
          tila = tilaValmis,
          toimipiste = jyväskylänNormaalikoulu,
          vahvistus = vahvistus,
          osasuoritukset = Some(
            List(
              suoritus(äidinkieli("AI1")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
              suoritus(kieli("B1", "SV")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(8)),
              suoritus(kieli("B1", "SV").copy(pakollinen = false, laajuus = vuosiviikkotuntia(1))).copy(vahvistus = vahvistus).copy(arviointi = hyväksytty),
              suoritus(kieli("A1", "EN")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(8)),
              suoritus(uskonto("KT1")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(10)),
              suoritus(oppiaine("HI")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(8)),
              suoritus(oppiaine("YH")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(10)),
              suoritus(oppiaine("MA")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
              suoritus(oppiaine("KE")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(7)),
              suoritus(oppiaine("FY")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
              suoritus(oppiaine("BI")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
              suoritus(oppiaine("GE")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
              suoritus(oppiaine("MU")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(7)),
              suoritus(oppiaine("KU")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(8)),
              suoritus(oppiaine("KO")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(8)),
              suoritus(oppiaine("KO").copy(pakollinen = false, laajuus = vuosiviikkotuntia(1))).copy(vahvistus = vahvistus).copy(arviointi = hyväksytty),
              suoritus(oppiaine("TE")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(8)),
              suoritus(oppiaine("KS")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
              suoritus(oppiaine("LI")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
              suoritus(oppiaine("LI").copy(pakollinen = false, laajuus = vuosiviikkotuntia(0.5))).copy(vahvistus = vahvistus).copy(arviointi = hyväksytty),
              suoritus(kieli("B2", "DE").copy(pakollinen = false, laajuus = vuosiviikkotuntia(4))).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9))
            ))
        )),
      opiskeluoikeudenTila = Some(OpiskeluoikeudenTila(
        List(
          Opiskeluoikeusjakso(date(2007, 8, 15), Some(date(2016, 6, 3)), opiskeluoikeusAktiivinen, None),
          Opiskeluoikeusjakso(date(2016, 6, 4), None, opiskeluoikeusPäättynyt, None)
        )
      )),
      läsnäolotiedot = None
    ))
  )

  val examples = List(
    Example("peruskoulutus - uusi", "Uusi oppija lisätään suorittamaan peruskoulua", uusi),
    Example("peruskoulutus - päättötodistus", "Oppija on saanut peruskoulun päättötodistuksen", päättötodistus)
  )
}
