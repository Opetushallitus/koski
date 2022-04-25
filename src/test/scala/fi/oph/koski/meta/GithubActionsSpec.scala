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
      val branchYaml = Source.fromFile(".github/workflows/run_koski_tests_on_branches.yml").mkString
      val missing = testPackages.filter(
        !branchYaml.contains(_)
      ).toList
      withClue(s"Missing packages from tests: $missing") { missing.length should be (0) }
    }

    "Tarkistetaan, että tiedostossa test_build_deploy_master.yml on mainittu kaikki testipaketit" in {
      val branchYaml = Source.fromFile(".github/workflows/test_build_deploy_master.yml").mkString
      val missing = testPackages.filter(
        !branchYaml.contains(_)
      ).toList
      withClue(s"Missing packages from tests: $missing") { missing.length should be (0) }
    }
  }
}
