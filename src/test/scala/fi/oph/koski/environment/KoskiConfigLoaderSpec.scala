package fi.oph.koski.environment

import com.typesafe.config.{Config, ConfigException, ConfigFactory}
import fi.oph.koski.config.KoskiConfigLoader
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import scala.jdk.CollectionConverters._

class KoskiConfigLoaderSpec extends AnyFreeSpec with Matchers {
  private val localConf = ConfigFactory.parseFile(new File("src/main/conf/local.conf"))
  private val referenceConf = ConfigFactory.parseFile(new File("src/main/resources/reference.conf"))

  private def avaimet(config: Config): Set[String] = config.entrySet.asScala.map(_.getKey).toSet

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
      val config = KoskiConfigLoader.cloudConfig(ConfigFactory.parseString("""identification.url.fi = "https://esimerkki.fi""""), "OPHKoskiQA")
      config.getString("identification.url.sv") should be("https://esimerkki.fi")
    }

    "local.confin avaimia ei ole" in {
      val config = KoskiConfigLoader.cloudConfig(ConfigFactory.empty(), "OPHKoskiQA")
      avaimet(localConf).filter(config.hasPath) should be(Set("env"))
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

  "Paikallinen konfiguraatio" - {
    "local.conf ohittaa reference.confin" in {
      val config = KoskiConfigLoader.localConfig(
        application = ConfigFactory.empty(),
        local = ConfigFactory.parseString("sessionTimeoutMinutes = 5")
      )
      config.getInt("sessionTimeoutMinutes") should be(5)
    }

    "application.conf tai -Dconfig.resource ohittaa local.confin" in {
      val config = KoskiConfigLoader.localConfig(
        application = ConfigFactory.parseString("sessionTimeoutMinutes = 7"),
        local = ConfigFactory.parseString("sessionTimeoutMinutes = 5")
      )
      config.getInt("sessionTimeoutMinutes") should be(7)
    }

    "local.confin viittaus näkee application.confin arvon" in {
      val config = KoskiConfigLoader.localConfig(
        application = ConfigFactory.parseString("""db.host = "tietokanta""""),
        local = ConfigFactory.parseString(s"""db.host = "localhost"\nmuu.host = $${db.host}""")
      )
      config.getString("muu.host") should be("tietokanta")
    }

    "Oletuksena luetaan local.conf" in {
      KoskiConfigLoader.loadLocal().getString("db.host") should be("localhost")
    }

    "Tiedoston puuttuminen kaataa latauksen" in {
      intercept[ConfigException] {
        KoskiConfigLoader.loadLocal(new File("tiedostoa-ei-ole.conf"))
      }
    }

    "local.conf ei ole luokkapolulla, joten se ei päädy pakettiin" in {
      getClass.getResource("/local.conf") should be(null)
    }
  }

  "reference.conf" - {
    "toimii ilman local.confia" in {
      referenceConf.resolve().isResolved should be(true)
    }

    "ei määrittele local.confin avaimia" in {
      withClue("Pilvessäkin luettava avain kuuluu vain reference.confiin, paikallinen vain local.confiin: ") {
        avaimet(localConf).intersect(avaimet(referenceConf.resolve())) should be(empty)
      }
    }
  }
}
