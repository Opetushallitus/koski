package fi.oph.koski.documentation

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.LukioExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.schema._

object ExamplesLukio {
  def oppija(opiskeluoikeus: LukionOpiskeluoikeus) = Oppija(exampleHenkilö, List(opiskeluoikeus))

  val päättötodistus = LukionOpiskeluoikeus(
      alkamispäivä = Some(date(2012, 9, 1)),
      päättymispäivä = Some(date(2016, 6, 1)),
      tila = LukionOpiskeluoikeudenTila(
        List(
          LukionOpiskeluoikeusjakso(alku = date(2012, 9, 1), tila = opiskeluoikeusAktiivinen),
          LukionOpiskeluoikeusjakso(alku = date(2016, 6, 1), tila = opiskeluoikeusPäättynyt)
        )
      ),
      oppilaitos = jyväskylänNormaalikoulu,
      tavoite = tavoiteKokoOppimäärä,
      suoritukset = List(
        LukionOppimääränSuoritus(
          koulutusmoduuli = lukionOppimäärä,
          oppimäärä = nuortenOpetussuunnitelma,
          suorituskieli = suomenKieli,
          tila = tilaValmis,
          vahvistus = vahvistus(),
          toimipiste = jyväskylänNormaalikoulu,
          osasuoritukset = Some(List(
            suoritus(lukionÄidinkieli("AI1")).copy(arviointi = arviointi(9)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("ÄI1")).copy(arviointi = kurssinArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI2")).copy(arviointi = kurssinArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI3")).copy(arviointi = kurssinArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI4")).copy(arviointi = kurssinArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI5")).copy(arviointi = kurssinArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI6")).copy(arviointi = kurssinArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI8")).copy(arviointi = kurssinArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI9")).copy(arviointi = kurssinArviointi(9))
            ))),
            suoritus(lukionKieli("A1", "EN")).copy(arviointi = arviointi(9)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("ENA1")).copy(arviointi = kurssinArviointi(10)),
              kurssisuoritus(valtakunnallinenKurssi("ENA2")).copy(arviointi = kurssinArviointi(10)),
              kurssisuoritus(valtakunnallinenKurssi("ENA3")).copy(arviointi = kurssinArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("ENA4")).copy(arviointi = kurssinArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("ENA5")).copy(arviointi = kurssinArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("ENA6")).copy(arviointi = kurssinArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ENA7")).copy(arviointi = kurssinArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ENA8")).copy(arviointi = kurssinArviointi(9)),
              kurssisuoritus(paikallinenKurssi("ENA 10", "Abituki", "Aihepiirit liittyvät pakollisten ja valtakunnallisten syventävien kurssien aihekokonaisuuksiin niitä syventäen ja laajentaen. Kurssilla vankennetaan ylioppilaskokeessa tarvittavia tietoja ja taitoja. Pakollisilla ja valtakunnallisilla syventävillä kursseilla hankitun kielioppirakenteiden ja sanaston hallintaa vahvistetaan ja syvennetään. Arvioinnissa (suoritettu/hylätty) otetaan huomioon kaikki kielitaidon osa-alueet ja se perustuu jatkuvaan näyttöön. Kurssin päättyessä opiskelija on saanut lisävalmiuksia osallistua ylioppilaskokeeseen."))
                .copy(arviointi = kurssinArviointi("S")) // 0.5
            ))),
            suoritus(lukionKieli("B1", "SV")).copy(arviointi = arviointi(7)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("RUB11")).copy(arviointi = kurssinArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("RUB12")).copy(arviointi = kurssinArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("RUB13")).copy(arviointi = kurssinArviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("RUB14")).copy(arviointi = kurssinArviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("RUB15")).copy(arviointi = kurssinArviointi(6))
            ))),
            suoritus(lukionKieli("B3", "LA")).copy(arviointi = arviointi(9)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("LAB31")).copy(arviointi = kurssinArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("LAB32")).copy(arviointi = kurssinArviointi(8))
            ))),
            suoritus(matematiikka("MAA")).copy(arviointi = arviointi(9)).copy(osasuoritukset = Some(List(
              kurssisuoritus(paikallinenKurssi("MAA1", "Funktiot ja yhtälöt, pa, vuositaso 1",
                "Vahvistetaan yhtälön ratkaisemisen ja prosenttilaskennan taitoja. Syvennetään verrannollisuuden, neliöjuuren ja potenssin käsitteiden ymmärtämistä. Harjaannutaan käyttämään neliöjuuren ja potenssin laskusääntöjä. Syvennetään funktiokäsitteen ymmärtämistä tutkimalla potenssi- ja eksponenttifunktioita. Opetellaan ratkaisemaan potenssiyhtälöitä."))
                .copy(arviointi = kurssinArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("MAA2")).copy(arviointi = kurssinArviointi(10)),
              kurssisuoritus(valtakunnallinenKurssi("MAA3")).copy(arviointi = kurssinArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("MAA4")).copy(arviointi = kurssinArviointi(10)),
              kurssisuoritus(valtakunnallinenKurssi("MAA5")).copy(arviointi = kurssinArviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("MAA6")).copy(arviointi = kurssinArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("MAA7")).copy(arviointi = kurssinArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("MAA8")).copy(arviointi = kurssinArviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("MAA9")).copy(arviointi = kurssinArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("MAA10")).copy(arviointi = kurssinArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("MAA11")).copy(arviointi = kurssinArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("MAA12")).copy(arviointi = kurssinArviointi(10)),
              kurssisuoritus(valtakunnallinenKurssi("MAA13")).copy(arviointi = kurssinArviointi(8)),
              kurssisuoritus(paikallinenKurssi("MAA14", "Kertauskurssi, ksy, vuositaso 3", "Harjoitellaan käyttämään opittuja tietoja ja taitoja monipuolisissa ongelmanratkaisutilanteissa."))
                .copy(arviointi = kurssinArviointi(9)),
              kurssisuoritus(paikallinenKurssi("MAA16", "Analyyttisten menetelmien lisäkurssi, ksy, vuositaso 2", "Kurssilla syvennetään kurssien MAA4, MAA5 ja MAA7 sisältöjä."))
                .copy(arviointi = kurssinArviointi(9))
            ))),
            suoritus(lukionOppiaine("BI")).copy(arviointi = arviointi(9)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("BI1")).copy(arviointi = kurssinArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("BI2")).copy(arviointi = kurssinArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("BI3")).copy(arviointi = kurssinArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("BI4")).copy(arviointi = kurssinArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("BI5")).copy(arviointi = kurssinArviointi(10)),
              kurssisuoritus(paikallinenKurssi("BI6", "Cell Biology (½ kurssia), so, vuositaso 3", "Biology in English, including the structure of procaryotic and eucaryotic cells, cellular control, growth regulation, application of genetics and problemsolving. The course will familiarise students with English terminology, analytical techniques and laboratory work. Students should aim to improve their spoken English skills and vocabulary as well as learn about practical cell biology.")
                .copy(laajuus = laajuus(0.5f))).copy(arviointi = kurssinArviointi("S")),
              kurssisuoritus(paikallinenKurssi("BI7", "Biologia nova - ympäristö tutuksi (1-3 kurssia), so, vuositasot 1-2", "Kurssiin ei sisälly tuntiopetusta, ainoastaan suunnittelu-, opastus- ja kontrollituokioita. Opiskelija laatii yksilöllisen kurssikokonaisuuden, jonka päätarkoitus on perehtyä valitun (pirkanmaalaisen) ekosysteemin lajistoon. Opiskelija voi erikoistua esim. kasvilajistoon, sieniin, perhosiin, kaloihin, nisäkkäisiin jne. Suunnitelman laajuudesta ja laadusta riippuen opiskelija voi saada 1-3 kurssisuoritusmerkintää. Opiskeluaika voi olla enintään kaksi vuotta."))
                .copy(arviointi = kurssinArviointi("S")),
              kurssisuoritus(paikallinenKurssi("BI8", "Biologian kertauskurssi (½ kurssia), so, vuositaso 3", "Kurssilla kerrataan biologian keskeisiä asioita ainereaaliin valmistauduttaessa."))
                .copy(arviointi = kurssinArviointi("S"))
            ))),
            suoritus(lukionOppiaine("GE")).copy(arviointi = arviointi(8)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("GE1")).copy(arviointi = kurssinArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("GE2")).copy(arviointi = kurssinArviointi(7))
            ))),
            suoritus(lukionOppiaine("FY")).copy(arviointi = arviointi(8)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("FY1")).copy(arviointi = kurssinArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("FY2")).copy(arviointi = kurssinArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("FY3")).copy(arviointi = kurssinArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("FY4")).copy(arviointi = kurssinArviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("FY5")).copy(arviointi = kurssinArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("FY6")).copy(arviointi = kurssinArviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("FY7")).copy(arviointi = kurssinArviointi(8)),
              kurssisuoritus(paikallinenKurssi("FY8", "Aine ja säteily, sy, vuositaso 3", "Kurssin sisältö perustuu lähinnä 1900-luvun aikana selvitettyyn tietoon aineesta ja energiasta. Lisäksi käsitellään atomi-, ydin- ja alkeishiukkasfysiikkaa."))
                .copy(arviointi = kurssinArviointi(7)),
              kurssisuoritus(paikallinenKurssi("FY9", "Kokeellinen fysiikka, so, vuositaso 2", "Kurssi syventää ja täydentää muilla kursseilla esiin tulleita tai tulevia asioita. Kurssilla tutkitaan kokeellisesti fysiikan ilmiöitä eri osa-alueilta ja opetellaan raportoimaan tehty koe ja kokeen tulokset."))
                .copy(arviointi = kurssinArviointi(7)),
              kurssisuoritus(paikallinenKurssi("FY10", "Lukion fysiikan kokonaiskuva, so, vuositaso 3", "Kurssin tavoitteena on luoda kokonaiskuva fysiikasta. Kurssilla syvennetään fysiikan osaamista laskennallisella tasolla ja harjoitellaan reaalikokeeseen vastaamista."))
                .copy(arviointi = kurssinArviointi("S")),
              kurssisuoritus(paikallinenKurssi("FY11", "Fysiikka 11", "Fysiikka 11")).copy(arviointi = kurssinArviointi("S")),
              kurssisuoritus(paikallinenKurssi("FY12", "Fysiikka 12", "Fysiikka 12")).copy(arviointi = kurssinArviointi("S")),
              kurssisuoritus(paikallinenKurssi("FY13", "Fysiikka 13", "Fysiikka 13")).copy(arviointi = kurssinArviointi("S"))
            ))),
            suoritus(lukionOppiaine("KE")).copy(arviointi = arviointi(8)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("KE1")).copy(arviointi = kurssinArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("KE2")).copy(arviointi = kurssinArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("KE3")).copy(arviointi = kurssinArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("KE4")).copy(arviointi = kurssinArviointi(5)),
              kurssisuoritus(valtakunnallinenKurssi("KE5")).copy(arviointi = kurssinArviointi(7)),
              kurssisuoritus(paikallinenKurssi("KE6", "Kokeellinen kemia, so, vuositasot 2-3", "Tehdään erilaisia kvantitatiivisia ja kvalitatiivisia määrityksiä, synteesejä ja analyysejä sekä laaditaan työselostuksia. Mahdollisuuksien mukaan tehdään myös vierailuja alan yrityksiin ja oppilaitoksiin."))
                .copy(arviointi = kurssinArviointi(5)),
              kurssisuoritus(paikallinenKurssi("KE7", "Lukion kemian kokonaiskuva, so, vuositaso 3", "Kurssin tavoitteena on kerrata lukion oppimäärä ja antaa kokonaiskuva lukion kemiasta. Harjoitellaan reaalikokeeseen vastaamista."))
                .copy(arviointi = kurssinArviointi("S")),
              kurssisuoritus(paikallinenKurssi("KE8", "Kemia 8", "Kemia 8"))
                .copy(arviointi = kurssinArviointi("S"))
            ))),
            suoritus(lukionOppiaine("KT")).copy(arviointi = arviointi(8)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("UE1")).copy(arviointi = kurssinArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("UE2")).copy(arviointi = kurssinArviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("UE3")).copy(arviointi = kurssinArviointi(8))
            ))),
            suoritus(lukionOppiaine("FI")).copy(arviointi = arviointi(8)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("FI1")).copy(arviointi = kurssinArviointi(8))
            ))),
            suoritus(lukionOppiaine("PS")).copy(arviointi = arviointi(9)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("PS1")).copy(arviointi = kurssinArviointi(9))
            ))),
            suoritus(lukionOppiaine("HI")).copy(arviointi = arviointi(7)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("HI1")).copy(arviointi = kurssinArviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("HI2")).copy(arviointi = kurssinArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("HI3")).copy(arviointi = kurssinArviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("HI4")).copy(arviointi = kurssinArviointi(6))
            ))),
            suoritus(lukionOppiaine("YH")).copy(arviointi = arviointi(8)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("YH1")).copy(arviointi = kurssinArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("YH2")).copy(arviointi = kurssinArviointi(8))
            ))),
            suoritus(lukionOppiaine("LI")).copy(arviointi = arviointi(9)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("LI1")).copy(arviointi = kurssinArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("LI2")).copy(arviointi = kurssinArviointi(9)),
              kurssisuoritus(paikallinenKurssi("LI12", "Vanhat tanssit, kso", "Kurssin tavoitteena on kehittää sosiaalista vuorovaikutusta tanssin avulla. Tähän liittyy kiinteästi myös tapakasvatus. Kurssilla harjoitellaan ensisijaisesti ns. ”Vanhojen päivän” ohjelmistoa – vanhoja tansseja ja salonkitansseja, mutta myös tavallisia paritansseja. Kurssin käyminen ei velvoita osallistumaan juhlapäivän esityksiin. Kurssi arvioidaan suoritusmerkinnällä."))
                .copy(arviointi = kurssinArviointi("S"))
            ))),
            suoritus(lukionOppiaine("MU")).copy(arviointi = arviointi(8)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("MU1")).copy(arviointi = kurssinArviointi(8))
            ))),
            suoritus(lukionOppiaine("KU")).copy(arviointi = arviointi(9)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("KU1")).copy(arviointi = kurssinArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("KU2")).copy(arviointi = kurssinArviointi(9))
            ))),
            suoritus(lukionOppiaine("TE")).copy(arviointi = arviointi(9)).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("TE1")).copy(arviointi = kurssinArviointi(8))
            )))
            // TODO, opinto-ohjaus, kansalaisen turvakurssit
          ))
        )
      ),
      läsnäolotiedot = None
    )


  val aineopiskelija =
    LukionOpiskeluoikeus(
      id = None,
      versionumero = None,
      lähdejärjestelmänId = None,
      alkamispäivä = Some(date(2015, 9, 1)),
      päättymispäivä = Some(date(2016, 1, 10)),
      oppilaitos = jyväskylänNormaalikoulu,
      tavoite = tavoiteOppiaineenOppimäärä,
      suoritukset = List(
        LukionOppiaineenOppimääränSuoritus(
          koulutusmoduuli = lukionOppiaine("HI"),
          suorituskieli = suomenKieli,
          tila = tilaValmis,
          vahvistus = vahvistus(),
          toimipiste = jyväskylänNormaalikoulu,
          arviointi = arviointi(9),
          osasuoritukset = Some(List(
            kurssisuoritus(valtakunnallinenKurssi("HI1")).copy(arviointi = kurssinArviointi(7)),
            kurssisuoritus(valtakunnallinenKurssi("HI2")).copy(arviointi = kurssinArviointi(8)),
            kurssisuoritus(valtakunnallinenKurssi("HI3")).copy(arviointi = kurssinArviointi(7)),
            kurssisuoritus(valtakunnallinenKurssi("HI4")).copy(arviointi = kurssinArviointi(6))
          ))
        )
      ),
      tila = LukionOpiskeluoikeudenTila(
        List(
          LukionOpiskeluoikeusjakso(alku = date(2015, 9, 1), tila = opiskeluoikeusAktiivinen),
          LukionOpiskeluoikeusjakso(alku = date(2016, 1, 10), tila = opiskeluoikeusPäättynyt)
        )
      ),
      läsnäolotiedot = None
    )


  val examples = List(
    Example("lukio - uusi", "Uusi oppija lisätään suorittamaan lukiota", oppija(lukionOpiskeluoikeus())),
    Example("lukio - päättötodistus", "Oppija on saanut päättötodistuksen", oppija(päättötodistus)),
    Example("lukio - lukion oppiaineen oppimäärä - päättötodistus", "Opiskelija on suorittanut lukion historian oppimäärän", oppija(aineopiskelija))
  )
}

