package fi.oph.koski.browserstack

import java.net.URL
import java.util.Date

import fi.oph.koski.api.LocalJettyHttpSpecification
import fi.oph.koski.integrationtest.EnvVariables
import fi.oph.koski.jettylauncher.SharedJetty
import fi.oph.koski.json.JsonSerializer
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.remote.{DesiredCapabilities, RemoteWebDriver}
import org.scalatest.{FreeSpec, Tag}

/**
  * Runs our mocha UI tests remotely on BrowserStack. To run this you need to
  *
  * - Run the BrowserStackLocal executable with a valid browserstack key. See https://www.browserstack.com/local-testing
  * - Supply the BROWSERSTACK_USERNAME and BROWSERSTACK_AUTOMATE_KEY environment variables (or system properties) to the JVM
  */
class BrowserstackMochaTest extends FreeSpec with LocalJettyHttpSpecification with EnvVariables {
  lazy val USERNAME = requiredEnv("BROWSERSTACK_USERNAME")
  lazy val AUTOMATE_KEY = requiredEnv("BROWSERSTACK_AUTOMATE_KEY")
  lazy val URL: String = "https://" + USERNAME + ":" + AUTOMATE_KEY + "@hub-cloud.browserstack.com/wd/hub"

  "Mocha tests on BrowserStack" - {
    "Chrome" taggedAs(BrowserStack) in {
      runMochaTests(Chrome)
    }
    // Currently doesn't pass, needs investigation
    "IE11" taggedAs(BrowserStack) in {
      runMochaTests(IE11)
    }
    "Edge 16" taggedAs(BrowserStack) in {
      runMochaTests(Edge16)
    }
    "Firefox" taggedAs(BrowserStack) in {
      runMochaTests(Firefox)
    }
    "Safari" taggedAs(BrowserStack) in {
      runMochaTests(Safari)
    }
    // To generate browser capabilities for more browsers, see this page: https://www.browserstack.com/automate/java
  }

  private def runMochaTests(capabilities: BrowserCapabilities) = {
    SharedJetty.start
    val driver = new RemoteWebDriver(new URL(URL), capabilities.caps)
    driver.get(SharedJetty.baseUrl + "/test/runner.html?grep=Ammatillinen%20koulutus%20Opiskeluoikeuden%20lis%C3%A4%C3%A4minen%20Validointi%20Kun%20kutsumanimi%20l%C3%B6ytyy%20v%C3%A4liviivallisesta%20nimest%C3%A4") // <- add some grep params here if you want to run a subset
    verifyMochaStarted(driver)
    var stats = getMochaStats(driver)
    while (!stats.ended) {
      Thread.sleep(1000)
      stats = getMochaStats(driver)
    }
    println("Mocha tests ended")

    val errorLines = getMochaLog(driver).map(_.toOneLiner)
    if (errorLines.nonEmpty) {
      println("ERRORS: \n" + errorLines.mkString("\n"))
      fail("Mocha tests failed")
    }

    driver.quit()
  }

  private def verifyMochaStarted(driver: RemoteWebDriver) = {
    val started = driver.asInstanceOf[JavascriptExecutor].executeScript("return !!window.runner").asInstanceOf[Boolean]
    if (!started) {
      System.err.println(driver.getPageSource)
      fail("Mocha not loaded correctly")
    }
  }

  private def getMochaStats(driver: RemoteWebDriver) = {
    val scriptResult = driver.asInstanceOf[JavascriptExecutor].executeScript("return JSON.stringify(runner.stats)").asInstanceOf[String]
    val stats = JsonSerializer.parse[MochaStats](scriptResult)
    println(scriptResult)
    stats
  }

  private def getMochaLog(driver: RemoteWebDriver) = {
    val scriptResult = driver.asInstanceOf[JavascriptExecutor].executeScript("return JSON.stringify(runner.errors)").asInstanceOf[String]
    JsonSerializer.parse[List[MochaError]](scriptResult)
  }
}

sealed trait BrowserCapabilities {
  val caps = new DesiredCapabilities
  caps.setCapability("resolution", "1024x768")
  caps.setCapability("browserstack.local", "true")
  caps.setCapability("browserstack.debug", "true")
  //caps.setCapability("browserstack.networkLogs", "true")
  //caps.setCapability("browserstack.video", "true")
}

object Chrome extends BrowserCapabilities {
  caps.setCapability("browser", "Chrome")
  caps.setCapability("browser_version", "62.0")
  caps.setCapability("os", "Windows")
  caps.setCapability("os_version", "8")
}

object IE11 extends BrowserCapabilities {
  caps.setCapability("browser", "IE")
  caps.setCapability("browser_version", "11.0")
  caps.setCapability("os", "Windows")
  caps.setCapability("os_version", "10")
}

object Edge16 extends BrowserCapabilities {
  caps.setCapability("browser", "Edge")
  caps.setCapability("browser_version", "16.0")
  caps.setCapability("os", "Windows")
  caps.setCapability("os_version", "10")
}

object Firefox extends BrowserCapabilities {
  caps.setCapability("browser", "Firefox")
  caps.setCapability("browser_version", "57.0 beta")
  caps.setCapability("os", "Windows")
  caps.setCapability("os_version", "10")
}
object Safari extends BrowserCapabilities {
  caps.setCapability("browser", "Safari")
  caps.setCapability("browser_version", "11.0")
  caps.setCapability("os", "OS X")
  caps.setCapability("os_version", "High Sierra")
}
case class MochaStats(suites: Int, tests: Int, passes: Int, pending: Int, failures: Int, start: Date, end: Option[Date], duration: Option[Int]) {
  def ended = end.isDefined
}

case class MochaError(title: String, message: String, parent: Option[MochaContainer]) {
  def toOneLiner = parent.map(_.toOneLiner).getOrElse("") + title + " : " + message
}
case class MochaContainer(title: String, parent: Option[MochaContainer]) {
  def toOneLiner = title + " / "
}

object BrowserStack extends Tag("browserstack")