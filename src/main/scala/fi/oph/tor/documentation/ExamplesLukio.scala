package fi.oph.tor.documentation

import java.time.LocalDate.{of => date}

import fi.oph.tor.documentation.ExampleData._
import fi.oph.tor.documentation.LukioExampleData._
import fi.oph.tor.documentation.YleissivistavakoulutusExampleData._
import fi.oph.tor.oppija.MockOppijat
import fi.oph.tor.schema._
import fi.oph.tor.localization.LocalizedStringImplicits._

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
          koulutusmoduuli = ylioppilastutkinto,
          paikallinenId = None,
          suorituskieli = suomenKieli,
          tila = tilaKesken,
          toimipiste = jyväskylänNormaalikoulu,
          osasuoritukset = None
        )
      ),
      opiskeluoikeudenTila = Some(YleissivistäväOpiskeluoikeudenTila(
        List(
          YleissivistäväOpiskeluoikeusjakso(date(2012, 9, 1), Some(date(2016, 1, 9)), opiskeluoikeusAktiivinen),
          YleissivistäväOpiskeluoikeusjakso(date(2016, 1, 10), None, opiskeluoikeusPäättynyt)
        )
      )),
      läsnäolotiedot = None
    ))
  )

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
          koulutusmoduuli = ylioppilastutkinto,
          paikallinenId = None,
          suorituskieli = suomenKieli,
          tila = tilaValmis,
          vahvistus = vahvistus,
          toimipiste = jyväskylänNormaalikoulu,
          osasuoritukset = Some(List(
            suoritus(äidinkieli("AI1")).copy(arviointi = arviointi(9)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("ÄI1")).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI2")).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI3")).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI4")).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI5")).copy(arviointi = arviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI6")).copy(arviointi = arviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI8")).copy(arviointi = arviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI9")).copy(arviointi = arviointi(9))
            ))),
            suoritus(kieli("A1", "EN")).copy(arviointi = arviointi(9)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("ENA1")).copy(arviointi = arviointi(10)),
              kurssisuoritus(valtakunnallinenKurssi("ENA2")).copy(arviointi = arviointi(10)),
              kurssisuoritus(valtakunnallinenKurssi("ENA3")).copy(arviointi = arviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("ENA4")).copy(arviointi = arviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("ENA5")).copy(arviointi = arviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("ENA6")).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ENA7")).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ENA8")).copy(arviointi = arviointi(9)),
              kurssisuoritus(paikallinenKurssi("ENA 10", "Abituki")).copy(arviointi = arviointi("S")) // 0.5
            ))),
            suoritus(kieli("B1", "SV")).copy(arviointi = arviointi(7)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("RUB11")).copy(arviointi = arviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("RUB12")).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("RUB13")).copy(arviointi = arviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("RUB14")).copy(arviointi = arviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("RUB15")).copy(arviointi = arviointi(6))
            ))),
            suoritus(kieli("B3", "LA")).copy(arviointi = arviointi(9)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("LAB31")).copy(arviointi = arviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("LAB32")).copy(arviointi = arviointi(8))
            ))),
            suoritus(matematiikka("MAA")).copy(arviointi = arviointi(9)).copy(osasuoritukset = Some(List(
              kurssisuoritus(paikallinenKurssi("MAA1", "Funktiot ja yhtälöt, pa, vuositaso 1")).copy(arviointi = arviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("MAA2")).copy(arviointi = arviointi(10)),
              kurssisuoritus(valtakunnallinenKurssi("MAA3")).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("MAA4")).copy(arviointi = arviointi(10)),
              kurssisuoritus(valtakunnallinenKurssi("MAA5")).copy(arviointi = arviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("MAA6")).copy(arviointi = arviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("MAA7")).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("MAA8")).copy(arviointi = arviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("MAA9")).copy(arviointi = arviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("MAA10")).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("MAA11")).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("MAA12")).copy(arviointi = arviointi(10)),
              kurssisuoritus(valtakunnallinenKurssi("MAA13")).copy(arviointi = arviointi(8)),
              kurssisuoritus(paikallinenKurssi("MAA14", "Kertauskurssi, ksy, vuositaso 3")).copy(arviointi = arviointi(9)),
              kurssisuoritus(paikallinenKurssi("MAA16", "Analyyttisten menetelmien lisäkurssi, ksy, vuositaso 2")).copy(arviointi = arviointi(9))
            ))),
            suoritus(oppiaine("BI")).copy(arviointi = arviointi(9)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("BI1")).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("BI2")).copy(arviointi = arviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("BI3")).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("BI4")).copy(arviointi = arviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("BI5")).copy(arviointi = arviointi(10)),
              kurssisuoritus(paikallinenKurssi("BI6", "Cell Biology (½ kurssia), so, vuositaso 3").copy(laajuus = laajuus(0.5f))).copy(arviointi = arviointi("S")),
              kurssisuoritus(paikallinenKurssi("BI7", "Biologia nova - ympäristö tutuksi (1-3 kurssia), so, vuositasot 1-2")).copy(arviointi = arviointi("S")),
              kurssisuoritus(paikallinenKurssi("BI8", "Biologian kertauskurssi (½ kurssia), so, vuositaso 3")).copy(arviointi = arviointi("S"))
            ))),
            suoritus(oppiaine("GE")).copy(arviointi = arviointi(8)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("GE1")).copy(arviointi = arviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("GE2")).copy(arviointi = arviointi(7))
            ))),
            suoritus(oppiaine("FY")).copy(arviointi = arviointi(8)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("FY1")).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("FY2")).copy(arviointi = arviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("FY3")).copy(arviointi = arviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("FY4")).copy(arviointi = arviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("FY5")).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("FY6")).copy(arviointi = arviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("FY7")).copy(arviointi = arviointi(8)),
              kurssisuoritus(paikallinenKurssi("FY8", "Aine ja säteily, sy, vuositaso 3")).copy(arviointi = arviointi(7)),
              kurssisuoritus(paikallinenKurssi("FY9", "Kokeellinen fysiikka, so, vuositaso 2")).copy(arviointi = arviointi(7)),
              kurssisuoritus(paikallinenKurssi("FY10", "Lukion fysiikan kokonaiskuva, so, vuositaso 3")).copy(arviointi = arviointi("S")),
              kurssisuoritus(paikallinenKurssi("FY11", "Fysiikka 11")).copy(arviointi = arviointi("S")),
              kurssisuoritus(paikallinenKurssi("FY12", "Fysiikka 12")).copy(arviointi = arviointi("S")),
              kurssisuoritus(paikallinenKurssi("FY13", "Fysiikka 13")).copy(arviointi = arviointi("S"))
            ))),
            suoritus(oppiaine("KE")).copy(arviointi = arviointi(8)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("KE1")).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("KE2")).copy(arviointi = arviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("KE3")).copy(arviointi = arviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("KE4")).copy(arviointi = arviointi(5)),
              kurssisuoritus(valtakunnallinenKurssi("KE5")).copy(arviointi = arviointi(7)),
              kurssisuoritus(paikallinenKurssi("KE6", "Kokeellinen kemia, so, vuositasot 2-3")).copy(arviointi = arviointi(5)),
              kurssisuoritus(paikallinenKurssi("KE7", "Lukion kemian kokonaiskuva, so, vuositaso 3")).copy(arviointi = arviointi("S")),
              kurssisuoritus(paikallinenKurssi("KE8", "Kemia 8")).copy(arviointi = arviointi("S"))
            ))),
            suoritus(uskonto("KT1")).copy(arviointi = arviointi(8)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("UE1")).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("UE2")).copy(arviointi = arviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("UE3")).copy(arviointi = arviointi(8))
            ))),
            suoritus(oppiaine("FI")).copy(arviointi = arviointi(8)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("FI1")).copy(arviointi = arviointi(8))
            ))),
            suoritus(oppiaine("PS")).copy(arviointi = arviointi(9)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("PS1")).copy(arviointi = arviointi(9))
            ))),
            suoritus(oppiaine("HI")).copy(arviointi = arviointi(7)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("HI1")).copy(arviointi = arviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("HI2")).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("HI3")).copy(arviointi = arviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("HI4")).copy(arviointi = arviointi(6))
            ))),
            suoritus(oppiaine("YH")).copy(arviointi = arviointi(8)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("YH1")).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("YH2")).copy(arviointi = arviointi(8))
            ))),
            suoritus(oppiaine("LI")).copy(arviointi = arviointi(9)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("LI1")).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("LI2")).copy(arviointi = arviointi(9)),
              kurssisuoritus(paikallinenKurssi("LI12", "Vanhat tanssit, kso")).copy(arviointi = arviointi("S"))
            ))),
            suoritus(oppiaine("MU")).copy(arviointi = arviointi(8)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("MU1")).copy(arviointi = arviointi(8))
            ))),
            suoritus(oppiaine("KU")).copy(arviointi = arviointi(9)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("KU1")).copy(arviointi = arviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("KU2")).copy(arviointi = arviointi(9))
            ))),
            suoritus(oppiaine("TE")).copy(arviointi = arviointi(9)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("TE1")).copy(arviointi = arviointi(8))
            )))
            // TODO, opinto-ohjaus, kansalaisen turvakurssit
          ))
        )
      ),
      opiskeluoikeudenTila = Some(YleissivistäväOpiskeluoikeudenTila(
        List(
          YleissivistäväOpiskeluoikeusjakso(alku = date(2012, 9, 1), loppu = Some(date(2016, 1, 9)), tila = opiskeluoikeusAktiivinen),
          YleissivistäväOpiskeluoikeusjakso(alku = date(2016, 1, 10), loppu = None, tila = opiskeluoikeusPäättynyt)
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

  val ylioppilastutkinto: Ylioppilastutkinto = Ylioppilastutkinto(perusteenDiaarinumero = Some("60/011/2015"))

  val vahvistus: Some[Vahvistus] = Some(Vahvistus(päivä = date(2016, 6, 4), jyväskylä, myöntäjäOrganisaatio = jyväskylänNormaalikoulu, myöntäjäHenkilöt = List(OrganisaatioHenkilö("Reijo Reksi", "rehtori", jyväskylänNormaalikoulu))))

  def suoritus(aine: LukionOppiaine): LukionOppiaineenSuoritus = LukionOppiaineenSuoritus(
    koulutusmoduuli = aine,
    paikallinenId = None,
    suorituskieli = None,
    arviointi = None,
    tila = tilaValmis,
    osasuoritukset = None
  )

  def kurssisuoritus(kurssi: LukionKurssi) = LukionKurssinSuoritus(
    koulutusmoduuli = kurssi,
    suorituskieli = None,
    paikallinenId = None,
    arviointi = None,
    tila = tilaValmis
  )

  def valtakunnallinenKurssi(kurssi: String) = ValtakunnallinenLukionKurssi(Koodistokoodiviite(koodistoUri = "lukionkurssit", koodiarvo = kurssi), laajuus(1.0f))
  def paikallinenKurssi(koodi: String, nimi: String) = PaikallinenLukionKurssi(Paikallinenkoodi(koodiarvo = koodi, nimi = nimi, koodistoUri = "paikallinen"), laajuus(1.0f))

  def matematiikka(matematiikka: String) = LukionMatematiikka(oppimäärä = Koodistokoodiviite(koodiarvo = matematiikka, koodistoUri = "oppiainematematiikka"))

  def laajuus(laajuus: Float): Some[Laajuus] = Some(Laajuus(laajuus, Koodistokoodiviite(koodistoUri = "opintojenlaajuusyksikko", koodiarvo = "4")))
}