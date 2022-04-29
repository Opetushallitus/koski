package fi.oph.koski.documentation

import java.time.LocalDate
import java.time.LocalDate.{of => date}
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.ExamplesLukio.{aikuistenOpsinPerusteet2004, aikuistenOpsinPerusteet2015}
import fi.oph.koski.documentation.LukioExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

object ExamplesLukio {

  def oppija(opiskeluoikeus: LukionOpiskeluoikeus) = Oppija(exampleHenkilö, List(opiskeluoikeus))

  val aikuistenOpsinPerusteet2015 = "70/011/2015"
  val aikuistenOpsinPerusteet2004 = "4/011/2004"

  lazy val erityisenKoulutustehtävänJakso = ErityisenKoulutustehtävänJakso(date(2012, 9, 1), Some(date(2013, 9, 1)), Koodistokoodiviite("103", Some("Kieliin painottuva koulutus"), "erityinenkoulutustehtava"))
  lazy val ulkomaanjakso = Ulkomaanjakso(date(2012, 9, 1), Some(date(2013, 9, 1)), ruotsi, "Harjoittelua ulkomailla")
  def päättötodistus(oppilaitos: Oppilaitos = jyväskylänNormaalikoulu, toimipiste: OrganisaatioWithOid = jyväskylänNormaalikoulu) = {
    LukionOpiskeluoikeus(
      tila = LukionOpiskeluoikeudenTila(
        List(
          LukionOpiskeluoikeusjakso(alku = date(2012, 9, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
          LukionOpiskeluoikeusjakso(alku = date(2016, 6, 8), tila = opiskeluoikeusPäättynyt, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
        )
      ),
      oppilaitos = Some(oppilaitos),
      lisätiedot = Some(LukionOpiskeluoikeudenLisätiedot(
        pidennettyPäättymispäivä = false,
        ulkomainenVaihtoopiskelija = false,
        erityisenKoulutustehtävänJaksot = Some(List(erityisenKoulutustehtävänJakso)),
        ulkomaanjaksot = Some(List(ulkomaanjakso)),
        oikeusMaksuttomaanAsuntolapaikkaan = None,
        sisäoppilaitosmainenMajoitus = Some(List(Aikajakso(date(2012, 9, 1), Some(date(2013, 9, 1)))))
      )),
      suoritukset = List(
        LukionOppimääränSuoritus2015(
          koulutusmoduuli = lukionOppimäärä,
          oppimäärä = nuortenOpetussuunnitelma,
          suorituskieli = suomenKieli,
          vahvistus = vahvistusPaikkakunnalla(päivä = date(2016, 6, 8)),
          toimipiste = toimipiste,
          todistuksellaNäkyvätLisätiedot = Some("Ruotsin opinnoista osa hyväksiluettu Ruotsissa suoritettujen lukio-opintojen perusteella"),
          ryhmä = Some("12A"),
          osasuoritukset = Some(List(
            suoritus(lukionÄidinkieli("AI1", pakollinen = true)).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("ÄI1")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI2")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI3")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI4")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI5")).copy(arviointi = numeerinenArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI6")).copy(arviointi = numeerinenArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI8")).copy(arviointi = numeerinenArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI9").copy(laajuus = None)).copy(arviointi = numeerinenArviointi(9))
            ))),
            suoritus(lukionKieli("A1", "EN")).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("ENA1")).copy(arviointi = numeerinenArviointi(10)),
              kurssisuoritus(valtakunnallinenKurssi("ENA2")).copy(arviointi = numeerinenArviointi(10)),
              kurssisuoritus(valtakunnallinenKurssi("ENA3")).copy(arviointi = numeerinenArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("ENA4")).copy(arviointi = numeerinenArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("ENA5")).copy(arviointi = numeerinenArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("ENA6")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ENA7")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ENA8")).copy(arviointi = numeerinenArviointi(9)),
              kurssisuoritus(syventäväKurssi("ENA 10", "Abituki", "Aihepiirit liittyvät pakollisten ja valtakunnallisten syventävien kurssien aihekokonaisuuksiin niitä syventäen ja laajentaen. Kurssilla vankennetaan ylioppilaskokeessa tarvittavia tietoja ja taitoja. Pakollisilla ja valtakunnallisilla syventävillä kursseilla hankitun kielioppirakenteiden ja sanaston hallintaa vahvistetaan ja syvennetään. Arvioinnissa (suoritettu/hylätty) otetaan huomioon kaikki kielitaidon osa-alueet ja se perustuu jatkuvaan näyttöön. Kurssin päättyessä opiskelija on saanut lisävalmiuksia osallistua ylioppilaskokeeseen."))
                .copy(arviointi = sanallinenArviointi("S")) // 0.5
            ))),
            suoritus(lukionKieli("B1", "SV")).copy(arviointi = arviointi("7")).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("RUB11")).copy(arviointi = numeerinenArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("RUB12")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("RUB13")).copy(arviointi = numeerinenArviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("RUB14")).copy(arviointi = numeerinenArviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("RUB15")).copy(arviointi = numeerinenArviointi(6))
            ))),
            suoritus(lukionKieli("B3", "LA")).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("LAB31")).copy(arviointi = numeerinenArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("LAB32")).copy(arviointi = numeerinenArviointi(8))
            ))),
            suoritus(matematiikka("MAA", None)).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
              kurssisuoritus(syventäväKurssi("MAA1", "Funktiot ja yhtälöt, pa, vuositaso 1",
                "Vahvistetaan yhtälön ratkaisemisen ja prosenttilaskennan taitoja. Syvennetään verrannollisuuden, neliöjuuren ja potenssin käsitteiden ymmärtämistä. Harjaannutaan käyttämään neliöjuuren ja potenssin laskusääntöjä. Syvennetään funktiokäsitteen ymmärtämistä tutkimalla potenssi- ja eksponenttifunktioita. Opetellaan ratkaisemaan potenssiyhtälöitä."))
                .copy(arviointi = Some(numeerinenArviointi(8).get ::: numeerinenArviointi(9, date(2017, 6, 4)).get)),
              kurssisuoritus(valtakunnallinenKurssi("MAA2")).copy(arviointi = numeerinenArviointi(10)),
              kurssisuoritus(valtakunnallinenKurssi("MAA3")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("MAA4")).copy(arviointi = numeerinenArviointi(10)),
              kurssisuoritus(valtakunnallinenKurssi("MAA5")).copy(arviointi = numeerinenArviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("MAA6")).copy(arviointi = numeerinenArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("MAA7")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("MAA8")).copy(arviointi = numeerinenArviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("MAA9")).copy(arviointi = numeerinenArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("MAA10")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("MAA11")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("MAA12")).copy(arviointi = numeerinenArviointi(10)),
              kurssisuoritus(valtakunnallinenKurssi("MAA13")).copy(arviointi = sanallinenArviointi("H", kuvaus = None)),
              kurssisuoritus(syventäväKurssi("MAA14", "Kertauskurssi, ksy, vuositaso 3", "Harjoitellaan käyttämään opittuja tietoja ja taitoja monipuolisissa ongelmanratkaisutilanteissa."))
                .copy(arviointi = numeerinenArviointi(9)),
              kurssisuoritus(syventäväKurssi("MAA16", "Analyyttisten menetelmien lisäkurssi, ksy, vuositaso 2", "Kurssilla syvennetään kurssien MAA4, MAA5 ja MAA7 sisältöjä."))
                .copy(arviointi = numeerinenArviointi(9))
            ))),
            suoritus(lukionOppiaine("BI", None)).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("BI1")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("BI2")).copy(arviointi = numeerinenArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("BI3")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("BI4")).copy(arviointi = numeerinenArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("BI5")).copy(arviointi = numeerinenArviointi(10)),
              kurssisuoritus(syventäväKurssi("BI6", "Cell Biology (½ kurssia), so, vuositaso 3", "Biology in English, including the structure of procaryotic and eucaryotic cells, cellular control, growth regulation, application of genetics and problemsolving. The course will familiarise students with English terminology, analytical techniques and laboratory work. Students should aim to improve their spoken English skills and vocabulary as well as learn about practical cell biology.")
                .copy(laajuus = Some(laajuus(0.5f)))).copy(arviointi = sanallinenArviointi("S")),
              kurssisuoritus(syventäväKurssi("BI7", "Biologia nova - ympäristö tutuksi (1-3 kurssia), so, vuositasot 1-2", "Kurssiin ei sisälly tuntiopetusta, ainoastaan suunnittelu-, opastus- ja kontrollituokioita. Opiskelija laatii yksilöllisen kurssikokonaisuuden, jonka päätarkoitus on perehtyä valitun (pirkanmaalaisen) ekosysteemin lajistoon. Opiskelija voi erikoistua esim. kasvilajistoon, sieniin, perhosiin, kaloihin, nisäkkäisiin jne. Suunnitelman laajuudesta ja laadusta riippuen opiskelija voi saada 1-3 kurssisuoritusmerkintää. Opiskeluaika voi olla enintään kaksi vuotta."))
                .copy(arviointi = sanallinenArviointi("S")),
              kurssisuoritus(syventäväKurssi("BI8", "Biologian kertauskurssi (½ kurssia), so, vuositaso 3", "Kurssilla kerrataan biologian keskeisiä asioita ainereaaliin valmistauduttaessa."))
                .copy(arviointi = sanallinenArviointi("S"))
            ))),
            suoritus(lukionOppiaine("GE", None)).copy(arviointi = arviointi("8")).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("GE1")).copy(arviointi = numeerinenArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("GE2")).copy(arviointi = numeerinenArviointi(7))
            ))),
            suoritus(lukionOppiaine("FY", None)).copy(arviointi = arviointi("8")).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("FY1")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("FY2")).copy(arviointi = numeerinenArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("FY3")).copy(arviointi = numeerinenArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("FY4")).copy(arviointi = numeerinenArviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("FY5")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("FY6")).copy(arviointi = numeerinenArviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("FY7")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(syventäväKurssi("FY8", "Aine ja säteily, sy, vuositaso 3", "Kurssin sisältö perustuu lähinnä 1900-luvun aikana selvitettyyn tietoon aineesta ja energiasta. Lisäksi käsitellään atomi-, ydin- ja alkeishiukkasfysiikkaa."))
                .copy(arviointi = numeerinenArviointi(7)),
              kurssisuoritus(syventäväKurssi("FY9", "Kokeellinen fysiikka, so, vuositaso 2", "Kurssi syventää ja täydentää muilla kursseilla esiin tulleita tai tulevia asioita. Kurssilla tutkitaan kokeellisesti fysiikan ilmiöitä eri osa-alueilta ja opetellaan raportoimaan tehty koe ja kokeen tulokset."))
                .copy(arviointi = numeerinenArviointi(7)),
              kurssisuoritus(soveltavaKurssi("FY10", "Lukion fysiikan kokonaiskuva, so, vuositaso 3", "Kurssin tavoitteena on luoda kokonaiskuva fysiikasta. Kurssilla syvennetään fysiikan osaamista laskennallisella tasolla ja harjoitellaan reaalikokeeseen vastaamista."))
                .copy(arviointi = sanallinenArviointi("S")),
              kurssisuoritus(syventäväKurssi("FY11", "Fysiikka 11", "Fysiikka 11")).copy(arviointi = sanallinenArviointi("S")),
              kurssisuoritus(syventäväKurssi("FY12", "Fysiikka 12", "Fysiikka 12")).copy(arviointi = sanallinenArviointi("S")),
              kurssisuoritus(syventäväKurssi("FY13", "Fysiikka 13", "Fysiikka 13")).copy(arviointi = sanallinenArviointi("S"))
            ))),
            suoritus(lukionOppiaine("KE", None)).copy(arviointi = arviointi("8")).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("KE1")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("KE2")).copy(arviointi = numeerinenArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("KE3")).copy(arviointi = numeerinenArviointi(9)),
              kurssisuoritus(valtakunnallinenKurssi("KE4")).copy(arviointi = numeerinenArviointi(5)),
              kurssisuoritus(valtakunnallinenKurssi("KE5")).copy(arviointi = numeerinenArviointi(7)),
              kurssisuoritus(soveltavaKurssi("KE6", "Kokeellinen kemia, so, vuositasot 2-3", "Tehdään erilaisia kvantitatiivisia ja kvalitatiivisia määrityksiä, synteesejä ja analyysejä sekä laaditaan työselostuksia. Mahdollisuuksien mukaan tehdään myös vierailuja alan yrityksiin ja oppilaitoksiin."))
                .copy(arviointi = numeerinenArviointi(5)),
              kurssisuoritus(syventäväKurssi("KE7", "Lukion kemian kokonaiskuva, so, vuositaso 3", "Kurssin tavoitteena on kerrata lukion oppimäärä ja antaa kokonaiskuva lukion kemiasta. Harjoitellaan reaalikokeeseen vastaamista."))
                .copy(arviointi = sanallinenArviointi("S")),
              kurssisuoritus(syventäväKurssi("KE8", "Kemia 8", "Kemia 8"))
                .copy(arviointi = sanallinenArviointi("S"))
            ))),
            suoritus(lukionUskonto(uskonto = Some("IS"), diaarinumero = None)).copy(arviointi = arviointi("8")).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("UE1")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("UE2")).copy(arviointi = numeerinenArviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("UE3")).copy(arviointi = numeerinenArviointi(8))
            ))),
            suoritus(lukionOppiaine("FI", None)).copy(arviointi = arviointi("8")).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("FI1")).copy(arviointi = numeerinenArviointi(8))
            ))),
            suoritus(lukionOppiaine("PS", None)).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("PS1")).copy(arviointi = numeerinenArviointi(9))
            ))),
            suoritus(lukionOppiaine("HI", None)).copy(arviointi = arviointi("7")).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("HI1")).copy(arviointi = numeerinenArviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("HI2")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("HI3")).copy(arviointi = numeerinenArviointi(7)),
              kurssisuoritus(valtakunnallinenKurssi("HI4")).copy(arviointi = numeerinenArviointi(6))
            ))),
            suoritus(lukionOppiaine("YH", None)).copy(arviointi = arviointi("8")).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("YH1")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("YH2")).copy(arviointi = numeerinenArviointi(8))
            ))),
            suoritus(lukionOppiaine("LI", None)).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("LI1")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("LI2")).copy(arviointi = numeerinenArviointi(9)),
              kurssisuoritus(soveltavaKurssi("LI12", "Vanhat tanssit, kso", "Kurssin tavoitteena on kehittää sosiaalista vuorovaikutusta tanssin avulla. Tähän liittyy kiinteästi myös tapakasvatus. Kurssilla harjoitellaan ensisijaisesti ns. ”Vanhojen päivän” ohjelmistoa – vanhoja tansseja ja salonkitansseja, mutta myös tavallisia paritansseja. Kurssin käyminen ei velvoita osallistumaan juhlapäivän esityksiin. Kurssi arvioidaan suoritusmerkinnällä."))
                .copy(arviointi = sanallinenArviointi("S"))
            ))),
            suoritus(lukionOppiaine("MU", None)).copy(arviointi = arviointi("8")).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("MU1")).copy(arviointi = numeerinenArviointi(8), tunnustettu = Some(AmmatillinenExampleData.tunnustettu))
            ))),
            suoritus(lukionOppiaine("KU", None)).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("KU1")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(soveltavaKurssi("KU2", "Elävän mallin piirustus, lukiodiplomi", "Elävän mallin piirustus"), Some(true)).copy(arviointi = numeerinenArviointi(9))
            ))),
            suoritus(lukionOppiaine("TE", None)).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("TE1")).copy(arviointi = numeerinenArviointi(8))
            ))),
            suoritus(PaikallinenLukionOppiaine2015(PaikallinenKoodi("ITT", "Tanssi ja liike"), "Tanssi ja liike", pakollinen = false)).copy(arviointi = arviointi("10"), osasuoritukset = Some(List(
              kurssisuoritus(soveltavaKurssi("ITT1", "Tanssin introkurssi", "Opiskelija oppii tuntemaan omaa kehoansa monipuolisesti. Hän osaa käyttää liikkeen peruselementtejä liikkumisessaan\nja kykenee improvisoimaan liikkeellisesti annetun aiheen mukaan.")).copy(arviointi = numeerinenArviointi(10))
            ))),
            MuidenLukioOpintojenSuoritus2015(
              koulutusmoduuli = MuuLukioOpinto2015(Koodistokoodiviite("TO", "lukionmuutopinnot")),
              osasuoritukset = Some(List(
                kurssisuoritus(soveltavaKurssi("MTA", "Monitieteinen ajattelu", "Monitieteisen ajattelun kurssi")).copy(arviointi = sanallinenArviointi("S", päivä = date(2016, 6, 8)))
              )),
              arviointi = arviointi("S")
            ),
            MuidenLukioOpintojenSuoritus2015(
              koulutusmoduuli = MuuLukioOpinto2015(Koodistokoodiviite("OA", "lukionmuutopinnot")),
              osasuoritukset = Some(List(
                kurssisuoritus(soveltavaKurssi("OA1", "Oman äidinkielen keskustelukurssi", "Keskustellaan omalla äidinkielellä keskitetyissä opetusryhmissä")).copy(arviointi = sanallinenArviointi("S", kuvaus = Some("Sujuvaa keskustelua"), päivä = date(2016, 6, 8)))
              )),
              arviointi = arviointi("S")
            )
          ))
        )
      )
    )
  }

  val omanÄidinkielenOpinnotSaame = Some(OmanÄidinkielenOpinnotLaajuusKursseina(
    arvosana = Koodistokoodiviite(koodiarvo = "8", koodistoUri = "arviointiasteikkoyleissivistava"),
    arviointipäivä = None,
    kieli = Kielivalikoima.saame,
    laajuus = Some(LaajuusKursseissa(1))
  ))

  val aineopiskelija =
    LukionOpiskeluoikeus(
      versionumero = None,
      lähdejärjestelmänId = None,
      oppilaitos = Some(jyväskylänNormaalikoulu),
      suoritukset = List(
        LukionOppiaineenOppimääränSuoritus2015(
          koulutusmoduuli = lukionOppiaine("HI", diaarinumero = Some("60/011/2015")),
          suorituskieli = suomenKieli,
          vahvistus = vahvistusPaikkakunnalla(päivä = date(2016, 1, 10)),
          toimipiste = jyväskylänNormaalikoulu,
          arviointi = arviointi("9"),
          osasuoritukset = Some(List(
            kurssisuoritus(valtakunnallinenVanhanOpsinKurssi("HI1")).copy(arviointi = numeerinenArviointi(7, päivä = date(2016, 1, 10))),
            kurssisuoritus(valtakunnallinenKurssi("HI2")).copy(arviointi = numeerinenArviointi(8, päivä = date(2016, 1, 10))),
            kurssisuoritus(valtakunnallinenKurssi("HI3")).copy(arviointi = numeerinenArviointi(7, päivä = date(2016, 1, 10))),
            kurssisuoritus(valtakunnallinenKurssi("HI4")).copy(arviointi = numeerinenArviointi(6, päivä = date(2016, 1, 10)))
          ))
        )
      ),
      tila = LukionOpiskeluoikeudenTila(
        List(
          LukionOpiskeluoikeusjakso(alku = date(2015, 9, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
          LukionOpiskeluoikeusjakso(alku = date(2016, 1, 10), tila = opiskeluoikeusPäättynyt, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
        )
      )
    )

  val aineOpiskelijaAktiivinen =
    LukionOpiskeluoikeus(
      versionumero = None,
      lähdejärjestelmänId = None,
      oppilaitos = Some(jyväskylänNormaalikoulu),
      suoritukset = List(
        LukionOppiaineenOppimääränSuoritus2015(
          koulutusmoduuli = lukionOppiaine("HI", diaarinumero = Some("60/011/2015")),
          suorituskieli = suomenKieli,
          vahvistus = vahvistusPaikkakunnalla(päivä = date(2016, 1, 10)),
          toimipiste = jyväskylänNormaalikoulu,
          arviointi = arviointi("9"),
          osasuoritukset = Some(List(
            kurssisuoritus(valtakunnallinenVanhanOpsinKurssi("HI1")).copy(arviointi = numeerinenArviointi(7, päivä = date(2016, 1, 10))),
            kurssisuoritus(valtakunnallinenKurssi("HI2")).copy(arviointi = numeerinenArviointi(8, päivä = date(2016, 1, 10))),
            kurssisuoritus(valtakunnallinenKurssi("HI3")).copy(arviointi = numeerinenArviointi(7, päivä = date(2016, 1, 10))),
            kurssisuoritus(valtakunnallinenKurssi("HI4")).copy(arviointi = numeerinenArviointi(6, päivä = date(2016, 1, 10)))
          ))
        ),
        LukionOppiaineenOppimääränSuoritus2015(
          koulutusmoduuli = lukionOppiaine("KE", diaarinumero = Some("60/011/2015")),
          suorituskieli = suomenKieli,
          vahvistus = vahvistusPaikkakunnalla(päivä = date(2015, 1, 10)),
          toimipiste = jyväskylänNormaalikoulu,
          arviointi = arviointi("8"),
          osasuoritukset = Some(List(
            kurssisuoritus(valtakunnallinenVanhanOpsinKurssi("KE1")).copy(arviointi = numeerinenArviointi(7, päivä = date(2016, 1, 10)))
          ))
        ),
        LukionOppiaineenOppimääränSuoritus2015(
          koulutusmoduuli = lukionOppiaine("FI", diaarinumero = Some("60/011/2015")),
          suorituskieli = suomenKieli,
          toimipiste = jyväskylänNormaalikoulu,
          arviointi = arviointi("9"),
          osasuoritukset = Some(List(
            kurssisuoritus(valtakunnallinenVanhanOpsinKurssi("FI1")).copy(arviointi = numeerinenArviointi(7, päivä = date(2016, 1, 10)))
          ))
        )
      ),
      tila = LukionOpiskeluoikeudenTila(
        List(
          LukionOpiskeluoikeusjakso(alku = date(2015, 9, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
        )
      )
    )


  val aineOpiskelijaEiTiedossaOppiaineella = aineOpiskelijaAktiivinen.copy(
    suoritukset = aineOpiskelijaAktiivinen.suoritukset ::: List(
      LukionOppiaineenOppimääränSuoritus2015(
        koulutusmoduuli = EiTiedossaOppiaine(),
        suorituskieli = suomenKieli,
        toimipiste = jyväskylänNormaalikoulu,
        arviointi = arviointi("9"),
        osasuoritukset = Some(List(
          kurssisuoritus(valtakunnallinenVanhanOpsinKurssi("FI1")).copy(arviointi = numeerinenArviointi(7, päivä = date(2016, 1, 10)))
        ))
      ))
  )

  val lukioKesken =
    LukionOpiskeluoikeus(
      versionumero = None,
      lähdejärjestelmänId = None,
      tila = LukionOpiskeluoikeudenTila(
        List(
          LukionOpiskeluoikeusjakso(alku = date(2012, 9, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
        )
      ),
      oppilaitos = Some(jyväskylänNormaalikoulu),
      suoritukset = List(
        LukionOppimääränSuoritus2015(
          koulutusmoduuli = lukionOppimäärä,
          oppimäärä = nuortenOpetussuunnitelma,
          suorituskieli = suomenKieli,
          omanÄidinkielenOpinnot = omanÄidinkielenOpinnotSaame,
          vahvistus = None,
          toimipiste = jyväskylänNormaalikoulu,
          todistuksellaNäkyvätLisätiedot = None,
          osasuoritukset = Some(List(
            suoritus(lukionÄidinkieli("AI1", pakollinen = true)).copy(arviointi = None).copy(osasuoritukset = Some(List(
              kurssisuoritus(valtakunnallinenKurssi("ÄI1")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI2")).copy(arviointi = numeerinenArviointi(8)),
              kurssisuoritus(valtakunnallinenKurssi("ÄI3")).copy(arviointi = None)
            )))
          ))
        )
      )
    )


  val sisältyvä = lukioKesken.copy(
    sisältyyOpiskeluoikeuteen = Some(SisältäväOpiskeluoikeus(ressunLukio, "1.2.246.562.15.97433262579"))
  )

  val aineOpiskelijaKesken = aineopiskelija.copy(tila = LukionOpiskeluoikeudenTila(
        List(LukionOpiskeluoikeusjakso(alku = date(2015, 9, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))))
  )

  val examples = List(
    Example("lukio - uusi", "Uusi oppija lisätään suorittamaan lukiota", oppija(lukionOpiskeluoikeus())),
    Example("lukio - päättötodistus", "Oppija on saanut päättötodistuksen", oppija(päättötodistus())),
    Example("lukio - lukion oppiaineen oppimäärä - päättötodistus", "Opiskelija on suorittanut lukion historian oppimäärän", oppija(aineopiskelija)),
    Example("lukio - lukion oppiaineen oppimäärä - kesken", "Valmiita ja keskeneräisiä oppiaineita", oppija(aineOpiskelijaAktiivinen)),
    Example("lukio - sisältyy toisen oppilaitoksen opiskeluoikeuteen", "Toisen oppilaitoksen opiskeluoikeuteen sisältyvä opiskeluoikeus", oppija(sisältyvä), statusCode = 400)
  )
}

object LukioExampleData {
  def arviointi(arvosana: String): Some[List[LukionOppiaineenArviointi]] = {
    Some(List(LukionOppiaineenArviointi(arvosana)))
  }

  val hyväksytty = Some(List(LukionOppiaineenArviointi("S")))

  val exampleHenkilö = asUusiOppija(KoskiSpecificMockOppijat.lukiolainen)

  val ylioppilastutkinto: Ylioppilastutkinto = Ylioppilastutkinto(perusteenDiaarinumero = Some("60/011/2015"))

  val lukionOppimäärä: LukionOppimäärä = LukionOppimäärä(perusteenDiaarinumero = Some("60/011/2015"))

  val lukionOppiaineenOppimääränSuoritusYhteiskuntaoppi: LukionOppiaineenOppimääränSuoritus2015 = LukionOppiaineenOppimääränSuoritus2015(
    koulutusmoduuli = lukionOppiaine("YH", diaarinumero = Some("60/011/2015")),
    suorituskieli = suomenKieli,
    vahvistus = vahvistusPaikkakunnalla(päivä = date(2016, 1, 10)),
    toimipiste = jyväskylänNormaalikoulu,
    arviointi = arviointi("9"),
    osasuoritukset = Some(List(
      kurssisuoritus(valtakunnallinenKurssi("YH1")).copy(arviointi = numeerinenArviointi(8, päivä = date(2016, 1, 10))),
      kurssisuoritus(valtakunnallinenKurssi("YH2")).copy(arviointi = numeerinenArviointi(7, päivä = date(2016, 1, 10)))
    ))
  )

  val lukionOppiaineenOppimääränSuoritusFilosofia: LukionOppiaineenOppimääränSuoritus2015 = LukionOppiaineenOppimääränSuoritus2015(
    koulutusmoduuli = lukionOppiaine("FI", diaarinumero = Some("60/011/2015")),
    suorituskieli = suomenKieli,
    vahvistus = vahvistusPaikkakunnalla(päivä = date(2016, 1, 10)),
    toimipiste = jyväskylänNormaalikoulu,
    arviointi = arviointi("10"),
    osasuoritukset = Some(List(
      kurssisuoritus(valtakunnallinenKurssi("FI1")).copy(arviointi = numeerinenArviointi(8, päivä = date(2016, 1, 10))),
      kurssisuoritus(valtakunnallinenKurssi("FI2", syventävä)).copy(arviointi = numeerinenArviointi(7, päivä = date(2016, 1, 10))),
      kurssisuoritus(valtakunnallinenKurssi("FI3", syventävä)).copy(arviointi = numeerinenArviointi(6, päivä = date(2016, 1, 10)))
    ))
  )

  val lukionOppiaineenOppimääränSuoritusA1Englanti: LukionOppiaineenOppimääränSuoritus2015 = LukionOppiaineenOppimääränSuoritus2015(
    koulutusmoduuli = lukionKieli("A1","EN").copy(perusteenDiaarinumero = Some("60/011/2015")),
    suorituskieli = suomenKieli,
    vahvistus = vahvistusPaikkakunnalla(päivä = date(2016, 1, 10)),
    toimipiste = jyväskylänNormaalikoulu,
    arviointi = arviointi("7"),
    osasuoritukset = Some(List(
      kurssisuoritus(valtakunnallinenKurssi("ENA1")).copy(arviointi = numeerinenArviointi(10)),
      kurssisuoritus(valtakunnallinenKurssi("ENA2")).copy(arviointi = numeerinenArviointi(10)),
      kurssisuoritus(valtakunnallinenKurssi("ENA3")).copy(arviointi = numeerinenArviointi(9))
    ))
  )

  val lukionOppiaineenOppimääränSuoritusPitkäMatematiikka: LukionOppiaineenOppimääränSuoritus2015 = LukionOppiaineenOppimääränSuoritus2015(
    koulutusmoduuli = matematiikka("MAA", perusteenDiaarinumero = Some("60/011/2015")),
    suorituskieli = suomenKieli,
    vahvistus = vahvistusPaikkakunnalla(päivä = date(2016, 1, 10)),
    toimipiste = jyväskylänNormaalikoulu,
    arviointi = arviointi("8"),
    osasuoritukset = Some(List(
      kurssisuoritus(valtakunnallinenKurssi("MAA2")).copy(arviointi = numeerinenArviointi(6)),
      kurssisuoritus(valtakunnallinenKurssi("MAA3")).copy(arviointi = numeerinenArviointi(7)),
      kurssisuoritus(valtakunnallinenKurssi("MAA4")).copy(arviointi = numeerinenArviointi(8)),
      kurssisuoritus(valtakunnallinenKurssi("MAA5")).copy(arviointi = numeerinenArviointi(9)),
      kurssisuoritus(valtakunnallinenKurssi("MAA6")).copy(arviointi = numeerinenArviointi(10))
    ))
  )

  val opiskeluoikeusAktiivinen = Koodistokoodiviite("lasna", Some("Läsnä"), "koskiopiskeluoikeudentila", Some(1))
  val opiskeluoikeusPäättynyt = Koodistokoodiviite("valmistunut", Some("Valmistunut"), "koskiopiskeluoikeudentila", Some(1))

  val aikuistenOpetussuunnitelma = Koodistokoodiviite("aikuistenops", Some("Aikuisten ops"), "lukionoppimaara", Some(1))
  val nuortenOpetussuunnitelma = Koodistokoodiviite("nuortenops", Some("Nuorten ops"), "lukionoppimaara", Some(1))

  def suoritus(aine: LukionOppiaine2015): LukionOppiaineenSuoritus2015 = LukionOppiaineenSuoritus2015(
    koulutusmoduuli = aine,
    suorituskieli = None,
    arviointi = None,
    osasuoritukset = None
  )

  def kurssisuoritus(kurssi: LukionKurssi2015, suoritettuLukiodiplomina: Option[Boolean] = None) = LukionKurssinSuoritus2015(
    koulutusmoduuli = kurssi,
    suorituskieli = None,
    arviointi = None,
    suoritettuLukiodiplomina = suoritettuLukiodiplomina
  )

  lazy val pakollinen = Koodistokoodiviite("pakollinen", "lukionkurssintyyppi")
  lazy val syventävä = Koodistokoodiviite("syventava", "lukionkurssintyyppi")
  lazy val soveltava = Koodistokoodiviite("soveltava", "lukionkurssintyyppi")

  def valtakunnallinenKurssi(kurssi: String, kurssinTyyppi: Koodistokoodiviite = pakollinen) =
    ValtakunnallinenLukionKurssi2015(Koodistokoodiviite(koodistoUri = "lukionkurssit", koodiarvo = kurssi), Some(laajuus(1.0f)), kurssinTyyppi = kurssinTyyppi)

  def valtakunnallinenVanhanOpsinKurssi(kurssi: String, kurssinTyyppi: Koodistokoodiviite = pakollinen) =
    ValtakunnallinenLukionKurssi2015(Koodistokoodiviite(koodistoUri = "lukionkurssitops2003nuoret", koodiarvo = kurssi), Some(laajuus(1.0f)), kurssinTyyppi = kurssinTyyppi)

  def syventäväKurssi(koodi: String, nimi: String, kuvaus: String) =
    PaikallinenLukionKurssi2015(PaikallinenKoodi(koodiarvo = koodi, nimi = nimi), Some(laajuus(1.0f)), kuvaus, kurssinTyyppi = syventävä)

  def soveltavaKurssi(koodi: String, nimi: String, kuvaus: String) =
    PaikallinenLukionKurssi2015(PaikallinenKoodi(koodiarvo = koodi, nimi = nimi), Some(laajuus(1.0f)), kuvaus, kurssinTyyppi = soveltava)

  def matematiikka(matematiikka: String, laajuus: LaajuusKursseissa, perusteenDiaarinumero: Option[String]) =
    LukionMatematiikka2015(oppimäärä = Koodistokoodiviite(koodiarvo = matematiikka, koodistoUri = "oppiainematematiikka"), perusteenDiaarinumero = perusteenDiaarinumero, laajuus = Some(laajuus))

  def matematiikka(matematiikka: String, perusteenDiaarinumero: Option[String]) =
    LukionMatematiikka2015(oppimäärä = Koodistokoodiviite(koodiarvo = matematiikka, koodistoUri = "oppiainematematiikka"), perusteenDiaarinumero = perusteenDiaarinumero)

  def laajuus(laajuus: Double, yksikkö: String = laajuusKursseissa.koodiarvo): LaajuusKursseissa = LaajuusKursseissa(laajuus, Koodistokoodiviite(koodistoUri = "opintojenlaajuusyksikko", koodiarvo = yksikkö))

  def lukionOppiaine(aine: String, laajuus: LaajuusKursseissa, diaarinumero: Option[String]) =
    LukionMuuValtakunnallinenOppiaine2015(tunniste = Koodistokoodiviite(koodistoUri = "koskioppiaineetyleissivistava", koodiarvo = aine), perusteenDiaarinumero = diaarinumero, laajuus = Some(laajuus))

  def lukionOppiaine(aine: String, diaarinumero: Option[String]) =
    LukionMuuValtakunnallinenOppiaine2015(tunniste = Koodistokoodiviite(koodistoUri = "koskioppiaineetyleissivistava", koodiarvo = aine), perusteenDiaarinumero = diaarinumero)

  def lukionUskonto(uskonto: Option[String], laajuus: LaajuusKursseissa, diaarinumero: Option[String]): LukionUskonto2015 =
    LukionUskonto2015(
      tunniste = Koodistokoodiviite(koodistoUri = "koskioppiaineetyleissivistava", koodiarvo = "KT"),
      uskonnonOppimäärä = uskonto.map(u => Koodistokoodiviite(koodistoUri = "uskonnonoppimaara", koodiarvo = u)),
      perusteenDiaarinumero = diaarinumero,
      laajuus = Some(laajuus)
    )

  def lukionUskonto(uskonto: Option[String], diaarinumero: Option[String]): LukionUskonto2015 =
    LukionUskonto2015(
      tunniste = Koodistokoodiviite(koodistoUri = "koskioppiaineetyleissivistava", koodiarvo = "KT"),
      uskonnonOppimäärä = uskonto.map(u => Koodistokoodiviite(koodistoUri = "uskonnonoppimaara", koodiarvo = u)),
      perusteenDiaarinumero = diaarinumero
    )

  def lukionÄidinkieli(kieli: String, laajuus: LaajuusKursseissa, pakollinen: Boolean) =
    LukionÄidinkieliJaKirjallisuus2015(kieli = Koodistokoodiviite(koodiarvo = kieli, koodistoUri = "oppiaineaidinkielijakirjallisuus"), laajuus = Some(laajuus), pakollinen = pakollinen)

  def lukionÄidinkieli(kieli: String, pakollinen: Boolean) =
    LukionÄidinkieliJaKirjallisuus2015(kieli = Koodistokoodiviite(koodiarvo = kieli, koodistoUri = "oppiaineaidinkielijakirjallisuus"), pakollinen = pakollinen)

  def lukionKieli(oppiaine: String, kieli: String) = {
    VierasTaiToinenKotimainenKieli2015(
      tunniste = Koodistokoodiviite(koodiarvo = oppiaine, koodistoUri = "koskioppiaineetyleissivistava"),
      kieli = Koodistokoodiviite(koodiarvo = kieli, koodistoUri = "kielivalikoima"))
  }

  def numeerinenArviointi(arvosana: Int, päivä: LocalDate = date(2016, 6, 4)): Some[List[LukionArviointi]] = {
    Some(List(new NumeerinenLukionArviointi(arvosana = Koodistokoodiviite(koodiarvo = arvosana.toString, koodistoUri = "arviointiasteikkoyleissivistava"), päivä)))
  }

  def sanallinenArviointi(arvosana: String, kuvaus: Option[String] = None, päivä: LocalDate = date(2016, 6, 4)): Some[List[LukionArviointi]] = (Arviointi.numeerinen(arvosana), kuvaus) match {
    case (Some(numero), None) => numeerinenArviointi(numero, päivä)
    case _ => Some(List(new SanallinenLukionArviointi(arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkoyleissivistava"), kuvaus.map(LocalizedString.finnish), päivä)))
  }

  def lukionOpiskeluoikeus(oppilaitos: Oppilaitos = jyväskylänNormaalikoulu) = LukionOpiskeluoikeus(
    versionumero = None,
    lähdejärjestelmänId = None,
    oppilaitos = Some(oppilaitos),
    suoritukset = List(
      LukionOppimääränSuoritus2015(
        koulutusmoduuli = lukionOppimäärä,
        oppimäärä = nuortenOpetussuunnitelma,
        suorituskieli = suomenKieli,
        toimipiste = oppilaitos,
        osasuoritukset = None
      )
    ),
    tila = LukionOpiskeluoikeudenTila(
      List(
        LukionOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
      )
    )
  )

  def alkamispäivällä(oo: LukionOpiskeluoikeus, alkamispäivä: LocalDate): LukionOpiskeluoikeus =
    lisääTila(
      oo.copy(tila = new LukionOpiskeluoikeudenTila(opiskeluoikeusjaksot = List())),
      alkamispäivä,
      ExampleData.opiskeluoikeusLäsnä
    )

  def lisääTila(oo: LukionOpiskeluoikeus, päivä: LocalDate, tila: Koodistokoodiviite): LukionOpiskeluoikeus = oo.copy(
    tila = LukionOpiskeluoikeudenTila(oo.tila.opiskeluoikeusjaksot ++ List(LukionOpiskeluoikeusjakso(päivä, tila, Some(ExampleData.valtionosuusRahoitteinen))))
  )
}
