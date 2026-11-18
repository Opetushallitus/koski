package fi.oph.koski.ibis

import fi.oph.koski.TestEnvironment
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.nio.charset.StandardCharsets
import java.time.LocalDate
import scala.io.Source

class IbisCsvParserSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  private def resurssi(nimi: String): String = Source.fromInputStream(getClass.getResourceAsStream(s"/ibis/$nimi"), "UTF-8").mkString
  private lazy val esimerkki = resurssi("esimerkki.csv")

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
      rivi.sessionNumber should be("0001")
      rivi.personalCode should be("abc123")
      rivi.category should be("DIPLOMA")
      rivi.lastName should be("IB-Diploma")
      rivi.firstName should be("Dani")
      rivi.universityId should be("")
      rivi.lastChangeDate should be(Some(LocalDate.of(2016, 7, 6)))
      rivi.totalPoints should be(Some(37))
      rivi.resultCode should be("D")
      rivi.eeTokPoints should be(Some(2))
      rivi.session.koodiarvo should be("2016K")
    }

    "lukee toistuvat oppiainekentät" in {
      val oppiaineet = rivit.head.subjects
      oppiaineet should have length 8
      oppiaineet.head should be(IbisSubject(2016, "MAY", "FINNISH A: Literature", "HL", "7", additional = false))
      oppiaineet.map(_.level) should be(List("HL", "HL", "HL", "SL", "SL", "SL", "EE", "TK"))
    }

    "käsittelee lainausmerkeissä olevat pilkut ja kahdennetut lainausmerkit" in {
      val courseRivi = rivit(1)
      courseRivi.lastName should be("Kurssi, Kalle")
      courseRivi.firstName should be("Kalle \"Kale\"")
      courseRivi.universityId should be("1230456789")
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
      retakeRivi.subjects.head.session.koodiarvo should be("2015S")
      retakeRivi.subjects(1).grade should be("N")
    }

    "ohittaa otsikkorivin" in {
      val otsikolla = "YEAR,\"MONTH\",\"SCHOOL\",\"SESSION NUMBER\",\"PERSONAL CODE\"\n" + esimerkki
      val (virheet, rivit) = IbisCsvParser.parse(otsikolla)
      virheet should be(empty)
      rivit should have length 3
    }

    "raportoi virheelliset rivit rivinumerolla ja jäsentää loput" in {
      val rikki = esimerkki.replace("\"def456\"", "\"def456\",\"ylimääräinen\"")
      val (virheet, rivit) = IbisCsvParser.parse(rikki)
      rivit should have length 2
      virheet.map(_.line) should be(List(3))
      virheet.head.message should include("ei ole jaollinen luvulla 6")
    }

    "hylkää tuntemattoman tutkintokerran kuukauden" in {
      val (virheet, rivit) = IbisCsvParser.parse(esimerkki.replace("\"2015\",\"NOVEMBER\"", "\"2015\",\"JUNE\""))
      rivit should have length 2
      virheet.head.message should include("JUNE")
    }
  }

  "Excelissä tallennettu tiedosto, jonka jokainen rivi on yhtenä lainausmerkeissä olevana kenttänä" - {
    "jäsennetään samoin kuin alkuperäinen tiedosto" in {
      val kääritty = esimerkki.split("\n").map(rivi => "\"" + rivi.replace("\"", "\"\"") + "\"").mkString("\r\n")
      IbisCsvParser.parse(kääritty) should be(IbisCsvParser.parse(esimerkki))
    }
  }

  "Kevään 2026 tulostiedosto" - {
    val (virheet, rivit) = IbisCsvParser.parse(resurssi("ib_diploma_kevat_2026_example.csv"))

    "jäsennetään ilman virheitä" in {
      virheet should be(empty)
      rivit.map(_.personalCode) should be(List("abc123", "abc132", "cba987", "cab789"))
    }

    "säilyttää oppiaineiden nimet ja tutkintokerrat sellaisinaan" in {
      rivit.map(_.subjects.length) should be(List(8, 8, 8, 8))
      rivit(1).subjects.head should be(IbisSubject(2025, "MAY", "FINNISH A: Literature", "SL", "6", additional = false))
      rivit(1).subjects.map(_.name) should contain("MATHEMATICS ANALYSIS AND APPRO")
    }
  }

  "Merkistö" - {
    "ohittaa UTF-8:n BOM:n eikä keskeydy, vaikka nimessä on muun merkistön merkki" in {
      val bom = Array(0xEF, 0xBB, 0xBF).map(_.toByte)
      // ISO-8859-1 ja ISO-8859-3 koodaavat ä:n samalla tavulla, joka ei ole kelvollista UTF-8:aa.
      val rivi = esimerkki.linesIterator.next().replace("IB-Diploma", "Mäkinen").getBytes(StandardCharsets.ISO_8859_1)
      val (virheet, rivit) = IbisCsvParser.parse(bom ++ rivi)
      virheet should be(empty)
      rivit.map(_.personalCode) should be(List("abc123"))
      rivit.head.subjects should have length 8
    }
  }

  "Rivin jako kenttiin" in {
    IbisCsvParser.splitLine("\"a\",\"b,c\",\"d \"\"e\"\"\",,f") should be(List("a", "b,c", "d \"e\"", "", "f"))
  }
}
