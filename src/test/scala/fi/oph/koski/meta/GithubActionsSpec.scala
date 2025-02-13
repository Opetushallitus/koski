package fi.oph.koski.migration

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import scala.io.Source

class GithubActionsSpec extends AnyFreeSpec with Matchers {
  lazy val testPackages =
    new File("./src/test/scala/fi/oph/koski/").listFiles.filter(_.isDirectory).map("fi.oph.koski." + _.getName)
      .filterNot(_.contains("fi.oph.koski.e2e")) // Playwright-testit
      .filterNot(_.contains("fi.oph.koski.omadataoauth2.e2e")) // Playwright-testit
      .filterNot(_.contains("fi.oph.koski.mocha")) // Koski frontend
      .filterNot(_.contains("fi.oph.koski.frontendvalpas")) // Valpas frontend
      .filterNot(_.contains("fi.oph.koski.inenvironmentlocalization")) // Lokalisaatiotestit ympäristöä vastaan, rikki
      .filterNot(_.contains("fi.oph.koski.integrationtest")) // Testit rikki, vaatii setuppia?

  "Github Actions" - {
    "Tarkistetaan, että tiedostossa all_tests.yml on mainittu kaikki testipaketit" in {
      checkTestIntegrity(".github/workflows/all_tests.yml")
    }
  }

  "Local" - {
    "Tarkistetaan, että tiedostossa Makefile on mainittu kaikki testipaketit" in {
      checkTestIntegrity("Makefile")
    }
  }

  private def checkTestIntegrity(path: String) = {
    val sourceFile = Source.fromFile(path)
    val source = sourceFile.mkString
    sourceFile.close()
    val missing = testPackages.filter(!source.contains(_)).toList
    withClue(s"Missing packages from tests: $missing") { missing.length should be (0) }
  }
}
