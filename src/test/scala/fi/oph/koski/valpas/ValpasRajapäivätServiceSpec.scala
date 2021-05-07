package fi.oph.koski.valpas

import com.typesafe.config.ConfigFactory
import fi.oph.koski.valpas.opiskeluoikeusrepository.OletuksenaEdellinenVuosiKonfiguraattori

import java.time.LocalDate.{of => date}


class ValpasRajapäivätServiceSpec extends ValpasTestBase {
  private val config = ConfigFactory.parseString(
    """
      |valpas = {
      |  rajapäivät {
      |    2021 {
      |      foo = "2021-05-30"
      |    }
      |    2022 {
      |      foo = "2022-06-05"
      |    }
      |  }
      |}
    """.stripMargin)

  var error: String = ""

  val konfiguraattori = OletuksenaEdellinenVuosiKonfiguraattori(
    2021,
    config,
    (msg: String) => { error = msg },
    vuosi => s"valpas.rajapäivät.${vuosi}.foo"
  )

  "OletuksenaEdellinenVuosiKonfiguraattori palauttaa erikseen määritellyn päivän" in {
    error = ""
    val actualFoo = konfiguraattori.hae(2021)

    actualFoo should equal(date(2021, 5, 30))
    error should equal("")
  }

  "OletuksenaEdellinenVuosiKonfiguraattori palauttaa viimeisen määritellyn vuoden mukaan päätellyn päivän" in {
    error = ""
    val actualFoo = konfiguraattori.hae(2025)

    actualFoo should equal(date(2025, 6, 5))
    error should equal("valpas.rajapäivät.2025.foo ei määritelty, käytetään oletusta 2025-06-05")
  }

  "OletuksenaEdellinenVuosiKonfiguraattori palauttaa aloitusvuoden oletuspäivän" in {
    error = ""
    val actualFoo = konfiguraattori.hae(2020)

    actualFoo should equal(date(2020, 5, 30))
    error should equal("valpas.rajapäivät.2020.foo ei määritelty, käytetään oletusta 2020-05-30")
  }
}
