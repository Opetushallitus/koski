package fi.oph.koski.migration

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import scala.io.Source

class GithubActionsSpec extends AnyFreeSpec with Matchers {
  lazy val testPackages: Seq[String] = {
    def allSubdirs(dir: File): Seq[File] =
      Option(dir.listFiles).toSeq
        .flatten
        .filter(_.isDirectory)
        .flatMap(d => d +: allSubdirs(d))

    def containsAnyFile(dir: File): Boolean = {
      dir.listFiles.exists(_.isFile)
    }

    allSubdirs(new File("./src/test/scala/fi/oph/koski"))
      .filter(containsAnyFile)
      .map(_.getPath.stripPrefix("./src/test/scala/").replace(File.separatorChar, '.'))
      .filterNot(_.contains("fi.oph.koski.e2e")) // Playwright-testit
      .filterNot(_.contains("fi.oph.koski.omadataoauth2.e2e")) // Playwright-testit
      .filterNot(_.contains("fi.oph.koski.mocha")) // Koski frontend
      .filterNot(_.contains("fi.oph.koski.frontendvalpas")) // Valpas frontend
      .filterNot(_.contains("fi.oph.koski.inenvironmentlocalization")) // Lokalisaatiotestit ympäristöä vastaan, rikki
      .filterNot(_.contains("fi.oph.koski.integrationtest")) // Testit rikki, vaatii setuppia?
      .filterNot(_.contains("fi.oph.koski.util.tcp.tcp")) // TCP helpperit
  }

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
    withClue(s"Missing packages from tests: $missing") {
      missing.length should be(0)
    }
    val packagesWithoutTrailingComma = testPackages.filterNot(p =>
      source.contains(s"$p.") ||
        source.contains(s"$p,") ||
        source.contains(s"$p\n") ||
        source.contains(s"""$p"\n""")
    )
    withClue(s"Packages without trailing comma in $path: $packagesWithoutTrailingComma") {
      packagesWithoutTrailingComma.length should be(0)
    }
  }
}
