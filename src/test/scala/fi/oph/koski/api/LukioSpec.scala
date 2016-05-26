package fi.oph.koski.api

import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.organisaatio.MockOrganisaatiot
import org.scalatest.{FunSpec, Matchers}

class LukioSpec extends FunSpec with Matchers with OpintosuoritusoteTestMethods {
  describe("Lukio") {
    it("Opintosuoritusote") {
      opintosuoritusote(MockOppijat.lukiolainen.hetu, MockOrganisaatiot.jyväskylänNormaalikoulu) should equal(
        """Opintosuoritukset
          |Kurssia Arvosana Suor.pvm
          |A1 A1-kieli, englanti 9 kiitettävä
          |ENA 10 Abituki 1 hyväksytty
          |ENA1 Englannin kieli ja maailmani  1 erinomainen
          |ENA2 Ihminen verkostoissa  1 erinomainen
          |ENA7 Kestävä elämäntapa  1 hyvä
          |ENA3 Kulttuuri-ilmiöitä  1 kiitettävä
          |ENA6 Opiskelu, työ ja toimeentulo  1 hyvä
          |ENA5 Tiede ja tulevaisuus 1 kiitettävä
          |ENA8 Viesti ja vaikuta puhuen  1 kiitettävä
          |ENA4 Yhteiskunta ja ympäröivä maailma  1 kiitettävä
          |B1 B1-kieli, ruotsi 5 tyydyttävä
          |RUB12 Hyvinvointi ja ihmissuhteet 1 hyvä
          |RUB13 Kulttuuri ja mediat 1 tyydyttävä
          |RUB11 Minun ruotsini 1 kiitettävä
          |RUB14 Monenlaiset elinympäristömme 1 tyydyttävä
          |RUB15 Opiskelu- ja työelämää ruotsiksi 1 kohtalainen
          |B3 B3-kieli, latina 2 kiitettävä
          |LAB32 Antiikin elämää  1 hyvä
          |LAB31 Ikkunat auki antiikkiin 1 kiitettävä
          |BI Biologia 7,5 kiitettävä
          |BI7 Biologia nova - ympäristö tutuksi (1-3 kurssia), so, vuositasot 1-2 1 hyväksytty
          |BI8 Biologian kertauskurssi (½ kurssia), so, vuositaso 3 1 hyväksytty
          |BI5 Biologian sovellukset  1 erinomainen
          |BI6 Cell Biology (½ kurssia), so, vuositaso 3 0,5 hyväksytty
          |BI2 Ekologia ja ympäristö  1 kiitettävä
          |BI1 Elämä ja evoluutio  1 hyvä
          |BI4 Ihmisen biologia  1 kiitettävä
          |BI3 Solu ja perinnöllisyys  1 hyvä
          |FI Filosofia 1 hyvä
          |FI1 Johdatus filosofiseen ajatteluun  1 hyvä
          |FY Fysiikka 13 hyvä
          |FY7 Aine ja säteily  1 hyvä
          |FY8 Aine ja säteily, sy, vuositaso 3 1 tyydyttävä
          |FY11 Fysiikka 11 1 hyväksytty
          |FY12 Fysiikka 12 1 hyväksytty
          |FY13 Fysiikka 13 1 hyväksytty
          |FY1 Fysiikka luonnontieteenä  1 hyvä
          |FY5 Jaksollinen liike ja aallot  1 hyvä
          |FY9 Kokeellinen fysiikka, so, vuositaso 2 1 tyydyttävä
          |FY10 Lukion fysiikan kokonaiskuva, so, vuositaso 3 1 hyväksytty
          |FY2 Lämpö 1 kiitettävä
          |FY3 Sähkö  1 kiitettävä
          |FY6 Sähkömagnetismi 1 tyydyttävä
          |FY4 Voima ja liike  1 tyydyttävä
          |HI Historia 4 tyydyttävä
          |HI4 Eurooppalaisen maailmankuvan kehitys  1 kohtalainen
          |HI1 Ihminen ympäristön ja yhteiskuntien muutoksessa  1 tyydyttävä
          |HI3 Itsenäisen Suomen historia  1 tyydyttävä
          |HI2 Kansainväliset suhteet  1 hyvä
          |KE Kemia 8 hyvä
          |KE2 Ihmisen ja elinympäristön kemiaa  1 kiitettävä
          |KE8 Kemia 8 1 hyväksytty
          |KE1 Kemiaa kaikkialla  1 hyvä
          |KE6 Kokeellinen kemia, so, vuositasot 2-3 1 välttävä
          |KE7 Lukion kemian kokonaiskuva, so, vuositaso 3 1 hyväksytty
          |KE4 Materiaalit ja teknologia  1 välttävä
          |KE3 Reaktiot ja energia  1 kiitettävä
          |KE5 Reaktiot ja tasapaino  1 tyydyttävä
          |KU Kuvataide 2 kiitettävä
          |KU1 Kuvat ja kulttuurit  1 hyvä
          |KU2 Muotoillut ja rakennetut ympäristöt  1 kiitettävä
          |LI Liikunta 3 kiitettävä
          |LI2 Aktiivinen elämäntapa  1 kiitettävä
          |LI1 Energiaa liikunnasta  1 hyvä
          |LI12 Vanhat tanssit, kso 1 hyväksytty
          |GE Maantieto 2 hyvä
          |GE1 Maailma muutoksessa  1 kiitettävä
          |GE2 Sininen planeetta  1 tyydyttävä
          |MA  Matematiikka, pitkä oppimäärä 15 kiitettävä
          |MAA12 Algoritmit matematiikassa  1 erinomainen
          |MAA5 Analyyttinen geometria  1 tyydyttävä
          |MAA16 Analyyttisten menetelmien lisäkurssi, ksy, vuositaso 2 1 kiitettävä
          |MAA6 Derivaatta 1 kiitettävä
          |MAA13 Differentiaali- ja integraalilaskennan jatkokurssi  1 hyvä
          |MAA1 Funktiot ja yhtälöt, pa, vuositaso 1 1 kiitettävä
          |MAA3 Geometria 1 hyvä
          |MAA9 Integraalilaskenta 1 kiitettävä
          |MAA8 Juuri- ja logaritmifunktiot  1 tyydyttävä
          |MAA14 Kertauskurssi, ksy, vuositaso 3 1 kiitettävä
          |MAA11 Lukuteoria ja todistaminen  1 hyvä
          |MAA2 Polynomifunktiot ja -yhtälöt  1 erinomainen
          |MAA10 Todennäköisyys ja tilastot  1 hyvä
          |MAA7 Trigonometriset funktiot  1 hyvä
          |MAA4 Vektorit 1 erinomainen
          |MU Musiikki 1 hyvä
          |MU1 Musiikki ja minä  1 hyvä
          |PS Psykologia 1 kiitettävä
          |PS1 Psyykkinen toiminta ja oppiminen  1 kiitettävä
          |TE Terveystieto 1 kiitettävä
          |TE1 Terveyden perusteet  1 hyvä
          |KT Uskonto tai elämänkatsomustieto, Evankelisluterilainen uskonto 3 hyvä
          |UE3 Maailman uskontoja ja uskonnollisia liikkeitä  1 hyvä
          |UE2 Maailmanlaajuinen kristinusko  1 tyydyttävä
          |UE1 Uskonto ilmiönä – kristinuskon, juutalaisuuden ja islamin jäljillä  1 hyvä
          |YH Yhteiskuntaoppi 2 hyvä
          |YH1 Suomalainen yhteiskunta  1 hyvä
          |YH2 Taloustieto 1 hyvä
          |AI Äidinkieli ja kirjallisuus 8 kiitettävä
          |ÄI2 Kieli, kulttuuri ja identiteetti 1 hyvä
          |ÄI3 Kirjallisuuden keinoja ja tulkintaa 1 hyvä
          |ÄI8 Kirjoittamistaitojen syventäminen 1 kiitettävä
          |ÄI9 Lukutaitojen syventäminen 1 kiitettävä
          |ÄI6 Nykykulttuuri ja kertomukset 1 kiitettävä
          |ÄI5 Teksti ja konteksti 1 kiitettävä
          |ÄI4 Tekstit ja vaikuttaminen 1 hyvä
          |ÄI1 Tekstit ja vuorovaikutus 1 hyvä""".stripMargin
      )
    }
  }
}