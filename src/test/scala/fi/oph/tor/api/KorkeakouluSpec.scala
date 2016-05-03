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

        val diploma: String = suoritukset.flatMap(print(_, 0)).mkString("\n").trim
        diploma should equal(
"""751101
  Design and Analysis of Welded Structures 3.0 op
  Diplomityö (KON) 30.0 op
    Diplomityö 30.0 op
    Kypsyysnäyte 0.0 op
  Mechanical Engineering 65.0 op
    Digital Design and Manufacturing 20.0 op
      Computer Aided Design Basic Course 5.0 op
      Digital Manufacturing 4.0 op
      CAE Project 3.0 op
      Castings 4.0 op
      Welding Methods and Production 4.0 op
    Mechanics of Materials 25.0 op
      Lujuusopin lisensiaattiseminaari L 5.0 op
      Rakenneaineet jännitysten ja ympäristön vaikutusten alaisina 5.0 op
      Elementtimenetelmä II L 5.0 op
      Dynamics of Structures; lectures and exercises L 5.0 op
      Rakenteiden väsyminen L 5.0 op
    Product Development 20.0 op
      Product Development P 5.0 op
      Research and Development (R&D) Management 5.0 op
      Product Development Project P 10.0 op
  Kitkallinen virtaus L 5.0 op
  Ranska 1A 2.0 op
  Finite Element Method I 5.0 op
  Tieteen metodiikan opinnot (KON) 12.0 op
    Get to know Finland 1.0 op
    Searching for Scientific Information 2.0 op
    Suomi 1A 2.0 op
    Information Visualization L 5.0 op
    Suomi 1B 2.0 op
  Potential Flow Theory for Lifting Surfaces 3.0 op
  Composite Structures 5.0 op
  Lightweight Structures P 5.0 op
  Vapaasti valittavat opinnot (KON) 16.0 op
    Laskennallisen virtausmekaniikan ja lämmönsiirron perusteet L 7.0 op
    Introduction to Risk Analysis of Structure P 5.0 op
    Mechatronics Exercises 4.0 op
  Virtaussimulointi L 6.0 op""")
      }
    }
  }

  def print(suoritus: Suoritus, indent: Int = 0): List[String] = {
    val description: String = suoritus match {
      case s: KorkeakouluTutkinnonSuoritus => s.koulutusmoduuli.tunniste.koodiarvo
      case s: KorkeakoulunOpintojaksonSuoritus => s.koulutusmoduuli.nimi.get("fi") + s.koulutusmoduuli.laajuus.map(l => " " + l.arvo + " op").getOrElse("")
    }
    val indented: String = (1 to indent).map(_ => " ").mkString("") + description
    val osat = suoritus.osasuoritusLista.flatMap(osasuoritus => print(osasuoritus, indent + 2))
    indented :: osat
  }
}