object LukioExampleData {
  def arviointi(arvosana: String): Some[List[LukionOppiaineenArviointi]] = {
    Some(List(LukionOppiaineenArviointi(arvosana)))
  }

  val hyväksytty = Some(List(LukionOppiaineenArviointi("S")))

  val exampleHenkilö = MockOppijat.lukiolainen.vainHenkilötiedot

  val ylioppilastutkinto: Ylioppilastutkinto = Ylioppilastutkinto(perusteenDiaarinumero = Some("60/011/2015"))

  val lukionOppimäärä: LukionOppimäärä = LukionOppimäärä(perusteenDiaarinumero = Some("60/011/2015"))

  val tavoiteKokoOppimäärä = Koodistokoodiviite("lukionoppimaara", "suorituksentyyppi")
  val tavoiteOppiaineenOppimäärä = Koodistokoodiviite("lukionoppiaineenoppimaara", "suorituksentyyppi")

  val opiskeluoikeusAktiivinen = Koodistokoodiviite("lasna", Some("Läsnä"), "koskiopiskeluoikeudentila", Some(1))
  val opiskeluoikeusPäättynyt = Koodistokoodiviite("valmistunut", Some("Valmistunut"), "koskiopiskeluoikeudentila", Some(1))

  val aikuistenOpetussuunnitelma = Koodistokoodiviite("aikuistenops", Some("Aikuisten ops"), "lukionoppimaara", Some(1))
  val nuortenOpetussuunnitelma = Koodistokoodiviite("nuortenops", Some("Nuorten ops"), "lukionoppimaara", Some(1))

