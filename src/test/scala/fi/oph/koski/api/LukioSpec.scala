package fi.oph.koski.api

import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.schema.TaydellisetHenkilötiedot
import org.scalatest.{FunSpec, Matchers}

class LukioSpec extends FunSpec with Matchers with OpintosuoritusoteTestMethods with TodistusTestMethods with OpiskeluOikeusTestMethods {
  describe("Lukio") {
    it("Opintosuoritusote") {
      opintosuoritusote(MockOppijat.lukiolainen) should equal(
        """Opintosuoritukset
          |Kurssia Arvosana Suor.pvm
          |A1 A1-kieli, englanti 9 9
          |ENA 10 Abituki 1 hyväksytty 4.6.2016
          |ENA1 Englannin kieli ja maailmani 1 10 4.6.2016
          |ENA2 Ihminen verkostoissa 1 10 4.6.2016
          |ENA7 Kestävä elämäntapa 1 8 4.6.2016
          |ENA3 Kulttuuri-ilmiöitä 1 9 4.6.2016
          |ENA6 Opiskelu, työ ja toimeentulo 1 8 4.6.2016
          |ENA5 Tiede ja tulevaisuus 1 9 4.6.2016
          |ENA8 Viesti ja vaikuta puhuen 1 9 4.6.2016
          |ENA4 Yhteiskunta ja ympäröivä maailma 1 9 4.6.2016
          |B1 B1-kieli, ruotsi 5 7
          |RUB12 Hyvinvointi ja ihmissuhteet 1 8 4.6.2016
          |RUB13 Kulttuuri ja mediat 1 7 4.6.2016
          |RUB11 Minun ruotsini 1 9 4.6.2016
          |RUB14 Monenlaiset elinympäristömme 1 7 4.6.2016
          |RUB15 Opiskelu- ja työelämää ruotsiksi 1 6 4.6.2016
          |B3 B3-kieli, latina 2 9
          |LAB32 Antiikin elämää 1 8 4.6.2016
          |LAB31 Ikkunat auki antiikkiin 1 9 4.6.2016
          |BI Biologia 7,5 9
          |BI7 Biologia nova - ympäristö tutuksi (1-3 kurssia), so, vuositasot 1-2 1 hyväksytty 4.6.2016
          |BI8 Biologian kertauskurssi (½ kurssia), so, vuositaso 3 1 hyväksytty 4.6.2016
          |BI5 Biologian sovellukset 1 10 4.6.2016
          |BI6 Cell Biology (½ kurssia), so, vuositaso 3 0,5 hyväksytty 4.6.2016
          |BI2 Ekologia ja ympäristö 1 9 4.6.2016
          |BI1 Elämä ja evoluutio 1 8 4.6.2016
          |BI4 Ihmisen biologia 1 9 4.6.2016
          |BI3 Solu ja perinnöllisyys 1 8 4.6.2016
          |FI Filosofia 1 8
          |FI1 Johdatus filosofiseen ajatteluun 1 8 4.6.2016
          |FY Fysiikka 13 8
          |FY7 Aine ja säteily 1 8 4.6.2016
          |FY8 Aine ja säteily, sy, vuositaso 3 1 7 4.6.2016
          |FY11 Fysiikka 11 1 hyväksytty 4.6.2016
          |FY12 Fysiikka 12 1 hyväksytty 4.6.2016
          |FY13 Fysiikka 13 1 hyväksytty 4.6.2016
          |FY1 Fysiikka luonnontieteenä 1 8 4.6.2016
          |FY5 Jaksollinen liike ja aallot 1 8 4.6.2016
          |FY9 Kokeellinen fysiikka, so, vuositaso 2 1 7 4.6.2016
          |FY10 Lukion fysiikan kokonaiskuva, so, vuositaso 3 1 hyväksytty 4.6.2016
          |FY2 Lämpö 1 9 4.6.2016
          |FY3 Sähkö 1 9 4.6.2016
          |FY6 Sähkömagnetismi 1 7 4.6.2016
          |FY4 Voima ja liike 1 7 4.6.2016
          |HI Historia 4 7
          |HI4 Eurooppalaisen maailmankuvan kehitys 1 6 4.6.2016
          |HI1 Ihminen ympäristön ja yhteiskuntien muutoksessa 1 7 4.6.2016
          |HI3 Itsenäisen Suomen historia 1 7 4.6.2016
          |HI2 Kansainväliset suhteet 1 8 4.6.2016
          |KE Kemia 8 8
          |KE2 Ihmisen ja elinympäristön kemiaa 1 9 4.6.2016
          |KE8 Kemia 8 1 hyväksytty 4.6.2016
          |KE1 Kemiaa kaikkialla 1 8 4.6.2016
          |KE6 Kokeellinen kemia, so, vuositasot 2-3 1 5 4.6.2016
          |KE7 Lukion kemian kokonaiskuva, so, vuositaso 3 1 hyväksytty 4.6.2016
          |KE4 Materiaalit ja teknologia 1 5 4.6.2016
          |KE3 Reaktiot ja energia 1 9 4.6.2016
          |KE5 Reaktiot ja tasapaino 1 7 4.6.2016
          |KU Kuvataide 2 9
          |KU1 Kuvat ja kulttuurit 1 8 4.6.2016
          |KU2 Muotoillut ja rakennetut ympäristöt 1 9 4.6.2016
          |LI Liikunta 3 9
          |LI2 Aktiivinen elämäntapa 1 9 4.6.2016
          |LI1 Energiaa liikunnasta 1 8 4.6.2016
          |LI12 Vanhat tanssit, kso 1 hyväksytty 4.6.2016
          |GE Maantieto 2 8
          |GE1 Maailma muutoksessa 1 9 4.6.2016
          |GE2 Sininen planeetta 1 7 4.6.2016
          |MA Matematiikka, pitkä oppimäärä 15 9
          |MAA12 Algoritmit matematiikassa 1 10 4.6.2016
          |MAA5 Analyyttinen geometria 1 7 4.6.2016
          |MAA16 Analyyttisten menetelmien lisäkurssi, ksy, vuositaso 2 1 9 4.6.2016
          |MAA6 Derivaatta 1 9 4.6.2016
          |MAA13 Differentiaali- ja integraalilaskennan jatkokurssi 1 8 4.6.2016
          |MAA1 Funktiot ja yhtälöt, pa, vuositaso 1 1 9 4.6.2016
          |MAA3 Geometria 1 8 4.6.2016
          |MAA9 Integraalilaskenta 1 9 4.6.2016
          |MAA8 Juuri- ja logaritmifunktiot 1 7 4.6.2016
          |MAA14 Kertauskurssi, ksy, vuositaso 3 1 9 4.6.2016
          |MAA11 Lukuteoria ja todistaminen 1 8 4.6.2016
          |MAA2 Polynomifunktiot ja -yhtälöt 1 10 4.6.2016
          |MAA10 Todennäköisyys ja tilastot 1 8 4.6.2016
          |MAA7 Trigonometriset funktiot 1 8 4.6.2016
          |MAA4 Vektorit 1 10 4.6.2016
          |MU Musiikki 1 8
          |MU1 Musiikki ja minä 1 8 4.6.2016
          |PS Psykologia 1 9
          |PS1 Psyykkinen toiminta ja oppiminen 1 9 4.6.2016
          |TE Terveystieto 1 9
          |TE1 Terveyden perusteet 1 8 4.6.2016
          |KT Uskonto tai elämänkatsomustieto, Evankelisluterilainen uskonto 3 8
          |UE3 Maailman uskontoja ja uskonnollisia liikkeitä 1 8 4.6.2016
          |UE2 Maailmanlaajuinen kristinusko 1 7 4.6.2016
          |UE1 Uskonto ilmiönä – kristinuskon, juutalaisuuden ja islamin jäljillä 1 8 4.6.2016
          |YH Yhteiskuntaoppi 2 8
          |YH1 Suomalainen yhteiskunta 1 8 4.6.2016
          |YH2 Taloustieto 1 8 4.6.2016
          |AI Äidinkieli ja kirjallisuus 8 9
          |ÄI2 Kieli, kulttuuri ja identiteetti 1 8 4.6.2016
          |ÄI3 Kirjallisuuden keinoja ja tulkintaa 1 8 4.6.2016
          |ÄI8 Kirjoittamistaitojen syventäminen 1 9 4.6.2016
          |ÄI9 Lukutaitojen syventäminen 1 9 4.6.2016
          |ÄI6 Nykykulttuuri ja kertomukset 1 9 4.6.2016
          |ÄI5 Teksti ja konteksti 1 9 4.6.2016
          |ÄI4 Tekstit ja vaikuttaminen 1 8 4.6.2016
          |ÄI1 Tekstit ja vuorovaikutus 1 8 4.6.2016""".stripMargin
      )
    }

    it("Päättötodistus") {
      todistus(MockOppijat.lukiolainen.oid, "lukiokoulutus") should equal("""Lukion päättötodistus
                                                            |Jyväskylän yliopisto
                                                            |Jyväskylän normaalikoulu
                                                            |Lukiolainen, Liisa 110496-9369
                                                            |
                                                            |Äidinkieli ja kirjallisuus 8 Kiitettävä 9
                                                            |A1-kieli, englanti 9 Kiitettävä 9
                                                            |B1-kieli, ruotsi 5 Tyydyttävä 7
                                                            |B3-kieli, latina 2 Kiitettävä 9
                                                            |Matematiikka, pitkä oppimäärä 15 Kiitettävä 9
                                                            |Biologia 7,5 Kiitettävä 9
                                                            |Maantieto 2 Hyvä 8
                                                            |Fysiikka 13 Hyvä 8
                                                            |Kemia 8 Hyvä 8
                                                            |Uskonto tai elämänkatsomustieto, Evankelisluterilainen uskonto 3 Hyvä 8
                                                            |Filosofia 1 Hyvä 8
                                                            |Psykologia 1 Kiitettävä 9
                                                            |Historia 4 Tyydyttävä 7
                                                            |Yhteiskuntaoppi 2 Hyvä 8
                                                            |Liikunta 3 Kiitettävä 9
                                                            |Musiikki 1 Hyvä 8
                                                            |Kuvataide 2 Kiitettävä 9
                                                            |Terveystieto 1 Kiitettävä 9
                                                            |Opiskelijan suorittama kokonaiskurssimäärä 87,5""".stripMargin)
    }
  }

  def opintosuoritusote(henkilö: TaydellisetHenkilötiedot): String = {
    opintosuoritusoteOpiskeluoikeudelle(henkilö.oid, lukionOpiskeluoikeus)
  }

  def lukionOpiskeluoikeus = {
    opiskeluoikeus(MockOppijat.lukiolainen.oid, "lukiokoulutus").id.get
  }
}