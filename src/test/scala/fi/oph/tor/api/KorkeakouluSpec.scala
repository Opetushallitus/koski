package fi.oph.tor.api

import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.oppija.MockOppijat
import fi.oph.tor.schema.{KorkeakoulunOpintojaksonSuoritus, KorkeakouluTutkinnonSuoritus, KorkeakoulunOpiskeluoikeus, Suoritus}
import org.scalatest.{FunSpec, Matchers}

class KorkeakouluSpec extends FunSpec with Matchers with OpiskeluoikeusTestMethodsKorkeakoulu {
  describe("Korkeakoulun opiskeluoikeudet") {
    describe("Lisättäessä/päivitettäessä") {
      it("palautetaan HTTP 501") {
        putOpiskeluOikeus(defaultOpiskeluoikeus) {
          verifyResponseStatus(501, TorErrorCategory.notImplemented.readOnly("Korkeakoulutuksen opiskeluoikeuksia ei voi päivittää Koski-järjestelmässä"))
        }
      }
    }

    describe("Haettaessa") {
      it("Konvertoidaan Virta-järjestelmän opiskeluoikeus") {
        val opiskeluoikeus = lastOpiskeluOikeus(MockOppijat.korkeakoululainen.oid)

        opiskeluoikeus.tyyppi.koodiarvo should equal("korkeakoulutus")

        opiskeluoikeus.suoritukset.length should equal(1)

        val suoritukset: List[KorkeakouluTutkinnonSuoritus] = opiskeluoikeus.asInstanceOf[KorkeakoulunOpiskeluoikeus].suoritukset

        suoritukset.map(_.tila.koodiarvo) should equal(List("VALMIS"))

        val diploma: String = suoritukset.flatMap(print(_, 0)).mkString("\n").trim
        diploma should equal(
"""751101
  Design and Analysis of Welded Structures laajuus: 3.0 op arvosana: 4 pvm: 2014-06-23
  Diplomityö (KON) laajuus: 30.0 op arvosana: 4 pvm: 2016-03-21
    Diplomityö laajuus: 30.0 op arvosana: 4 pvm: 2016-03-21
    Kypsyysnäyte laajuus: 0.0 op arvosana: HYV pvm: 2016-03-08
  Mechanical Engineering laajuus: 65.0 op arvosana: 4 pvm: 2015-12-04
    Digital Design and Manufacturing laajuus: 20.0 op arvosana: 4 pvm: 2015-05-20
      Computer Aided Design Basic Course laajuus: 5.0 op arvosana: 3 pvm: 2014-04-28
      Digital Manufacturing laajuus: 4.0 op arvosana: 4 pvm: 2014-12-28
      CAE Project laajuus: 3.0 op arvosana: 4 pvm: 2015-05-20
      Castings laajuus: 4.0 op arvosana: 4 pvm: 2014-12-21
      Welding Methods and Production laajuus: 4.0 op arvosana: 3 pvm: 2014-06-23
    Mechanics of Materials laajuus: 25.0 op arvosana: 5 pvm: 2015-02-19
      Lujuusopin lisensiaattiseminaari L laajuus: 5.0 op arvosana: 3 pvm: 2014-07-08
      Rakenneaineet jännitysten ja ympäristön vaikutusten alaisina laajuus: 5.0 op arvosana: 5 pvm: 2015-02-19
      Elementtimenetelmä II L laajuus: 5.0 op arvosana: 5 pvm: 2014-12-16
      Dynamics of Structures; lectures and exercises L laajuus: 5.0 op arvosana: 5 pvm: 2014-05-30
      Rakenteiden väsyminen L laajuus: 5.0 op arvosana: 5 pvm: 2015-02-16
    Product Development laajuus: 20.0 op arvosana: 5 pvm: 2015-12-04
      Product Development P laajuus: 5.0 op arvosana: 5 pvm: 2015-12-04
      Research and Development (R&D) Management laajuus: 5.0 op arvosana: 4 pvm: 2014-06-05
      Product Development Project P laajuus: 10.0 op arvosana: 5 pvm: 2014-05-22
  Kitkallinen virtaus L laajuus: 5.0 op arvosana: 3 pvm: 2014-02-16
  Ranska 1A laajuus: 2.0 op arvosana: 4 pvm: 2014-10-21
  Finite Element Method I laajuus: 5.0 op arvosana: 4 pvm: 2014-04-15
  Tieteen metodiikan opinnot (KON) laajuus: 12.0 op arvosana: 3 pvm: 2014-09-05
    Get to know Finland laajuus: 1.0 op arvosana: HYV pvm: 2013-11-21
    Searching for Scientific Information laajuus: 2.0 op arvosana: HYV pvm: 2013-12-13
    Suomi 1A laajuus: 2.0 op arvosana: 3 pvm: 2014-02-20
    Information Visualization L laajuus: 5.0 op arvosana: 3 pvm: 2014-09-05
    Suomi 1B laajuus: 2.0 op arvosana: 3 pvm: 2014-04-08
  Potential Flow Theory for Lifting Surfaces laajuus: 3.0 op arvosana: 3 pvm: 2015-05-31
  Composite Structures laajuus: 5.0 op arvosana: 3 pvm: 2015-01-12
  Lightweight Structures P laajuus: 5.0 op arvosana: 2 pvm: 2015-05-12
  Vapaasti valittavat opinnot (KON) laajuus: 16.0 op arvosana: 4 pvm: 2015-05-28
    Laskennallisen virtausmekaniikan ja lämmönsiirron perusteet L laajuus: 7.0 op arvosana: 4 pvm: 2015-02-17
    Introduction to Risk Analysis of Structure P laajuus: 5.0 op arvosana: 4 pvm: 2013-11-18
    Mechatronics Exercises laajuus: 4.0 op arvosana: 4 pvm: 2015-05-28
  Virtaussimulointi L laajuus: 6.0 op arvosana: 3 pvm: 2015-08-31""")
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