  def suoritus(aine: LukionOppiaine): LukionOppiaineenSuoritus = LukionOppiaineenSuoritus(
    koulutusmoduuli = aine,
    suorituskieli = None,
    arviointi = None,
    tila = tilaValmis,
    osasuoritukset = None
  )

  def kurssisuoritus(kurssi: LukionKurssi) = LukionKurssinSuoritus(
    koulutusmoduuli = kurssi,
    suorituskieli = None,
    arviointi = None,
    tila = tilaValmis
  )

  def valtakunnallinenKurssi(kurssi: String): ValtakunnallinenLukionKurssi = ValtakunnallinenLukionKurssi(Koodistokoodiviite(koodistoUri = "lukionkurssit", koodiarvo = kurssi), laajuus(1.0f))
  def paikallinenKurssi(koodi: String, nimi: String, kuvaus: String) = PaikallinenLukionKurssi(PaikallinenKoodi(koodiarvo = koodi, nimi = nimi), laajuus(1.0f), kuvaus)

  def matematiikka(matematiikka: String) = LukionMatematiikka(oppimäärä = Koodistokoodiviite(koodiarvo = matematiikka, koodistoUri = "oppiainematematiikka"))

  def laajuus(laajuus: Float, yksikkö: String = "4"): Some[LaajuusKursseissa] = Some(LaajuusKursseissa(laajuus, Koodistokoodiviite(koodistoUri = "opintojenlaajuusyksikko", koodiarvo = yksikkö)))

