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
  Design and Analysis of Welded Structures
  Diplomityö (KON)
    Diplomityö
    Kypsyysnäyte
  Mechanical Engineering
    Digital Design and Manufacturing
      Computer Aided Design Basic Course
      Digital Manufacturing
      CAE Project
      Castings
      Welding Methods and Production
    Mechanics of Materials
      Lujuusopin lisensiaattiseminaari L
      Rakenneaineet jännitysten ja ympäristön vaikutusten alaisina
      Elementtimenetelmä II L
      Dynamics of Structures; lectures and exercises L
      Rakenteiden väsyminen L
    Product Development
      Product Development P
      Research and Development (R&D) Management
      Product Development Project P
  Kitkallinen virtaus L
  Ranska 1A
  Finite Element Method I
  Tieteen metodiikan opinnot (KON)
    Get to know Finland
    Searching for Scientific Information
    Suomi 1A
    Information Visualization L
    Suomi 1B
  Potential Flow Theory for Lifting Surfaces
  Composite Structures
  Lightweight Structures P
  Vapaasti valittavat opinnot (KON)
    Laskennallisen virtausmekaniikan ja lämmönsiirron perusteet L
    Introduction to Risk Analysis of Structure P
    Mechatronics Exercises
  Virtaussimulointi L""")
      }
    }
  }

  def print(suoritus: Suoritus, indent: Int = 0): List[String] = {
    val description: String = suoritus match {
      case s: KorkeakouluTutkinnonSuoritus => s.koulutusmoduuli.tunniste.koodiarvo
      case s: KorkeakoulunOpintojaksonSuoritus => s.koulutusmoduuli.nimi.get("fi")
    }
    val indented: String = (1 to indent).map(_ => " ").mkString("") + description
    val osat = suoritus.osasuoritusLista.flatMap(osasuoritus => print(osasuoritus, indent + 2))
    indented :: osat
  }
}
