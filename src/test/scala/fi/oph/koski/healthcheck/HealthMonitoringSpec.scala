package fi.oph.koski.healthcheck

import fi.oph.koski.healthcheck.Subsystem.{Oppijanumerorekisteri, Virta}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class HealthMonitoringSpec extends AnyFreeSpec with Matchers {
  private val ThresholdMillis = 15 * 60 * 1000L
  private val Minute = 60 * 1000L

  // Ohjattava kello ja Virta-katkosten hälytysten laskuri ilman lokitusriippuvuutta.
  private class Fixture {
    private var clock: Long = 0L
    private var alerts: Int = 0
    val monitoring: HealthMonitoring = new HealthMonitoring(() => clock, virtaOutageAlertingEnabled = true) {
      override protected def onVirtaOutageDetected(): Unit = alerts += 1
    }
    def advance(millis: Long): Unit = clock += millis
    def outageAlerts: Int = alerts
  }

  "Virta-katkoksen havaitseminen" - {
    "Yksittäiset virheet alle 15 minuutin sisällä eivät aiheuta hälytystä" in {
      val f = new Fixture
      f.monitoring.setSubsystemStatus(Virta, operational = false)
      f.advance(14 * Minute)
      f.monitoring.setSubsystemStatus(Virta, operational = false)
      f.outageAlerts should equal(0)
    }

    "Yhtäjaksoinen virhevirta yli 15 minuutin ajan aiheuttaa täsmälleen yhden hälytyksen" in {
      val f = new Fixture
      f.advance(ThresholdMillis)
      f.monitoring.setSubsystemStatus(Virta, operational = false)
      f.advance(Minute)
      f.monitoring.setSubsystemStatus(Virta, operational = false)
      f.advance(Minute)
      f.monitoring.setSubsystemStatus(Virta, operational = false)
      f.outageAlerts should equal(1)
    }

    "Onnistuminen nollaa tilan, joten myöhempi katko hälyttää uudelleen" in {
      val f = new Fixture
      f.advance(ThresholdMillis)
      f.monitoring.setSubsystemStatus(Virta, operational = false)
      f.outageAlerts should equal(1)

      f.monitoring.setSubsystemStatus(Virta, operational = true)
      f.advance(ThresholdMillis)
      f.monitoring.setSubsystemStatus(Virta, operational = false)
      f.outageAlerts should equal(2)
    }

    "Onnistumiset virhevirran välissä estävät hälytyksen" in {
      val f = new Fixture
      (1 to 10).foreach { _ =>
        f.advance(10 * Minute)
        f.monitoring.setSubsystemStatus(Virta, operational = false)
        f.advance(10 * Minute)
        f.monitoring.setSubsystemStatus(Virta, operational = true)
      }
      f.outageAlerts should equal(0)
    }

    "Muiden alijärjestelmien virheet eivät aiheuta Virta-katkoshälytystä" in {
      val f = new Fixture
      f.advance(ThresholdMillis)
      f.monitoring.setSubsystemStatus(Oppijanumerorekisteri, operational = false)
      f.outageAlerts should equal(0)
    }

    "Hälytys ei laukea kun se on pois päältä (vain tuotannossa käytössä)" in {
      var clock = 0L
      var alerts = 0
      val monitoring = new HealthMonitoring(() => clock, virtaOutageAlertingEnabled = false) {
        override protected def onVirtaOutageDetected(): Unit = alerts += 1
      }
      clock += ThresholdMillis
      monitoring.setSubsystemStatus(Virta, operational = false)
      alerts should equal(0)
    }
  }
}
