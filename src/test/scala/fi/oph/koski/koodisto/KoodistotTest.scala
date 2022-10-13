package fi.oph.koski.koodisto

import fi.oph.koski.TestEnvironment
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class KoodistotTest extends AnyFreeSpec with TestEnvironment with Matchers {
  "Koski-koodistojen mockdata löytyy, ja codesGroupUri on oikein" - {
    Koodistot.koskiKoodistoAsetukset.foreach { koodistoAsetus =>
      s"${koodistoAsetus.koodisto}/${koodistoAsetus.koodistoVersio}" in {
        getKoodisto(koodistoAsetus.koodisto, koodistoAsetus.koodistoVersio).codesGroupUri should equal("koski")
      }
    }
  }

  "Muiden koodistojen mockdata löytyy, ja codesGroupUri on oikein" - {
    // Suodata pois koskikoulutustendiaarinumerot joka on siirretty muihin koodistoihin jotta sitä ei yritetä luoda ympäristöihin
    Koodistot.muutKoodistoAsetukset.filter(_.koodisto != "koskikoulutustendiaarinumerot").foreach { koodistoAsetus =>
      s"${koodistoAsetus.koodisto}/${koodistoAsetus.koodistoVersio}" in {
        getKoodisto(koodistoAsetus.koodisto, koodistoAsetus.koodistoVersio).codesGroupUri should not equal("koski")
      }
    }
  }

  // Uutta koodistoa luodessa koodistopalvelu ei nykyään käytä annettua koodistoUri:a,
  // vaan muodostaa koodistoUri:n nimestä (suomenkielisestä jos löyty). Nimeä voi
  // kyllä muokata myöhemmin, joten ne eivät aina täsmää palvelussa.
  "Koski-koodistojen nimi ja koodistoUri täsmäävät" - {
    Koodistot.koskiKoodistoAsetukset.foreach { koodistoAsetus =>
      s"${koodistoAsetus.koodisto}/${koodistoAsetus.koodistoVersio}" in {
        val koodisto = getKoodisto(koodistoAsetus.koodisto, koodistoAsetus.koodistoVersio)
        val preferredOrder = Seq("FI", "SV", "EN")
        val nimi = koodisto.metadata
          .sortBy(m => preferredOrder.indexOf(m.kieli))
          .headOption
          .getOrElse(throw new RuntimeException("Metadata puuttuu?"))
          .nimi
          .getOrElse(throw new RuntimeException("Nimi puuttuu?"))
        transliterate(nimi) should equal(koodistoAsetus.koodisto)
      }
    }
  }

  // Vastaavasti uutta koodia ei voi luoda suoraan halutulle koodiUrille, vaan
  // koodistopalvelu muodostaa koodiUri:n koodistoUri:sta ja koodiArvosta (ja
  // tarvittaessa lisää loppuun "-1", "-2", jne. jos tulee duplikaatteja).
  // Jotta saadaan ennustettavat koodiUri:t (ja KoodistoCreator.scala toimii oikein)
  // niin pitää noudattaa samaa kaavaa.
  "Koski-koodistojen koodien koodiArvo ja koodiUri täsmäävät" - {
    // Nämä koodiarvot eivät noudata tätä kaavaa, joten niitä ei enää välttämättä pysty
    // luomaan automaattisesti uudestaan koodistopalveluun. Tuotantoon niitä ei tietysti
    // tarvitsekaan enää luoda uudestaan, joten annetaan niiden olla.
    val PoikkeavatKooditUrit = Seq(
      "arviointiasteikkodiavalmistava_2-1",
      "koskiyoarvosanat_i-1",
      "koskiyoarvosanat_i-2",
      "koskiyoarvosanat_i-3",
      "erityinenkoulutustehtava_ib-1",
      "lahdejarjestelma_espoovarda",
      "lahdejarjestelma_espoovarda-1",
      "arviointiasteikkoib_fail",
      "suorituksentyyppi_lukionoppiaineidenoppimaarat2019",
      "suorituksentyyppi_lukionpaikallinenopintojakso2019",
      "suorituksentyyppi_lukionvaltakunnallinenmoduuli2019",
    )
    // Tässä koodistossa on niin monta poikkeusta ettei erikseen luetella niitä tässä.
    val PoikkeavatKoodistot = Seq(
      "aikuistenperusopetuksenpaattovaiheenkurssit2017"
    )
    Koodistot.koskiKoodistot.filterNot(PoikkeavatKoodistot.contains).foreach { koodistoUri =>
      koodistoUri in {
        val koodit = getKoodistoKoodit(koodistoUri)
        koodit.filterNot(k => PoikkeavatKooditUrit.contains(k.koodiUri)).foreach { koodi =>
          var arvoTransliterated = transliterate(koodi.koodiArvo)
          if (arvoTransliterated.isEmpty)
            arvoTransliterated = "-"
          koodi.koodiUri should equal(s"${koodistoUri}_${arvoTransliterated}")
        }
      }
    }
  }

  private def getKoodisto(koodistoUri: String, koodistoVersio: Option[Int]) = {
    val versio = koodistoVersio.map(v => KoodistoViite(koodistoUri, v))
      .getOrElse(MockKoodistoPalvelu().getLatestVersionRequired(koodistoUri))
    MockKoodistoPalvelu().getKoodisto(versio).get
  }
  private def getKoodistoKoodit(koodistoUri: String) = {
    val viite = MockKoodistoPalvelu().getLatestVersionRequired(koodistoUri)
    MockKoodistoPalvelu().getKoodistoKoodit(viite)
  }

  // ks https://github.com/Opetushallitus/koodisto/blob/master/koodisto-service/src/main/java/fi/vm/sade/koodisto/service/business/impl/UriTransliteratorImpl.java
  private def transliterate(s: String): String = {
    s
      .toLowerCase
      .replace("å", "o")
      .replace("ä", "a")
      .replace("ö", "o")
      .filter(c => "abcdefghijklmnopqrstuvwxyz0123456789".contains(c))
  }
}
