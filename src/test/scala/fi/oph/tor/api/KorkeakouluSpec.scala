package fi.oph.tor.api

import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.oppija.MockOppijat
import fi.oph.tor.schema._
import org.scalatest.{FunSpec, Matchers}

import scala.collection.immutable.Seq
import scala.xml.Node

class KorkeakouluSpec extends FunSpec with Matchers with OpiskeluoikeusTestMethodsKorkeakoulu with SearchTestMethods {
  describe("Korkeakoulun opiskeluoikeudet") {
    describe("Lisättäessä/päivitettäessä") {
      it("palautetaan HTTP 501") {
        putOpiskeluOikeus(defaultOpiskeluoikeus) {
          verifyResponseStatus(501, TorErrorCategory.notImplemented.readOnly("Korkeakoulutuksen opiskeluoikeuksia ei voi päivittää Koski-järjestelmässä"))
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
            |751101 Dipl.ins., konetekniikka
            |Opintosuoritukset
            |751101 Dipl.ins., konetekniikka
            |K901-W Vapaasti valittavat opinnot (KON) 16 4 28.05.2015
            |Ene-39.4037 Laskennallisen virtausmekaniikan ja lämmönsiirron perusteet L 7 4 17.02.2015
            |Kul-24.4200 Introduction to Risk Analysis of Structure P 5 4 18.11.2013
            |Kon-41.3131 Mechatronics Exercises 4 4 28.05.2015
            |K901-D Diplomityö (KON) 30 4 21.03.2016
            |MEN.thes Diplomityö 30 4 21.03.2016
            |ENG.matr Kypsyysnäyte 0 hyväksytty 08.03.2016
            |IA3027 Mechanical Engineering 65 4 04.12.2015
            |K430-3 Digital Design and Manufacturing 20 4 20.05.2015
            |Kon-41.3006 Computer Aided Design Basic Course 5 3 28.04.2014
            |Kon-15.4101 Digital Manufacturing 4 4 28.12.2014
            |Kon-41.4207 CAE Project 3 4 20.05.2015
            |Kon-80.3125 Castings 4 4 21.12.2014
            |Kon-67.4208 Welding Methods and Production 4 3 23.06.2014
            |K420-3 Mechanics of Materials 25 5 19.02.2015
            |Kul-49.5100 Lujuusopin lisensiaattiseminaari L 5 3 08.07.2014
            |Kon-67.3401 Rakenneaineet jännitysten ja ympäristön vaikutusten alaisina 5 5 19.02.2015
            |Kul-49.4100 Elementtimenetelmä II L 5 5 16.12.2014
            |Kul-49.3400 Dynamics of Structures; lectures and exercises L 5 5 30.05.2014
            |Kul-49.4350 Rakenteiden väsyminen L 5 5 16.02.2015
            |K410-3 Product Development 20 5 04.12.2015
            |Kon-41.4001 Product Development P 5 5 04.12.2015
            |TU-91.2041 Research and Development (R&D) Management 5 4 05.06.2014
            |Kon-41.4002 Product Development Project P 10 5 22.05.2014
            |K901-M Tieteen metodiikan opinnot (KON) 12 3 05.09.2014
            |Kie-98.7009 Get to know Finland 1 hyväksytty 21.11.2013
            |Eri-0.5014 Searching for Scientific Information 2 hyväksytty 13.12.2013
            |Kie-98.7011 Suomi 1A 2 3 20.02.2014
            |T-61.5010 Information Visualization L 5 3 05.09.2014
            |Kie-98.7012 Suomi 1B 2 3 08.04.2014
            |Kon-67.4206 Design and Analysis of Welded Structures 3 4 23.06.2014
            |Ene-39.4031 Kitkallinen virtaus L 5 3 16.02.2014
            |Kie-98.4011 Ranska 1A 2 4 21.10.2014
            |Kul-49.3300 Finite Element Method I 5 4 15.04.2014
            |Kul-24.3710 Potential Flow Theory for Lifting Surfaces 3 3 31.05.2015
            |Kul-34.3600 Composite Structures 5 3 12.01.2015
            |Kul-34.4700 Lightweight Structures P 5 2 12.05.2015
            |Ene-39.4054 Virtaussimulointi L 6 3 31.08.2015""".stripMargin
        )
      }

      it("Opinto-oikeus, keskeneräinen tutkinto") {
        opintosuoritusote("090888-929X", "1.2.246.562.10.56753942459") should equal(
          """|Ensisijainen opinto-oikeus
            |Tavoitetutkinto Tekn. kand., kemian tekniikka
            |Voimassa 01.08.2008 - 31.07.2017
            |Suoritetut tutkinnot
            |Opintosuoritukset
            |KE-35.1210 Epäorgaanisen kemian laboratoriotyöt 4 hyväksytty 10.12.2009
            |T-106.1111 Johdatus opiskeluun ja tietojärjestelmiin 2 hyväksytty 26.10.2009
            |KE-35.1200 Epäorgaaninen kemia I 4 2 15.12.2009
            |Tfy-3.1241 Fysiikka IA 3 5 28.10.2009""".stripMargin
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
