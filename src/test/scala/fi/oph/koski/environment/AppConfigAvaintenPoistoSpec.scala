package fi.oph.koski.environment

import com.typesafe.config.ConfigFactory
import fi.oph.koski.config.AppConfigAvaintenPoisto
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class AppConfigAvaintenPoistoSpec extends AnyFreeSpec with Matchers {
  private val reference =
    """
      |sessionTimeoutMinutes = 60
      |kyselyt.timeout = 1h
      |""".stripMargin

  private def poista(appConfig: String, avaimet: String*) =
    AppConfigAvaintenPoisto.poista(ConfigFactory.parseString(reference), ConfigFactory.parseString(appConfig), avaimet.toList)

  "Poistaa avaimet ja tyhjäksi jääneen lohkon, muut arvot ja kommentit säilyvät" in {
    val tulos = poista(
      """
        |# Lyhyempi kuin oletus
        |sessionTimeoutMinutes = 30
        |kyselyt {
        |  timeout = 2h
        |  ecs { cluster = "c", service = "s" }
        |}
        |# Vanha
        |piwik.siteId = 7
        |features.kelaui = true
        |features.muu = false
        |""".stripMargin,
      "kyselyt.ecs.cluster", "kyselyt.ecs.service", "piwik.siteId", "features.kelaui"
    )

    tulos.tiedosto should be(
      """# Lyhyempi kuin oletus
        |sessionTimeoutMinutes = 30
        |
        |kyselyt.timeout = "2h"
        |
        |features.muu = false
        |""".stripMargin)
    tulos.poistetut should be(List("kyselyt.ecs.cluster", "kyselyt.ecs.service", "piwik.siteId", "features.kelaui"))
    tulos.puuttuneet should be(empty)
  }

  "Avain, jota tiedostossa ei ole, listataan eikä se ole virhe" in {
    val tulos = poista("sessionTimeoutMinutes = 30\nkyselyt.timeout = 2h\npiwik.siteId = 7", "piwik.siteId", "features.cas")
    tulos.poistetut should be(List("piwik.siteId"))
    tulos.puuttuneet should be(List("features.cas"))
  }

  "Avainta, joka on vielä reference.confissa, ei poisteta" in {
    val virhe = intercept[IllegalArgumentException](poista("sessionTimeoutMinutes = 30\nkyselyt.timeout = 2h", "kyselyt.timeout", "kyselyt"))
    virhe.getMessage should include("kyselyt.timeout, kyselyt")
  }

  "Tiedostoa, josta puuttuu reference.confin avaimia, ei kirjoiteta" in {
    val virhe = intercept[IllegalStateException](poista("sessionTimeoutMinutes = 30\npiwik.siteId = 7", "piwik.siteId"))
    virhe.getMessage should include("kyselyt.timeout")
  }

  "Ääkköset ja numerot avaimen osana toimivat" in {
    val tulos = poista(
      "sessionTimeoutMinutes = 30\nkyselyt.timeout = 2h\nfeatures.lähdejärjestelmäkytkennänPurkaminen = true\nvanha.2025.alku = 1",
      "features.lähdejärjestelmäkytkennänPurkaminen", "vanha.2025.alku"
    )
    tulos.poistetut should have length 2
    tulos.tiedosto should not include "lähdejärjestelmä"
    tulos.tiedosto should not include "vanha"
  }
}