  def lukionOppiaine(aine: String, laajuus: Option[LaajuusKursseissa] = None) = MuuOppiaine(tunniste = Koodistokoodiviite(koodistoUri = "koskioppiaineetyleissivistava", koodiarvo = aine), laajuus = laajuus)
  def lukionÄidinkieli(kieli: String) = AidinkieliJaKirjallisuus(kieli = Koodistokoodiviite(koodiarvo = kieli, koodistoUri = "oppiaineaidinkielijakirjallisuus"))
  def lukionKieli(oppiaine: String, kieli: String) = VierasTaiToinenKotimainenKieli(
    tunniste = Koodistokoodiviite(koodiarvo = oppiaine, koodistoUri = "koskioppiaineetyleissivistava"),
    kieli = Koodistokoodiviite(koodiarvo = kieli, koodistoUri = "kielivalikoima"))

  def kurssinArviointi(arvosana: String, päivä: LocalDate = date(2016, 6, 4)): Some[List[LukionKurssinArviointi]] = {
    Some(List(new LukionKurssinArviointi(arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkoyleissivistava"), päivä)))
  }

  def lukionOpiskeluoikeus(oppilaitos: Oppilaitos = jyväskylänNormaalikoulu) = LukionOpiskeluoikeus(
    id = None,
    versionumero = None,
    lähdejärjestelmänId = None,
    alkamispäivä = Some(date(2012, 9, 1)),
    päättymispäivä = None,
    oppilaitos = oppilaitos,
    tavoite = tavoiteKokoOppimäärä,
    suoritukset = List(
      LukionOppimääränSuoritus(
        koulutusmoduuli = lukionOppimäärä,
        oppimäärä = nuortenOpetussuunnitelma,
        suorituskieli = suomenKieli,
        tila = tilaKesken,
        toimipiste = oppilaitos,
        osasuoritukset = None
      )
    ),
    tila = LukionOpiskeluoikeudenTila(
      List(
        LukionOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusAktiivinen),
        LukionOpiskeluoikeusjakso(date(2016, 1, 10), opiskeluoikeusPäättynyt)
      )
    ),
    läsnäolotiedot = None
  )
}