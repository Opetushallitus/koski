package fi.oph.koski.api

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.schema._
import org.scalatest.{FunSpec, Matchers}

import scala.collection.immutable.Seq
import scala.xml.Node

class KorkeakouluSpec extends FunSpec with Matchers with OpiskeluoikeusTestMethodsKorkeakoulu with SearchTestMethods {
  describe("Korkeakoulun opiskeluoikeudet") {
    describe("Lisättäessä/päivitettäessä") {
      it("palautetaan HTTP 501") {
        putOpiskeluOikeus(defaultOpiskeluoikeus) {
          verifyResponseStatus(501, KoskiErrorCategory.notImplemented.readOnly("Korkeakoulutuksen opiskeluoikeuksia ei voi päivittää Koski-järjestelmässä"))
        }
      }
    }

    describe("Haettaessa henkilötunnuksella") {
      describe("Jos henkilöä ei löydy henkilöpalvelusta") {
        it("Haetaan Virrasta ja luodaan henkilö") {
          searchForHenkilötiedot("090888-929X").map(_.kokonimi) should equal(List("Harri Koskinen"))
        }
        it("Seuraavalla haulla käytetään aiemmin luotua henkilöä") {
          searchForHenkilötiedot("090888-929X").map(_.oid) should equal(searchForHenkilötiedot("090888-929X").map(_.oid))
        }
      }
    }

    describe("Suoritusten tilat") {
      it("Keskeneräinen tutkinto") {
        opiskeluoikeudet(MockOppijat.korkeakoululainen.oid).flatMap(_.suoritukset).filter(_.koulutusmoduuli.isTutkinto).map(_.tila.koodiarvo) should equal(List("KESKEN"))
      }
      it("Valmis tutkinto") {
        opiskeluoikeudet(MockOppijat.dippainssi.oid).flatMap(_.suoritukset).filter(_.koulutusmoduuli.isTutkinto).map(_.tila.koodiarvo) should equal(List("VALMIS"))
      }
    }

    describe("Haettaessa") {
      it("Konvertoidaan Virta-järjestelmän opiskeluoikeus") {
        val oikeudet = opiskeluoikeudet(MockOppijat.dippainssi.oid)
        oikeudet.length should equal(2)

        oikeudet(0).tyyppi.koodiarvo should equal("korkeakoulutus")
        oikeudet(0).suoritukset.length should equal(1)
        oikeudet(0).asInstanceOf[KorkeakoulunOpiskeluoikeus].suoritukset.map(_.tila.koodiarvo) should equal(List("VALMIS"))

        oikeudet(1).tyyppi.koodiarvo should equal("korkeakoulutus")
        oikeudet(1).suoritukset.length should equal(8)
        oikeudet(1).asInstanceOf[KorkeakoulunOpiskeluoikeus].suoritukset.map(_.tila.koodiarvo) foreach {
          _ should equal("VALMIS")
        }
      }
    }

    describe("Opintosuoritusote") {
      it("Valmistunut diplomi-insinööri") {
        opintosuoritusote("290492-9455", "1.2.246.562.10.56753942459") should equal(
          """|Suoritetut tutkinnot
            |751101 Dipl.ins., konetekniikka  22.3.2016
            |Opintosuoritukset
            |751101 Dipl.ins., konetekniikka 123 OIV 22.3.2016
            |K901-D Diplomityö (KON) 30 4 21.3.2016
            |MEN.thes Diplomityö 30 4 21.3.2016
            |ENG.matr Kypsyysnäyte 0 hyväksytty 8.3.2016
            |IA3027 Mechanical Engineering 65 4 4.12.2015
            |K430-3 Digital Design and Manufacturing 20 4 20.5.2015
            |Kon-41.4207 CAE Project 3 4 20.5.2015
            |Kon-80.3125 Castings 4 4 21.12.2014
            |Kon-41.3006 Computer Aided Design Basic Course 5 3 28.4.2014
            |Kon-15.4101 Digital Manufacturing 4 4 28.12.2014
            |Kon-67.4208 Welding Methods and Production 4 3 23.6.2014
            |K420-3 Mechanics of Materials 25 5 19.2.2015
            |Kul-49.3400 Dynamics of Structures; lectures and exercises L 5 5 30.5.2014
            |Kul-49.4100 Elementtimenetelmä II L 5 5 16.12.2014
            |Kul-49.5100 Lujuusopin lisensiaattiseminaari L 5 3 8.7.2014
            |Kon-67.3401 Rakenneaineet jännitysten ja ympäristön vaikutusten alaisina 5 5 19.2.2015
            |Kul-49.4350 Rakenteiden väsyminen L 5 5 16.2.2015
            |K410-3 Product Development 20 5 4.12.2015
            |Kon-41.4001 Product Development P 5 5 4.12.2015
            |Kon-41.4002 Product Development Project P 10 5 22.5.2014
            |TU-91.2041 Research and Development (R&D) Management 5 4 5.6.2014
            |K901-M Tieteen metodiikan opinnot (KON) 12 3 5.9.2014
            |Kie-98.7009 Get to know Finland 1 hyväksytty 21.11.2013
            |T-61.5010 Information Visualization L 5 3 5.9.2014
            |Eri-0.5014 Searching for Scientific Information 2 hyväksytty 13.12.2013
            |Kie-98.7011 Suomi 1A 2 3 20.2.2014
            |Kie-98.7012 Suomi 1B 2 3 8.4.2014
            |K901-W Vapaasti valittavat opinnot (KON) 16 4 28.5.2015
            |Kul-24.4200 Introduction to Risk Analysis of Structure P 5 4 18.11.2013
            |Ene-39.4037 Laskennallisen virtausmekaniikan ja lämmönsiirron perusteet L 7 4 17.2.2015
            |Kon-41.3131 Mechatronics Exercises 4 4 28.5.2015
            |Kul-34.3600 Composite Structures 5 3 12.1.2015
            |Kon-67.4206 Design and Analysis of Welded Structures 3 4 23.6.2014
            |Kul-49.3300 Finite Element Method I 5 4 15.4.2014
            |Ene-39.4031 Kitkallinen virtaus L 5 3 16.2.2014
            |Kul-34.4700 Lightweight Structures P 5 2 12.5.2015
            |Kul-24.3710 Potential Flow Theory for Lifting Surfaces 3 3 31.5.2015
            |Kie-98.4011 Ranska 1A 2 4 21.10.2014
            |Ene-39.4054 Virtaussimulointi L 6 3 31.8.2015""".stripMargin
        )
      }

      it("Opinto-oikeus, keskeneräinen tutkinto") {
        opintosuoritusote("090888-929X", "1.2.246.562.10.56753942459") should equal(
          """|Ensisijainen opinto-oikeus
            |Tavoitetutkinto Tekn. kand., kemian tekniikka
            |Voimassa 1.8.2008 - 31.7.2017
            |Suoritetut tutkinnot
            |Opintosuoritukset
            |KE-35.1200 Epäorgaaninen kemia I 4 2 15.12.2009
            |KE-35.1210 Epäorgaanisen kemian laboratoriotyöt 4 hyväksytty 10.12.2009
            |Tfy-3.1241 Fysiikka IA 3 5 28.10.2009
            |T-106.1111 Johdatus opiskeluun ja tietojärjestelmiin 2 hyväksytty 26.10.2009""".stripMargin
        )
      }

      it("AMK, keskeyttänyt") {
        opintosuoritusote("100193-948U", "1.2.246.562.10.25619624254") should equal(
          """|Suoritetut tutkinnot
            |Opintosuoritukset
            |106000 Anatomi och fysiologi 5 1 15.11.2013
            |106000 Engelska, Akutvård 5 3 3.4.2014
            |106000 Hälsovård 5 4 13.9.2013
            |106000 Inhemska språk 1, finska för akutvårdare och fysioterapeuter, 2 sp 2 3 14.2.2014
            |106000 Inhemska språk 1, svenska för fysioterapeuter och akutvårdare, 3 sp 3 4 12.12.2013
            |106000 Inremedicinsk vård 9 hyväksytty 6.6.2014
            |106000 Introduktion till högskolestudier 5 hyväksytty 6.11.2013
            |106000 Kirurgisk vård 6 hyväksytty 6.6.2014
            |106000 Klinisk vård I 5 3 17.12.2013
            |106000 Klinisk vård II 5 1 20.3.2014
            |106000 Läkemedelsräkning, terminstentamen för AV, februari, 5 tal 0 hyväksytty 12.2.2014
            |106000 Medicinska ämnen I, Inremedicin 5 2 12.3.2014
            |106000 Patientsäkerhet och evidensbaserat vårdande 5 hyväksytty 12.2.2014""".stripMargin
        )
      }

      it("AMK, valmistunut") {
        opintosuoritusote("101291-954C", "1.2.246.562.10.25619624254") should equal(
          """|Suoritetut tutkinnot
            |671112 Fysioterapeutti (AMK)  29.5.2015
            |Opintosuoritukset
            |671112 Fysioterapeutti (AMK)  hyväksytty 29.5.2015
            |116000 Anatomi, fysiologi och biomekanik 5 3 11.11.2011
            |116000 Arbetslivsorienterade projekt 0,5 hyväksytty 27.5.2015
            |116000 Bedömningsmetoder i fysioterapi 5 5 15.5.2012
            |116000 Beteendevetenskap och rehabilitering 5 4 15.6.2012
            |116000 Breddstudier 3 hyväksytty 15.11.2014
            |116000 Engelska 5 5 7.3.2013
            |116000 Finska för akutvård och ergo- och fysioterapi 5 5 17.3.2012
            |116000 Fördjupad yrkespraktik 10 hyväksytty 27.5.2015
            |116000 Förebyggandet av CRPS inom fysioterapi - En systematisk litteraturstudie 5 hyväksytty 9.6.2014
            |116000 Förebyggandet av CRPS inom fysioterapi - En systematisk litteraturstudie 10 3 19.5.2015
            |116000 Grundfrågor inom fysioterapiforskning 5 hyväksytty 10.12.2014
            |116000 Grundkurs i fysioterapi 5 3 9.12.2011
            |116000 Hälsofrämjande strategier i fysioterapi 5 4 26.6.2012
            |116000 Idrottsmedicin 7,5 hyväksytty 15.11.2014
            |116000 Idrottsvetenskap 5 4 19.4.2012
            |116000 Inriktad fysioterapi I - sjukhus 10 4 20.12.2012
            |116000 Inriktad fysioterapi II 10 5 19.4.2013
            |116000 Inriktad fysioterapi III 10 4 23.10.2013
            |116000 Introduktion till högskolestudier 5 hyväksytty 28.10.2011
            |116000 Kunskapsutveckling inom fysioterapi 5 4 14.6.2013
            |116000 Ländryggsbesvär - undersökning och behandling 7,5 hyväksytty 15.11.2014
            |116000 Medicinska ämnen I, Inremedicin 5 3 5.4.2012
            |116000 Medicinska ämnen II (psykiatri, neurologi och geriatrik) 5 4 25.10.2012
            |116000 Metoder i fysioterapi 5 4 16.11.2012
            |116000 Motorisk utveckling, kontroll och inlärning 5 5 20.12.2011
            |116000 Organisation och Ledarskap 5 4 26.10.2012
            |116000 Orientation to Studies at the Faculty of Health Sciences 1,5 hyväksytty 15.11.2014
            |116000 Practical Training, Hospital 10 hyväksytty 3.6.2013
            |116000 Rörelseapparatens anatomi 5 5 16.12.2011
            |116000 Svenska 5 5 2.12.2011
            |116000 Tillämpad fysioterapeutisk bedömning 5 5 16.1.2013
            |116000 Vetenskapsteori och metodik 10 5 13.12.2013
            |116000 Yrkespraktik II - HVC 10 hyväksytty 20.12.2013
            |116000 Yrkespraktik III, neurologisk fysioterapi 10 hyväksytty 8.4.2014""".stripMargin
        )
      }

      it("Tutkintoon johtamaton opiskeluoikeus") {
        opintosuoritusote("090888-929X", "1.2.246.562.10.27756776996") should equal(
          """|Suoritetut tutkinnot
            |Opintosuoritukset
            |05AVOIN/MI00AX91/3 Graafisen suunnittelun perusteet 5 hyväksytty 7.11.2015
            |05AVOIN/MI00BB13/3 Typografian perusteet 4 hyväksytty 11.4.2016""".stripMargin
        )
      }
    }
  }

  private def opintosuoritusote(searchTerm: String, oppilaitosOid: String) = {
    searchForHenkilötiedot(searchTerm).map(_.oid).map { oppijaOid =>
      authGet(s"opintosuoritusote/${oppijaOid}/${oppilaitosOid}") {
        verifyResponseStatus(200)

        val lines: Seq[String] = scala.xml.XML.loadString(response.body).flatMap(_.descendant_or_self).flatMap { case tr: Node if tr.label == "tr" && (tr \ "@class").text != "header" => Some((tr \ "td").map(_.text).mkString(" ").trim)
          case h3: Node if h3.label == "h3" => Some(h3.text.trim)
          case _ => None
        }
        lines.mkString("\n").trim
      }
    }.headOption.getOrElse("")
  }
}
