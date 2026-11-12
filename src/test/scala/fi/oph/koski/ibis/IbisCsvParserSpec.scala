package fi.oph.koski.ibis

import fi.oph.koski.TestEnvironment
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import scala.io.Source

class IbisCsvParserSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  private lazy val esimerkki = Source.fromInputStream(getClass.getResourceAsStream("/ibis/esimerkki.csv"), "UTF-8").mkString

  "IBIS-tulostiedoston jäsennys" - {
    val (virheet, rivit) = IbisCsvParser.parse(esimerkki)

    "jäsentää kaikki rivit ilman virheitä" in {
      virheet should be(empty)
      rivit should have length 3
    }

    "lukee kiinteät kentät" in {
      val rivi = rivit.head
      rivi.year should be(2016)
      rivi.month should be("MAY")
      rivi.school should be("000001")
      rivi.personalCode should be("ABC123")
      rivi.dateOfBirth should be(Some(LocalDate.of(2001, 7, 4)))
      rivi.category should be("DIPLOMA")
      rivi.lastName should be("IB-Diploma")
      rivi.firstName should be("Dani")
      rivi.lastChangeDate should be(Some(LocalDate.of(2016, 7, 6)))
      rivi.totalPoints should be(Some(37))
      rivi.resultCode should be("D")
      rivi.eeTokPoints should be(Some(2))
      rivi.session.koodiarvo should be("2016M")
    }

    "lukee toistuvat oppiainekentät" in {
      val oppiaineet = rivit.head.subjects
      oppiaineet should have length 8
      oppiaineet.head should be(IbisSubject(2016, "MAY", "100001", "HL", "7", additional = false, "FINNISH"))
      oppiaineet.filter(_.isCore).map(_.level) should be(List("EE", "TK"))
    }

    "käsittelee lainausmerkeissä olevat pilkut ja kahdennetut lainausmerkit" in {
      val courseRivi = rivit(1)
      courseRivi.lastName should be("Kurssi, Kalle")
      courseRivi.firstName should be("Kalle \"Kale\"")
      courseRivi.courseRef should be("BIOLOGY 00104")
      courseRivi.totalPoints should be(None)
      courseRivi.resultCode should be("")
      courseRivi.eeTokPoints should be(None)
    }

    "lukee additional subjectin ja aiemman tutkintokerran" in {
      val retakeRivi = rivit(2)
      retakeRivi.category should be("RETAKE")
      retakeRivi.resultCode should be("B")
      retakeRivi.subjects.head.additional should be(true)
      retakeRivi.subjects.head.session should be(IbisSession(2015, "NOVEMBER"))
      retakeRivi.subjects.head.session.koodiarvo should be("2015N")
      retakeRivi.subjects(1).grade should be("N")
    }

    "ohittaa otsikkorivin" in {
      val otsikolla = "\"YEAR\",\"MONTH\",\"SCHOOL\",\"PERSONAL CODE\"\n" + esimerkki
      val (virheet, rivit) = IbisCsvParser.parse(otsikolla)
      virheet should be(empty)
      rivit should have length 3
    }

    "raportoi virheelliset rivit rivinumerolla ja jäsentää loput" in {
      val rikki = esimerkki.replace("\"DEF456\"", "\"DEF456\",\"ylimääräinen\"")
      val (virheet, rivit) = IbisCsvParser.parse(rikki)
      rivit should have length 2
      virheet.map(_.line) should be(List(3))
      virheet.head.message should include("ei ole jaollinen")
    }

    "hylkää tuntemattoman tutkintokerran kuukauden" in {
      val (virheet, rivit) = IbisCsvParser.parse(esimerkki.replace("\"2015\",\"NOVEMBER\"", "\"2015\",\"JUNE\""))
      rivit should have length 2
      virheet.head.message should include("JUNE")
    }
  }

  "Rivin jako kenttiin" in {
    IbisCsvParser.splitLine("\"a\",\"b,c\",\"d \"\"e\"\"\",,f") should be(List("a", "b,c", "d \"e\"", "", "f"))
  }
}
