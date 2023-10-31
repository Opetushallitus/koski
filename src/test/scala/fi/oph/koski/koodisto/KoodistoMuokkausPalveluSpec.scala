package fi.oph.koski.koodisto

import fi.oph.koski.http.ServiceConfig
import fi.oph.koski.{TestEnvironment}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class KoodistoMuokkausPalveluSpec extends AnyFreeSpec with TestEnvironment with Matchers {

  val config = ServiceConfig("fooUrl", "fooUser", "fooPwd", useCas = false)
  val kmp = new KoodistoMuokkausPalvelu(config)

  "Koodiin, jolta puuttuu ruotsinkielinen nimi, lisätään se" in {

    val koodi = KoodistoKoodi(
      koodiUri = "testi",
      koodiArvo = "testi",
      metadata = List(
        KoodistoKoodiMetadata(
          nimi = Some("suomi"),
          lyhytNimi = None,
          kuvaus = Some("suomi kuvaus"),
          kieli = Some("FI")
        ),
        KoodistoKoodiMetadata(
          nimi = None,
          lyhytNimi = None,
          kuvaus = None,
          kieli = Some("SV")
        ),
        KoodistoKoodiMetadata(
          nimi = Some("englanti"),
          lyhytNimi = None,
          kuvaus = None,
          kieli = Some("EN")
        ),
      ), versio = 1)

    val expectedTäydennettyKoodi = KoodistoKoodi(
      koodiUri = "testi",
      koodiArvo = "testi",
      metadata = List(
        KoodistoKoodiMetadata(
          nimi = Some("suomi"),
          lyhytNimi = None,
          kuvaus = Some("suomi kuvaus"),
          kieli = Some("FI")
        ),
        KoodistoKoodiMetadata(
          nimi = Some("suomi"),
          lyhytNimi = None,
          kuvaus = Some("suomi kuvaus"),
          kieli = Some("SV")
        ),
        KoodistoKoodiMetadata(
          nimi = Some("englanti"),
          lyhytNimi = None,
          kuvaus = None,
          kieli = Some("EN")
        ),
      ),
      versio = 1,
      tila = Some("LUONNOS"),
      version = Some(0),
    )

    val täydennettyKoodi =
      kmp.modifyKoodiForUpdate(koodi)

    täydennettyKoodi should equal(expectedTäydennettyKoodi)
  }

}
