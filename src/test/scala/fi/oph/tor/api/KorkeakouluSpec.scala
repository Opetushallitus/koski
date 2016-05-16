package fi.oph.tor.api

import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.oppija.MockOppijat
import fi.oph.tor.schema._
import org.scalatest.{FunSpec, Matchers}

import scala.collection.immutable.Seq

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
      it("näytetään") {
        val get1 = authGet("opintosuoritusote/1.2.246.562.24.000000000011/1.2.246.562.10.56753942459") {
          verifyResponseStatus(200)

          val tableRows: Seq[String] = (scala.xml.XML.loadString(response.body) \\ "tr").map { s =>
            (s \ "td").map(_.text).mkString(" ").trim
          }
          val lines: Seq[String] = tableRows
          val ote: String = lines.mkString("\n")

          ote.trim should equal(
            """|751101 Dipl.ins., konetekniikka
              |
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
      }

    }
  }

  def print(suoritus: Suoritus, indent: Int = 0): List[String] = {
    def laajuus(s: KorkeakoulunOpintojaksonSuoritus): String = {
      s.koulutusmoduuli.laajuus.map(" laajuus: " + _.arvo + " op").getOrElse("")
    }

    def arvosana(s: KorkeakoulunOpintojaksonSuoritus): String = {
      s.arviointi.toList.flatten.lastOption.map(" arvosana: " + _.arvosana.koodiarvo).getOrElse("")
    }
    def pvm(s: KorkeakoulunOpintojaksonSuoritus): String = {
      s.arviointi.toList.flatten.lastOption.flatMap(_.päivä.map(" pvm: " + _)).getOrElse("")
    }

    val description: String = suoritus match {
      case s: KorkeakouluTutkinnonSuoritus => s.koulutusmoduuli.tunniste.koodiarvo
      case s: KorkeakoulunOpintojaksonSuoritus => s.koulutusmoduuli.nimi.get("fi") + laajuus(s) + arvosana(s) + pvm(s)
    }
    val indented: String = (1 to indent).map(_ => " ").mkString("") + description
    val osat = suoritus.osasuoritusLista.flatMap(osasuoritus => print(osasuoritus, indent + 2))
    indented :: osat
  }
}
