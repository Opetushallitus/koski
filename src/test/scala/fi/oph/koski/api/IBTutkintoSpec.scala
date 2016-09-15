package fi.oph.koski.api

import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.schema.TäydellisetHenkilötiedot
import org.scalatest.{FunSpec, Matchers}

class IBTutkintoSpec extends FunSpec with Matchers with OpintosuoritusoteTestMethods with TodistusTestMethods with OpiskeluOikeusTestMethods with LocalJettyHttpSpecification {
  describe("IB-tutkinto") {
    it("Opintosuoritusote") {
      opintosuoritusote(MockOppijat.ibOpiskelija) should equal(
        """Opintosuoritukset
          |Kurssia Arvosana Suor.pvm
          |A1 A1-kieli, englanti 3
          |ENA1 Englannin kieli ja maailmani 1 10 4.6.2016
          |ENA2 Ihminen verkostoissa 1 10 4.6.2016
          |ENA5 Tiede ja tulevaisuus 1 10 4.6.2016
          |B1 B1-kieli, ruotsi 2
          |RUB12 Hyvinvointi ja ihmissuhteet 1 7 4.6.2016
          |RUB11 Minun ruotsini 1 8 4.6.2016
          |B2 B2-kieli, ranska 1
          |RAN3 Ravintolaranska 1 9 4.6.2016
          |B3 B3-kieli, espanja 1
          |ES1 Turistiespanja 1 hyväksytty 4.6.2016
          |BI Biologia 2
          |BI10 Biologian erikoiskurssi 1 hyväksytty 4.6.2016
          |BI1 Elämä ja evoluutio 1 8 4.6.2016
          |FI Filosofia 1
          |FI1 Johdatus filosofiseen ajatteluun 1 hyväksytty 4.6.2016
          |FY Fysiikka 1
          |FY1 Fysiikka luonnontieteenä 1 7 4.6.2016
          |HI Historia 3
          |HI10 Ajan lyhyt historia 1 hyväksytty 4.6.2016
          |HI4 Eurooppalaisen maailmankuvan kehitys 1 8 4.6.2016
          |HI3 Itsenäisen Suomen historia 1 9 4.6.2016
          |KE Kemia 1
          |KE1 Kemiaa kaikkialla 1 8 4.6.2016
          |KU Kuvataide 1
          |KU1 Kuvat ja kulttuurit 1 9 4.6.2016
          |LI Liikunta 1
          |LI1 Energiaa liikunnasta 1 8 4.6.2016
          |GE Maantieto 1
          |GE2 Sininen planeetta 1 10 4.6.2016
          |MA Matematiikka, pitkä oppimäärä 4
          |MAA12 Algoritmit matematiikassa 1 7 4.6.2016
          |MAA13 Differentiaali- ja integraalilaskennan jatkokurssi 1 7 4.6.2016
          |MAA11 Lukuteoria ja todistaminen 1 7 4.6.2016
          |MAA2 Polynomifunktiot ja -yhtälöt 1 7 4.6.2016
          |MU Musiikki 1
          |MU1 Musiikki ja minä 1 8 4.6.2016
          |OP Opinto-ohjaus 1
          |OP1 Minä opiskelijana 1 hyväksytty 4.6.2016
          |PS Psykologia 1
          |PS1 Psyykkinen toiminta ja oppiminen 1 8 4.6.2016
          |TE Terveystieto 1
          |TE1 Terveyden perusteet 1 7 4.6.2016
          |KT Uskonto tai elämänkatsomustieto 1
          |UK4 Uskonto suomalaisessa yhteiskunnassa 1 10 4.6.2016
          |YH Yhteiskuntaoppi 1
          |YH1 Suomalainen yhteiskunta 1 8 4.6.2016
          |AI Äidinkieli ja kirjallisuus 3
          |ÄI2 Kieli, kulttuuri ja identiteetti 1 8 4.6.2016
          |ÄI3 Kirjallisuuden keinoja ja tulkintaa 1 8 4.6.2016
          |ÄI1 Tekstit ja vuorovaikutus 1 8 4.6.2016""".stripMargin
      )
    }
  }

  def opintosuoritusote(henkilö: TäydellisetHenkilötiedot): String = {
    resetFixtures
    opintosuoritusoteOpiskeluoikeudelle(henkilö.oid, ibOpiskeluoikeus)
  }

  def ibOpiskeluoikeus = {
    getOpiskeluoikeus(MockOppijat.ibOpiskelija.oid, "ibtutkinto").id.get
  }
}