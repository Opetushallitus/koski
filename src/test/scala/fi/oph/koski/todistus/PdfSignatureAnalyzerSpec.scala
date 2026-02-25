package fi.oph.koski.todistus

import fi.oph.koski.KoskiApplicationForTests
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import scala.util.{Failure, Success}

class PdfSignatureAnalyzerSpec extends AnyFreeSpec with Matchers {

  private val mockdataDir = new File("src/main/resources/mockdata/todistus")
  private val validationConfig = PdfSignatureAnalyzer.ValidationConfig.fromConfig(KoskiApplicationForTests.config)
    .copy(hashValidointi = true)

  private def testPdfFile(testNameWithFileName: String)(testBody: File => Unit): Unit = {
    testNameWithFileName in {
      // Parse tiedostonimi testin nimestä (oletetaan että tiedostonimi on ennen ensimmäistä ":")
      val fileName = testNameWithFileName.split(":")(0).trim
      val pdfFile = new File(mockdataDir, fileName)
      pdfFile should exist
      testBody(pdfFile)
    }
  }

  "PdfSignatureAnalyzer" - {
    "kaikki mockdata-tiedostot on testattu" in {
      val allMockDataPdfFileNames = mockdataDir.listFiles()
        .filter(_.getName.endsWith(".pdf"))
        .filter(_.getName.startsWith("mock-"))
        .map(_.getName)
        .toSet

      // Hae kaikki testin nimet jotka sisältävät tiedostonimen
      val allTestNames = testNames
      val testedFiles = allMockDataPdfFileNames.filter { fileName =>
        // Tarkista onko tiedostolle oma testi (testi jossa on tiedostonimi mukana)
        allTestNames.exists(_.contains(fileName))
      }

      val untestedFiles = allMockDataPdfFileNames -- testedFiles

      withClue(s"Seuraavilla tiedostoilla ei ole omaa testiä (testinimen pitää sisältää tiedostonimi): ${untestedFiles.mkString(", ")}. " +
        s"Lisää jokaiselle tiedostolle oma testi tai lisää ne johonkin yleiseen testiin. ") {
        untestedFiles should be(empty)
      }

      println(s"✓ Kaikki ${allMockDataPdfFileNames.size} PDF-tiedostoa on testattu")
    }


    testPdfFile("mock-todistus-raw.pdf: allekirjoittamaton pdf on epävalidi") { pdfFile =>
      PdfSignatureAnalyzer.analyzePdfFile(pdfFile, validationConfig) match {
        case Success(report) =>
          report should not be null
          report.overallValid should be(false)
          report.signatureContents.validationErrors should contain("PDF does not contain a signature")
          report.summary should include("PDF does not contain a digital signature")

        case Failure(exception) =>
          fail(s"Analyysi epäonnistui: ${exception.getMessage}", exception)
      }
    }

    testPdfFile("mock-todistus-stamped.pdf: allekirjoitettu PDF on validi") { pdfFile =>
      PdfSignatureAnalyzer.analyzePdfFile(pdfFile, validationConfig) match {
        case Success(report) =>
          verifyIsValid(report)

        case Failure(exception) =>
          fail(s"Analyysi epäonnistui: ${exception.getMessage}", exception)
      }
    }

    testPdfFile("mock-todistus-stamped-tampered-content.pdf: Kryptografinen tarkistus havaitsee muokatun sisällön") { pdfFile =>
      PdfSignatureAnalyzer.analyzePdfFile(pdfFile, validationConfig) match {
        case Success(report) =>
          report should not be null
          report.overallValid should be(false)
          report.pkcs7 should not be None
          report.pkcs7.get.signatureValid should be(Some(false))
          report.pkcs7.get.signatureValidationError should not be None
          report.pkcs7.get.signatureValidationError.get should include("Signature verification failed")
        case Failure(exception) =>
          fail(s"Analyysi epäonnistui: ${exception.getMessage}", exception)
      }
    }

    testPdfFile("mock-todistus-stamped-missing-revocation-lists.pdf: huomaa puuttuvat revokaatiolistat") { pdfFile =>
      PdfSignatureAnalyzer.analyzePdfFile(pdfFile, validationConfig) match {
        case Success(report) =>
          report should not be null
          report.dss.isValid should be(false)
          report.overallValid should be(false)

        case Failure(exception) =>
          fail(s"Analyysi epäonnistui: ${exception.getMessage}", exception)
      }
    }

    testPdfFile("mock-todistus-stamped-invalid-signature-missing-revocation-lists.pdf: huomaa sekä puuttuvat revokaatiolistat että kryptografisesti rikkoutuneen signaturen") { pdfFile =>
      PdfSignatureAnalyzer.analyzePdfFile(pdfFile, validationConfig) match {
        case Success(report) =>
          report should not be null
          report.dss.isValid should be(false)
          report.overallValid should be(false)

          report.signatureContents.isValid should be(true)

          // Verify that cryptographic signature verification also fails
          report.pkcs7 should not be None
          report.pkcs7.get.signatureValid should be(Some(false))
          report.pkcs7.get.signatureValidationError should not be None

        case Failure(exception) =>
          fail(s"Analyysi epäonnistui: ${exception.getMessage}", exception)
      }
    }

    "Analysoi kaikki mockdata-hakemistossa olevat PDF:t ja tulostaa niistä onnistuneesti raportin" in {
      val pdfFiles = mockdataDir.listFiles().filter(_.getName.endsWith(".pdf"))
      pdfFiles should not be empty
      println(s"\nAnalysoidaan ${pdfFiles.length} PDF-tiedostoa mockdata-hakemistosta:\n")
      pdfFiles.foreach { pdfFile =>
        println(s"  - ${pdfFile.getName}")
        PdfSignatureAnalyzer.analyzePdfFile(pdfFile, validationConfig) match {
          case Success(report) =>
            // Tarkista että ei null-arvoja
            report should not be null
            report.byteRange should not be null
            report.signatureContents should not be null
            report.dss should not be null
            report.summary should not be null
            report.summary should not be empty
            // Tulosta yhteenveto
            val status = if (report.overallValid) "✓ VALID" else "✗ INVALID"
            val sigStatus = if (report.pkcs7.isDefined) "SIGNED" else "UNSIGNED"
            println(s"      $status ($sigStatus)")
            report.allValidationErrors.foreach { e => println(s"        ${e}")}
            report.allValidationErrors.isEmpty should be(report.overallValid)

          case Failure(exception) =>
            fail(s"Analyysi epäonnistui tiedostolle ${pdfFile.getName}: ${exception.getMessage}", exception)
        }
      }
      println()
    }

  }

  private def verifyIsValid(report: PdfSignatureAnalyzer.AnalysisReport): Unit = {
    report should not be null
    report.overallValid should be(true)
    report.byteRange.signatureLength should be > 0
    report.signatureContents.isValid should be(true)
    report.pkcs7 should not be None
    report.pkcs7.get.signatureValid should be(Some(true))
    report.pkcs7.foreach { pkcs7 =>
      pkcs7.signerCount should be > 0
      pkcs7.certificates.size should be >= 2
      pkcs7.isValid should be(true)
    }
    report.dss.hasOCSPs || report.dss.hasCRLs should be(true)
    report.dss.hasVRI should be(true)
    report.summary should include("signature and structure appear valid")
  }
}
