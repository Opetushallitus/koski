package fi.oph.koski.ibis

import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class IbisKielikarttaSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  "Kielikartta" - {
    "tunnistaa IBIS:n kielten nimet" in {
      List("FINNISH", "SWEDISH", "ENGLISH", "SPANISH", "NORWEGIAN", "JAPANESE").map(IbisKielikartta.get) should be(
        List(Some("FI"), Some("SV"), Some("EN"), Some("ES"), Some("NO"), Some("JA"))
      )
    }

    "tunnistaa saman kielen eri nimet" in {
      List("CHINESE", "MANDARIN", "CANTONESE").map(IbisKielikartta.get).distinct should be(List(Some("ZH")))
      List("MOD. GREEK", "MODERN GREEK").map(IbisKielikartta.get).distinct should be(List(Some("EL")))
    }

    "ei välitä kirjainkoosta eikä ylimääräisistä välilyönneistä" in {
      IbisKielikartta.get("  finnish ") should be(Some("FI"))
      IbisKielikartta.get("modern   greek") should be(Some("EL"))
    }

    "ei tunnista kieliä, joille ei ole yksiselitteistä koodia" in {
      List("BEMBA", "CLASS.GREEK", "FILIPINO", "NDEBELE", "").flatMap(IbisKielikartta.get) should be(empty)
    }

    "kartan nimet ovat valmiiksi normalisoituja" in {
      IbisKielikartta.kielet.keys.filterNot(nimi => IbisOppiainekartta.normalisoi(nimi) == nimi) should be(empty)
    }

    "kaikki koodit löytyvät kieli-koodistosta" in {
      val puuttuvat = IbisKielikartta.kielet.values.toList.distinct.sorted
        .filter(koodi => KoskiApplicationForTests.koodistoViitePalvelu.validate("kieli", koodi).isEmpty)
      puuttuvat should be(empty)
    }

    "vastaa oppiainekartan kielioppiaineiden kieliä" in {
      // Kielivalikoimassa mandariinikiinalla ja kantoninkiinalla on omat koodinsa, ja filipinolla on koodi vain kielivalikoimassa.
      val kielivalikoimanErot = Map("CMN" -> Some("ZH"), "YUE" -> Some("ZH"), "FIL" -> None)
      val eriävät = IbisOppiainekartta.oppiaineet.toList.sortBy(_._1).collect {
        case (nimi, IbisKieliOppiaine(_, kieli)) if IbisKielikartta.get(kielenNimi(nimi)) != kielivalikoimanErot.getOrElse(kieli, Some(kieli)) => nimi
      }
      eriävät should be(empty)
    }
  }

  private val KielenNimiJaTaso = """^(.+?) (?:A|AB\.?|B)(?:[ :].*)?$""".r

  // Kielioppiaineen nimi alkaa kielen nimellä, jota seuraa A, AB. tai B (FINNISH A: Literature, CHINESE B - MANDARIN).
  // Klassisten kielten nimi on pelkkä kielen nimi (LATIN).
  private def kielenNimi(oppiaineenNimi: String): String = oppiaineenNimi match {
    case KielenNimiJaTaso(kieli) => kieli
    case muu => muu
  }
}
