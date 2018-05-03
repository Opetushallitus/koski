package fi.oph.koski.localization

import fi.oph.koski.koodisto.{KoodistoKoodi, Koodistot, RemoteKoodistoPalvelu}
import org.scalatest.{AppendedClues, FreeSpec, Matchers}

/**
  * Tests that appropriate translations for koodisto "nimi" exists in the environment defined by the VIRKAILIJA_ROOT environment variable
  */
class KoodistotLocalizationTest extends FreeSpec with Matchers with AppendedClues {
  "Kielistetyt koodiarvot" - {
    lazy val root = sys.env.getOrElse("VIRKAILIJA_ROOT", throw new RuntimeException("Environment variable VIRKAILIJA_ROOT missing"))
    lazy val koodistoPalvelu = new RemoteKoodistoPalvelu(root)

    Koodistot.koodistoAsetukset.foreach { koodistoAsetus =>
      s"${koodistoAsetus.koodisto}" taggedAs (LocalizationTestTag) in {
        val koodistoViite = koodistoPalvelu.getLatestVersion(koodistoAsetus.koodisto)
        koodistoViite shouldBe defined
        val koodit = koodistoPalvelu.getKoodistoKoodit(koodistoViite.get)
        koodit shouldBe defined
        koodit.get should not be empty

        def hasFinnishName(k: KoodistoKoodi) = k.getMetadata("fi").exists(_.nimi.exists(_.trim.nonEmpty))
        def hasSwedishName(k: KoodistoKoodi) = k.getMetadata("sv").exists(_.nimi.exists(_.trim.nonEmpty))
        def hasSomeName(k: KoodistoKoodi) = k.nimi.map(_.get("fi")).exists(n => n != LocalizedString.missingString && n.trim.nonEmpty)
        def url = s"${root}/koski/dokumentaatio/koodisto/${koodistoViite.get.koodistoUri}/latest"

        withClue(s"Nimi puuttuu kokonaan, katso\n$url\n") {
          koodit.get.filterNot(hasSomeName).map(_.koodiUri) shouldBe empty
        }
        if (koodistoAsetus.vaadiSuomenkielinenNimi) {
          withClue(s"Suomenkielinen nimi puuttuu, katso\n$url?kieli=fi\n") {
            koodit.get.filterNot(hasFinnishName).map(_.koodiUri) shouldBe empty
          }
        }
        if (koodistoAsetus.vaadiRuotsinkielinenNimi) {
          withClue(s"Ruotsinkielinen nimi puuttuu, katso\n$url?kieli=sv\n") {
            koodit.get.filterNot(hasSwedishName).map(_.koodiUri) shouldBe empty
          }
        }
      }
    }
  }
}
