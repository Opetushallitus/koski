package fi.oph.koski.environment

import com.typesafe.config.ConfigFactory
import fi.oph.koski.config.{AppConfigAvaintarkistus, AppConfigYhdistys}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class AppConfigYhdistysSpec extends AnyFreeSpec with Matchers {
  private val reference =
    s"""
      |sessionTimeoutMinutes = 60
      |
      |kyselyt {
      |  timeout = 1h
      |  s3.bucket = "kyselyt"
      |  s3.endpoint = "http://localhost:4566"
      |}
      |
      |virta.serviceUrl = mock
      |identification.url.fi = "https://example.com"
      |identification.url.sv = $${identification.url.fi}
      |""".stripMargin

  private def yhdistä(appConfig: String) =
    AppConfigYhdistys.yhdistä(ConfigFactory.parseString(reference), ConfigFactory.parseString(appConfig))

  "Avaimet ovat reference.confin järjestyksessä lohkoissa ja AppConfigin arvot säilyvät" in {
    val tulos = yhdistä(
      """
        |identification.url.fi = "https://opintopolku.fi"
        |kyselyt.s3 { endpoint = "", bucket = "tuotanto" }
        |virta.serviceUrl = ""
        |sessionTimeoutMinutes = 30
        |kyselyt.timeout = 2h
        |identification.url.sv = "https://studieinfo.fi"
        |""".stripMargin)

    tulos.tiedosto should be(
      """sessionTimeoutMinutes = 30
        |
        |kyselyt {
        |  timeout = "2h"
        |  s3 {
        |    bucket = "tuotanto"
        |    endpoint = ""
        |  }
        |}
        |
        |virta.serviceUrl = ""
        |
        |identification.url {
        |  fi = "https://opintopolku.fi"
        |  sv = "https://studieinfo.fi"
        |}
        |""".stripMargin)
    tulos.lisätyt should be(empty)
  }

  "Puuttuva avain saa reference.confin oletuksen viittauksineen" in {
    val tulos = yhdistä("""virta.serviceUrl = "", kyselyt.s3.endpoint = """"")
    tulos.lisätyt should be(List("sessionTimeoutMinutes", "kyselyt.timeout", "kyselyt.s3.bucket", "identification.url.fi", "identification.url.sv"))
    tulos.tiedosto should include("sessionTimeoutMinutes = 60\n")
    tulos.tiedosto should include(s"  sv = $${identification.url.fi}\n")
    tulos.paikallisetOletukset should be(empty)
  }

  "Paikalliselta näyttävä oletus lisätään, mutta siitä varoitetaan" in {
    val tulos = yhdistä("")
    tulos.paikallisetOletukset should be(List("kyselyt.s3.endpoint", "virta.serviceUrl"))
    tulos.tiedosto should include("    endpoint = \"http://localhost:4566\"\n")
    tulos.tiedosto should include("virta.serviceUrl = \"mock\"\n")
  }

  "reference.confin Ei käytössä pilvessä -kommentti kopioidaan, eikä paikallisesta oletuksesta varoiteta" in {
    val referenceKommentilla =
      """
        |// Ei käytössä pilvessä: vain localstack käyttää.
        |endpoint = "http://localhost:4566"
        |// Kehittäjän kommentti
        |muu = 1
        |""".stripMargin
    def yhdistäKommentilla(appConfig: String) =
      AppConfigYhdistys.yhdistä(ConfigFactory.parseString(referenceKommentilla), ConfigFactory.parseString(appConfig))

    val lisätty = yhdistäKommentilla("")
    lisätty.tiedosto should be("# Ei käytössä pilvessä: vain localstack käyttää.\nendpoint = \"http://localhost:4566\"\n\nmuu = 1\n")
    lisätty.paikallisetOletukset should be(empty)

    val olemassaOleva = yhdistäKommentilla("# Oma kommentti\nendpoint = \"\"\nmuu = 2")
    olemassaOleva.tiedosto should startWith("# Oma kommentti\n# Ei käytössä pilvessä: vain localstack käyttää.\nendpoint = \"\"\n")
    yhdistäKommentilla(olemassaOleva.tiedosto).tiedosto should be(olemassaOleva.tiedosto)
  }

  "Avaimet, joita reference.confissa ei ole, tulevat loppuun AppConfigin järjestyksessä" in {
    val tulos = yhdistä(
      """
        |valpas.rajapäivät.2025.alku = "2025-06-01"
        |radiator.apiKey = "x"
        |kyselyt.vanha = 1
        |""".stripMargin)
    tulos.tiedosto should include("    endpoint = \"http://localhost:4566\"\n  }\n  vanha = 1\n}\n")
    tulos.tiedosto should endWith(
      """
        |valpas.rajapäivät.2025.alku = "2025-06-01"
        |
        |radiator.apiKey = "x"
        |""".stripMargin)
  }

  "Kommentit säilyvät, myös objektin kommentti" in {
    val tulos = yhdistä(
      """
        |# Lyhyempi kuin oletus
        |sessionTimeoutMinutes = 30
        |# Kyselyrajapinta
        |kyselyt {
        |  # Pidempi kuin oletus
        |  timeout = 2h
        |}
        |""".stripMargin)
    tulos.tiedosto should include("# Lyhyempi kuin oletus\nsessionTimeoutMinutes = 30\n")
    tulos.tiedosto should include("# Kyselyrajapinta\nkyselyt {\n  # Pidempi kuin oletus\n  timeout = \"2h\"\n")
  }

  "Pitkä lista jaetaan riveille kahden välilyönnin sisennyksellä" in {
    val tulos = yhdistä("""mydata.members = [{ id = "hsl", orgOid = "1.2.246.562.10.00000000001" }, { id = "tre", orgOid = "1.2.246.562.10.00000000002" }]""")
    tulos.tiedosto should include(
      """mydata.members = [
        |  {
        |    "id" : "hsl",
        |    "orgOid" : "1.2.246.562.10.00000000001"
        |  },
        |  {
        |    "id" : "tre",
        |    "orgOid" : "1.2.246.562.10.00000000002"
        |  }
        |]
        |""".stripMargin)
    yhdistä("ips = [\"127.0.0.1\"]").tiedosto should include("ips = [\"127.0.0.1\"]\n")
  }

  "Kommentti, jota ei ole kiinnitetty avaimeen, listataan kadonneena" in {
    val syöte = "# Otsikko\n\n# Erillinen\n\nsessionTimeoutMinutes = 45\n// Kiinni\nmuu = 1\n"
    val tulos = yhdistä(syöte)
    tulos.tiedosto should include("# Kiinni\nmuu = 1\n")
    AppConfigYhdistys.kadonneetKommentit(syöte, tulos.tiedosto) should be(List("Otsikko", "Erillinen"))
  }

  "Lainausmerkit eivät muuta arvoa" in {
    val tulos = yhdistä("muu = org.postgresql.Driver\nmuu2 = \"org.postgresql.Driver\"")
    tulos.tiedosto should include("muu = \"org.postgresql.Driver\"\n")
    tulos.tiedosto should include("muu2 = \"org.postgresql.Driver\"\n")
  }

  "Uudelleen ajo ei muuta tiedostoa" in {
    val ensimmäinen = yhdistä(
      """
        |# Kyselyrajapinta
        |kyselyt { timeout = 2h, s3.endpoint = "" }
        |# Polku
        |identification.url.fi = "https://opintopolku.fi"
        |virta.serviceUrl = ""
        |muu.avain = [1, 2]
        |""".stripMargin)
    yhdistä(ensimmäinen.tiedosto).tiedosto should be(ensimmäinen.tiedosto)
  }

  "Kosken reference.confilla tulos läpäisee avaintarkistuksen" in {
    val tulos = AppConfigYhdistys.yhdistä(AppConfigAvaintarkistus.koskenReferenceConf, ConfigFactory.empty())
    AppConfigAvaintarkistus.tarkista(AppConfigAvaintarkistus.koskenReferenceConf, ConfigFactory.parseString(tulos.tiedosto)).puuttuvat should be(empty)
  }
}
