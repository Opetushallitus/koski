
package fi.oph.koski.todistus

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory.fromAnyRef
import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.koskiuser.MockUsers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class TodistusFeatureFlagsSpec extends AnyFreeSpec with Matchers {

  private val app = KoskiApplicationForTests
  private val pääkäyttäjäSession = MockUsers.paakayttaja.toKoskiSpecificSession(app.käyttöoikeusRepository)
  private val yleisenKielitutkinnonKäyttäjäSession = MockUsers.yleisenKielitutkinnonKäyttäjä.toKoskiSpecificSession(app.käyttöoikeusRepository)

  private def makeConfig(enabledForAll: Boolean, enabledForPääkäyttäjä: Boolean) = {
    ConfigFactory.empty()
      .withValue("todistus.enabledForAll", fromAnyRef(enabledForAll))
      .withValue("todistus.enabledForPääkäyttäjä", fromAnyRef(enabledForPääkäyttäjä))
  }

  "TodistusFeatureFlags" - {
    "kun molemmat flagit ovat pois päältä" - {
      val flags = new TodistusFeatureFlags(makeConfig(enabledForAll = false, enabledForPääkäyttäjä = false))

      "palvelu ei ole käytössä" in {
        flags.isServiceEnabled should be(false)
      }

      "käyttöliittymä ei ole käytössä pääkäyttäjälle" in {
        flags.isEnabledForUser(pääkäyttäjäSession) should be(false)
      }

      "käyttöliittymä ei ole käytössä yleisen kielitutkinnon käyttäjälle" in {
        flags.isEnabledForUser(yleisenKielitutkinnonKäyttäjäSession) should be(false)
      }
    }

    "kun enabledForAll on päällä" - {
      val flags = new TodistusFeatureFlags(makeConfig(enabledForAll = true, enabledForPääkäyttäjä = false))

      "palvelu on käytössä" in {
        flags.isServiceEnabled should be(true)
      }

      "käyttöliittymä on käytössä yleisen kielitutkinnon käyttäjälle" in {
        flags.isEnabledForUser(yleisenKielitutkinnonKäyttäjäSession) should be(true)
      }

      "käyttöliittymä on käytössä pääkäyttäjälle" in {
        flags.isEnabledForUser(pääkäyttäjäSession) should be(true)
      }
    }

    "kun enabledForPääkäyttäjä on päällä" - {
      val flags = new TodistusFeatureFlags(makeConfig(enabledForAll = false, enabledForPääkäyttäjä = true))

      "palvelu on käytössä" in {
        flags.isServiceEnabled should be(true)
      }

      "käyttöliittymä on käytössä pääkäyttäjälle" in {
        flags.isEnabledForUser(pääkäyttäjäSession) should be(true)
      }

      "käyttöliittymä ei ole käytössä yleisen kielitutkinnon käyttäjälle" in {
        flags.isEnabledForUser(yleisenKielitutkinnonKäyttäjäSession) should be(false)
      }
    }

    "kun molemmat flagit ovat päällä" - {
      val flags = new TodistusFeatureFlags(makeConfig(enabledForAll = true, enabledForPääkäyttäjä = true))

      "palvelu on käytössä" in {
        flags.isServiceEnabled should be(true)
      }

      "käyttöliittymä on käytössä yleisen kielitutkinnon käyttäjälle" in {
        flags.isEnabledForUser(yleisenKielitutkinnonKäyttäjäSession) should be(true)
      }

      "käyttöliittymä on käytössä pääkäyttäjälle" in {
        flags.isEnabledForUser(pääkäyttäjäSession) should be(true)
      }
    }
  }
}
