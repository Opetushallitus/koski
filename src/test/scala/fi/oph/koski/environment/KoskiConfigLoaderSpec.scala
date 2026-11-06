package fi.oph.koski.environment

import com.typesafe.config.ConfigFactory
import fi.oph.koski.config.KoskiConfigLoader
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class KoskiConfigLoaderSpec extends AnyFreeSpec with Matchers {
  "Pilviympäristön konfiguraatio" - {
    "AppConfigin arvo ohittaa reference.confin oletuksen" in {
      val config = KoskiConfigLoader.cloudConfig(ConfigFactory.parseString("sessionTimeoutMinutes = 5"), "OPHKoskiQA")
      config.getInt("sessionTimeoutMinutes") should be(5)
    }

    "AppConfigista puuttuva arvo tulee reference.confista" in {
      val config = KoskiConfigLoader.cloudConfig(ConfigFactory.empty(), "OPHKoskiQA")
      config.hasPath("jettyIdleTimeoutSeconds") should be(true)
    }

    "AppConfigin arvo näkyy reference.confin viittauksissa" in {
      val config = KoskiConfigLoader.cloudConfig(ConfigFactory.parseString("""koski.oppija.domain.fi = "esimerkki.fi""""), "OPHKoskiQA")
      config.getStringList("koski.cookieDomains") should contain only "esimerkki.fi"
    }

    "Ympäristön nimi tulee ENV_NAME-muuttujasta" - {
      "kun AppConfigissa on eri env" in {
        val config = KoskiConfigLoader.cloudConfig(ConfigFactory.parseString("""env = "local""""), "OPHKoskiProd")
        config.getString("env") should be("OPHKoskiProd")
      }

      "kun AppConfigissa ei ole enviä" in {
        val config = KoskiConfigLoader.cloudConfig(ConfigFactory.empty(), "OPHKoskiProd")
        config.getString("env") should be("OPHKoskiProd")
      }
    }
  }
}
