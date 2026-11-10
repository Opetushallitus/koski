package fi.oph.koski.environment

import com.typesafe.config.{Config, ConfigException, ConfigFactory}
import fi.oph.koski.config.Environment
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class EnvironmentSpec extends AnyFreeSpec with Matchers {
  private def configWithEnv(env: String): Config = ConfigFactory.parseString(s"""env = "$env"""")

  "isProdEnvironment" - {
    "Tuotanto tunnistetaan" in {
      Environment.isProdEnvironment(configWithEnv(Environment.Prod)) should be(true)
    }

    "Tuntematon ympäristö tulkitaan tuotannoksi" in {
      Environment.isProdEnvironment(configWithEnv("OPHKoskiQa")) should be(true)
    }

    "Muut tunnetut ympäristöt eivät ole tuotantoa" in {
      Seq(Environment.Local, Environment.UnitTest, Environment.Dev, Environment.QA).foreach { env =>
        Environment.isProdEnvironment(configWithEnv(env)) should be(false)
      }
    }

    "Virkailija-URL ei vaikuta tulkintaan" in {
      val config = configWithEnv(Environment.QA)
        .withFallback(ConfigFactory.parseString("""opintopolku.virkailija.url = "https://virkailija.opintopolku.fi""""))
      Environment.isProdEnvironment(config) should be(false)
    }

    "Puuttuva env aiheuttaa virheen" in {
      intercept[ConfigException.Missing] {
        Environment.isProdEnvironment(ConfigFactory.empty())
      }
    }
  }
}
