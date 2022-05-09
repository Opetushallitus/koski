package fi.oph.koski.migration

import org.json4s.jackson.JsonMethods
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import scala.io.Source

class GithubActionsSpec extends AnyFreeSpec with Matchers {
  lazy val testPackages =
    new File("./src/test/scala/fi/oph/koski/").listFiles.filter(_.isDirectory).map("fi.oph.koski." + _.getName)

  "Github Actions" - {
    "Tarkistetaan, että tiedostossa run_koski_tests_on_branches.yml on mainittu kaikki testipaketit" in {
      checkTestIntegrity(".github/workflows/run_koski_tests_on_branches.yml")
    }

    "Tarkistetaan, että tiedostossa test_build_deploy_master.yml on mainittu kaikki testipaketit" in {
      checkTestIntegrity(".github/workflows/test_build_deploy_master.yml")
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
