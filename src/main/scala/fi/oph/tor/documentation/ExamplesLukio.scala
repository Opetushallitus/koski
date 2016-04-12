package fi.oph.tor.documentation

import java.time.LocalDate.{of => date}

import fi.oph.tor.documentation.ExampleData._
import fi.oph.tor.documentation.LukioExampleData._
import fi.oph.tor.documentation.YleissivistavakoulutusExampleData._
import fi.oph.tor.oppija.MockOppijat
import fi.oph.tor.schema._

object ExamplesLukio {
  val uusi = TorOppija(
    exampleHenkilö,
    List(LukionOpiskeluoikeus(
      id = None,
      versionumero = None,
      lähdejärjestelmänId = None,
      alkamispäivä = Some(date(2016, 9, 1)),
      arvioituPäättymispäivä = Some(date(2020, 5, 1)),
      päättymispäivä = None,
      oppilaitos = jyväskylänNormaalikoulu, None,
      suoritukset = List(
        LukionOppimääränSuoritus(
          paikallinenId = None,
          suorituskieli = suomenKieli,
          tila = tilaKesken,
          toimipiste = jyväskylänNormaalikoulu,
          osasuoritukset = None
        )
      ),
      opiskeluoikeudenTila = Some(OpiskeluoikeudenTila(
        List(
          Opiskeluoikeusjakso(date(2012, 9, 1), Some(date(2016, 1, 9)), opiskeluoikeusAktiivinen, None),
          Opiskeluoikeusjakso(date(2016, 1, 10), None, opiskeluoikeusPäättynyt, None)
        )
      )),
      läsnäolotiedot = None
    ))
  )

  private val vahvistus: Some[Vahvistus] = Some(Vahvistus(date(2016, 6, 4)))

  val päättötodistus = TorOppija(
    exampleHenkilö,
    List(LukionOpiskeluoikeus(
      id = None,
      versionumero = None,
      lähdejärjestelmänId = None,
      alkamispäivä = Some(date(2016, 9, 1)),
      arvioituPäättymispäivä = Some(date(2020, 5, 1)),
      päättymispäivä = None,
      oppilaitos = jyväskylänNormaalikoulu, None,
      suoritukset = List(
        LukionOppimääränSuoritus(
          paikallinenId = None,
          suorituskieli = suomenKieli,
          tila = tilaValmis,
          vahvistus = vahvistus,
          toimipiste = jyväskylänNormaalikoulu,
          osasuoritukset = Some(List(
            suoritus(äidinkieli("AI1")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("ÄI1")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI2")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI3")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI4")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI5")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI6")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI8")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI9")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9))
            ))),
            suoritus(kieli("A1", "EN")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
            suoritus(kieli("B1", "SV")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(7)),
            suoritus(kieli("B3", "LA")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
            suoritus(matematiikka("MAA")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
            suoritus(oppiaine("BI")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
            suoritus(oppiaine("GE")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(8)),
            suoritus(oppiaine("FY")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(8)),
            suoritus(oppiaine("KE")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(8)),
            suoritus(uskonto("KT1")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(8)),
            suoritus(oppiaine("FI")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(8)),
            suoritus(oppiaine("PS")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
            suoritus(oppiaine("HI")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(7)),
            suoritus(oppiaine("YH")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(8)),
            suoritus(oppiaine("LI")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
            suoritus(oppiaine("MU")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(8)),
            suoritus(oppiaine("KU")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
            suoritus(oppiaine("TE")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
            suoritus(oppiaine("TE")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9))
            // TODO, opinto-ohjaus, kansalaisen turvakurssit
          ))
        )
      ),
      opiskeluoikeudenTila = Some(OpiskeluoikeudenTila(
        List(
          Opiskeluoikeusjakso(alku = date(2012, 9, 1), loppu = Some(date(2016, 1, 9)), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = None),
          Opiskeluoikeusjakso(alku = date(2016, 1, 10), loppu = None, tila = opiskeluoikeusPäättynyt, opintojenRahoitus = None)
        )
      )),
      läsnäolotiedot = None
    ))
  )

  val examples = List(
    Example("lukio - uusi", "Uusi oppija lisätään suorittamaan lukiota", uusi),
    Example("lukio - päättötodistus", "Oppija on saanut päättötodistuksen", päättötodistus)
  )
}

object LukioExampleData {
  val exampleHenkilö = MockOppijat.lukiolainen.vainHenkilötiedot

  def suoritus(aine: LukionOppiaine): LukionOppiaineenSuoritus = LukionOppiaineenSuoritus(
    koulutusmoduuli = aine,
    paikallinenId = None,
    suorituskieli = None,
    arviointi = None,
    tila = tilaValmis,
    vahvistus = None,
    osasuoritukset = None
  )

  def kurssisuoritus(kurssi: LukionKurssi) = LukionKurssinSuoritus(
    koulutusmoduuli = kurssi,
    suorituskieli = None,
    paikallinenId = None,
    vahvistus = None,
    arviointi = None,
    tila = tilaValmis
  )

  def valtakunnallinenKurssi(kurssi: String) = ValtakunnallinenLukionKurssi(Koodistokoodiviite(koodistoUri = "lukionkurssit", koodiarvo = kurssi))
  def paikallinenKurssi(koodi: String, nimi: String) = PaikallinenLukionKurssi(Paikallinenkoodi(koodiarvo = koodi, nimi = nimi, koodistoUri = "paikallinen"))

  def matematiikka(matematiikka: String) = LukionMatematiikka(oppimäärä = Koodistokoodiviite(koodiarvo = matematiikka, koodistoUri = "oppiainematematiikka"))
}