package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.schema.Henkilö
import org.scalatest.{FreeSpec, Matchers}

class IBTutkintoSpec extends FreeSpec with Matchers with OpintosuoritusoteTestMethods with TodistusTestMethods with OpiskeluoikeusTestMethods with LocalJettyHttpSpecification {
  "IB-tutkinto" - {
    "Opintosuoritusote" in {
      opintosuoritusote(MockOppijat.ibPredicted.oid) should equal(
        """Preliminary year courses
          |Kurssia Arvosana Suor.pvm
          |A1 A1-kieli, englanti 3 10
          |ENA1 Englannin kieli ja maailmani 1 10 4.6.2016
          |ENA2 Ihminen verkostoissa 1 10 4.6.2016
          |ENA5 Tiede ja tulevaisuus 1 10 4.6.2016
          |B1 B1-kieli, ruotsi 2 7
          |RUB11 Minun ruotsini 1 8 4.6.2016
          |RUB12 Hyvinvointi ja ihmissuhteet 1 7 4.6.2016
          |B2 B2-kieli, ranska 1 9
          |RAN3 Ravintolaranska 1 9 4.6.2016
          |B3 B3-kieli, espanja 1 6
          |ES1 Turistiespanja 1 hyväksytty 4.6.2016
          |BI Biologia 2 8
          |BI1 Elämä ja evoluutio 1 8 4.6.2016
          |BI10 Biologian erikoiskurssi 1 hyväksytty 4.6.2016
          |FI Filosofia 1 7
          |FI1 Johdatus filosofiseen ajatteluun 1 hyväksytty 4.6.2016
          |FY Fysiikka 1 7
          |FY1 Fysiikka luonnontieteenä 1 7 4.6.2016
          |HI Historia 3 8
          |HI3 Itsenäisen Suomen historia 1 9 4.6.2016
          |HI4 Eurooppalaisen maailmankuvan kehitys 1 8 4.6.2016
          |HI10 Ajan lyhyt historia 1 hyväksytty 4.6.2016
          |KE Kemia 1 8
          |KE1 Kemiaa kaikkialla 1 8 4.6.2016
          |KU Kuvataide 1 9
          |KU1 Kuvat ja kulttuurit 1 9 4.6.2016
          |LI Liikunta 1 8
          |LI1 Energiaa liikunnasta 1 8 4.6.2016
          |GE Maantieto 1 10
          |GE2 Sininen planeetta 1 10 4.6.2016
          |MA Matematiikka, pitkä oppimäärä 4 7
          |MAA2 Polynomifunktiot ja -yhtälöt 1 7 4.6.2016
          |MAA11 Lukuteoria ja todistaminen 1 7 4.6.2016
          |MAA12 Algoritmit matematiikassa 1 7 4.6.2016
          |MAA13 Differentiaali- ja integraalilaskennan jatkokurssi 1 7 4.6.2016
          |MU Musiikki 1 8
          |MU1 Musiikki ja minä 1 8 4.6.2016
          |OP Opinto-ohjaus 1 7
          |OP1 Minä opiskelijana 1 hyväksytty 4.6.2016
          |PS Psykologia 1 8
          |PS1 Psyykkinen toiminta ja oppiminen 1 8 4.6.2016
          |TO Teemaopinnot 1 hyväksytty
          |MTA Monitieteinen ajattelu 1 hyväksytty 8.6.2016
          |TE Terveystieto 1 7
          |TE1 Terveyden perusteet 1 7 4.6.2016
          |KT Uskonto/Elämänkatsomustieto 1 10
          |UK4 Uskonto suomalaisessa yhteiskunnassa 1 10 4.6.2016
          |YH Yhteiskuntaoppi 1 8
          |YH1 Suomalainen yhteiskunta 1 8 4.6.2016
          |AI Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus 3 8
          |ÄI1 Tekstit ja vuorovaikutus 1 8 4.6.2016
          |ÄI2 Kieli, kulttuuri ja identiteetti 1 8 4.6.2016
          |ÄI3 Kirjallisuuden keinoja ja tulkintaa 1 8 4.6.2016
          |International Baccalaureate Diploma Programme
          |Group 1
          |Arvosana Suor.pvm
          |A2 Language A: language and literature, englanti 0 7 4.6.2016
          |ENG_B_H1 ENG_B_H1 6A 4.6.2016
          |ENG_B_H2 ENG_B_H2 7 4.6.2016
          |ENG_B_H4 ENG_B_H4 pass 4.6.2016
          |ENG_B_H5 ENG_B_H5 6 4.6.2016
          |ENG_B_H6 ENG_B_H6 6 4.6.2016
          |ENG_B_H8 ENG_B_H8 5 4.6.2016
          |A Language A: literature, suomi 0 4 4.6.2016
          |FIN_S1 FIN_S1 4B 4.6.2016
          |FIN_S2 FIN_S2 4B 4.6.2016
          |FIN_S3 FIN_S3 pass 4.6.2016
          |FIN_S4 FIN_S4 5C 4.6.2016
          |FIN_S5 FIN_S5 6B 4.6.2016
          |FIN_S6 FIN_S6 5B 4.6.2016
          |FIN_S7 FIN_S7 5B 4.6.2016
          |FIN_S8 FIN_S8 pass 4.6.2016
          |FIN_S9 FIN_S9 5C 4.6.2016
          |Group 3
          |Arvosana Suor.pvm
          |HIS History 0 6 4.6.2016
          |HIS_H3 HIS_H3 6A 4.6.2016
          |HIS_H4 HIS_H4 6A 4.6.2016
          |HIS_H5 HIS_H5 7B 4.6.2016
          |HIS_H6 HIS_H6 6A 4.6.2016
          |HIS_H7 HIS_H7 1C 4.6.2016
          |HIS_H9 HIS_H9 pass 4.6.2016
          |PSY Psychology 0 7 4.6.2016
          |PSY_S1 PSY_S1 6A 4.6.2016
          |PSY_S2 PSY_S2 6B 4.6.2016
          |PSY_S3 PSY_S3 6B 4.6.2016
          |PSY_S4 PSY_S4 5B 4.6.2016
          |PSY_S5 PSY_S5 pass 4.6.2016
          |PSY_S6 PSY_S6 6B 4.6.2016
          |PSY_S7 PSY_S7 5B 4.6.2016
          |PSY_S8 PSY_S8 2C 4.6.2016
          |PSY_S9 PSY_S9 pass 4.6.2016
          |Group 4
          |Arvosana Suor.pvm
          |BIO Biology 0 5 4.6.2016
          |BIO_H1 BIO_H1 5B 4.6.2016
          |BIO_H2 BIO_H2 4B 4.6.2016
          |BIO_H3 BIO_H3 pass 4.6.2016
          |BIO_H4 BIO_H4 5B 4.6.2016
          |BIO_H5 BIO_H5 5B 4.6.2016
          |BIO_H6 BIO_H6 2B 4.6.2016
          |BIO_H7 BIO_H7 3C 4.6.2016
          |BIO_H8 BIO_H8 4C 4.6.2016
          |BIO_H9 BIO_H9 1C 4.6.2016
          |Group 5
          |Arvosana Suor.pvm
          |MATST Mathematical studies 0 5 4.6.2016
          |MATST_S1 MATST_S1 5A 4.6.2016
          |MATST_S2 MATST_S2 7A 4.6.2016
          |MATST_S3 MATST_S3 6A 4.6.2016
          |MATST_S4 MATST_S4 6A 4.6.2016
          |MATST_S5 MATST_S5 4B 4.6.2016
          |MATST_S6 MATST_S6 pass 4.6.2016
          |Others
          |Arvosana Suor.pvm
          |TOK Theory of knowledge 0 Excellent 4.6.2016
          |TOK1 TOK1 pass 4.6.2016
          |TOK2 TOK2 pass 4.6.2016""".stripMargin
      )
    }

    "Päättötodistus predicted grades" in {
      todistus(MockOppijat.ibPredicted.oid, "ibtutkinto") should equal(
        """International Baccalaureate
          |Predicted Grades
          |HELSINGIN KAUPUNKI
          |Ressun lukio
          |IB-predicted, Petteri 071096-317K
          |
          |Language A: literature, Finnish SL Satisfactory 4
          |Language A: language and literature, English HL Excellent 7
          |History HL Very good 6
          |Psychology SL Excellent 7
          |Biology HL Good 5
          |Mathematical studies SL Good 5
          |Subject: Language A: language and literature, English
          |Topic: How is the theme of racial injustice treated in Harper Lee's To Kill a Mockingbird and Solomon Northup's 12 Years a Slave""".stripMargin)
    }

    "Päättötodistus final grades" in {
      todistus(MockOppijat.ibFinal.oid, "ibtutkinto") should equal(
        """International Baccalaureate
          |Final Grades
          |HELSINGIN KAUPUNKI
          |Ressun lukio
          |IB-final, Iina 040701-432D
          |
          |Language A: literature, Finnish SL Satisfactory 4
          |Language A: language and literature, English HL Excellent 7
          |History HL Very good 6
          |Psychology SL Excellent 7
          |Biology HL Good 5
          |Mathematical studies SL Good 5
          |Subject: Language A: language and literature, English
          |Topic: How is the theme of racial injustice treated in Harper Lee's To Kill a Mockingbird and Solomon Northup's 12 Years a Slave""".stripMargin)
    }
  }

  private def opintosuoritusote(oppijaOid: Henkilö.Oid): String = {
    resetFixtures
    opintosuoritusoteOpiskeluoikeudelle(oppijaOid, ibOpiskeluoikeus)
  }

  private def ibOpiskeluoikeus = {
    getOpiskeluoikeus(MockOppijat.ibPredicted.oid, "ibtutkinto").oid.get
  }
}
