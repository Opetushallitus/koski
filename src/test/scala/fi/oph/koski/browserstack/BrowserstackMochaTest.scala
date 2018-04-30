package fi.oph.koski.browserstack

import java.net.URL
import java.util.Date

import fi.oph.koski.api.LocalJettyHttpSpecification
import fi.oph.koski.integrationtest.EnvVariables
import fi.oph.koski.json.JsonSerializer
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.remote.{DesiredCapabilities, RemoteWebDriver}
import org.scalatest.concurrent.{Signaler, TimeLimits}
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{FreeSpec, Tag}

class ChromeTest extends BrowserstackMochaTest {
  object Chrome extends BrowserCapabilities {
    caps.setCapability("browser", "Chrome")
    caps.setCapability("browser_version", "62.0")
    caps.setCapability("os", "Windows")
    caps.setCapability("os_version", "8")
  }
  runMochaTests(Chrome)
}

class FirefoxTest extends BrowserstackMochaTest {
  object Firefox extends BrowserCapabilities {
    caps.setCapability("browser", "Firefox")
    caps.setCapability("browser_version", "57.0")
    caps.setCapability("os", "Windows")
    caps.setCapability("os_version", "10")
  }
  runMochaTests(Firefox)
}

class IE11Test extends BrowserstackMochaTest {
  object IE11 extends BrowserCapabilities {
    caps.setCapability("browser", "IE")
    caps.setCapability("browser_version", "11.0")
    caps.setCapability("os", "Windows")
    caps.setCapability("os_version", "10")
  }

  runMochaTests(IE11) // Some tests pass some fail
}

class Edge16Test extends BrowserstackMochaTest {
  object Edge16 extends BrowserCapabilities {
    caps.setCapability("browser", "Edge")
    caps.setCapability("browser_version", "16.0")
    caps.setCapability("os", "Windows")
    caps.setCapability("os_version", "10")
  }
  runMochaTests(Edge16)
}

class SafariTest extends BrowserstackMochaTest {
  object Safari extends BrowserCapabilities {
    caps.setCapability("browser", "Safari")
    caps.setCapability("browser_version", "10.0")
    caps.setCapability("os", "OS X")
    caps.setCapability("os_version", "Sierra")
  }
  runMochaTests(Safari)
}

/**
  * Runs our mocha UI tests remotely on BrowserStack. To run this you need to
  *
  * - Run the BrowserStackLocal executable with a valid browserstack key. See https://www.browserstack.com/local-testing
  * - Supply the BROWSERSTACK_USERNAME and BROWSERSTACK_AUTOMATE_KEY environment variables (or system properties) to the JVM
  *
  * To add more browsers, see https://www.browserstack.com/automate/junit
  */
abstract class BrowserstackMochaTest extends FreeSpec with LocalJettyHttpSpecification with EnvVariables with TimeLimits {
  lazy val USERNAME = requiredEnv("BROWSERSTACK_USERNAME")
  lazy val AUTOMATE_KEY = requiredEnv("BROWSERSTACK_AUTOMATE_KEY")
  lazy val URL: String = "https://" + USERNAME + ":" + AUTOMATE_KEY + "@hub-cloud.browserstack.com/wd/hub"

  def runMochaTests(capabilities: BrowserCapabilities) = {
    s"Mocha tests on BrowserStack ${this.getClass.getSimpleName}" taggedAs(BrowserStack) in {
      val driver = new RemoteWebDriver(new URL(URL), capabilities.caps)
      driver.get(baseUrl + "/test/runner.html?grep=BrowserStack") // <- add some grep params here if you want to run a subset
      verifyMochaStarted(driver)
      var stats = getMochaStats(driver)
      @volatile var timedOut = false
      val signaler = new Signaler { override def apply(testThread: Thread): Unit = { timedOut = true } }
      failAfter(Span(120, Seconds)) {
        while (!stats.ended && !timedOut) {
          Thread.sleep(1000)
          stats = getMochaStats(driver)
        }
      }(signaler)
      println("Mocha tests ended")

      val errorLines = getMochaLog(driver).map(_.toOneLiner)
      if (errorLines.nonEmpty) {
        fail("Mocha tests failed:\n" + errorLines.mkString("\n"))
      }

      driver.quit()
    }
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
  caps.setCapability("resolution", "1920x1080")
  caps.setCapability("browserstack.local", "true")
  caps.setCapability("browserstack.debug", "true")
  caps.setCapability("browserstack.console", "verbose")
  //caps.setCapability("browserstack.networkLogs", "true")
  //caps.setCapability("browserstack.video", "true")
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
