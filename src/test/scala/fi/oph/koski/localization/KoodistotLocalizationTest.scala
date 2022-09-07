package fi.oph.koski.localization

import fi.oph.koski.TestEnvironment
import fi.oph.koski.koodisto.{KoodistoKoodi, Koodistot, RemoteKoodistoPalvelu}
import fi.oph.koski.schema.LocalizedString
import org.scalatest.AppendedClues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

/**
  * Tests that appropriate translations for koodisto "nimi" exists in the environment defined by the VIRKAILIJA_ROOT environment variable
  */
class KoodistotLocalizationTest extends AnyFreeSpec with TestEnvironment with Matchers with AppendedClues {
  "Kielistetyt koodiarvot" - {
    lazy val root = sys.env.getOrElse("VIRKAILIJA_ROOT", throw new RuntimeException("Environment variable VIRKAILIJA_ROOT missing"))
    lazy val koodistoPalvelu = new RemoteKoodistoPalvelu(root)

    Koodistot.koodistoAsetukset.filter(_.koodistoVersio.isEmpty).foreach { koodistoAsetus =>
      s"${koodistoAsetus.koodisto}" taggedAs (LocalizationTestTag) in {
        val koodistoViite = koodistoPalvelu.getLatestVersionRequired(koodistoAsetus.koodisto)
        val koodit = koodistoPalvelu.getKoodistoKoodit(koodistoViite)
        koodit should not be empty

        def hasFinnishName(k: KoodistoKoodi) = k.getMetadata("fi").exists(_.nimi.exists(_.trim.nonEmpty))
        def hasSwedishName(k: KoodistoKoodi) = k.getMetadata("sv").exists(_.nimi.exists(_.trim.nonEmpty))
        def hasSomeName(k: KoodistoKoodi) = k.nimi.map(_.get("fi")).exists(n => n != LocalizedString.missingString && n.trim.nonEmpty)
        def url = s"${root}/koski/dokumentaatio/koodisto/${koodistoViite.koodistoUri}/latest"

        withClue(s"Nimi puuttuu kokonaan, katso\n$url\n") {
          koodit.filterNot(hasSomeName).map(_.koodiUri).sorted shouldBe empty
        }
        if (koodistoAsetus.vaadiSuomenkielinenNimi) {
          withClue(s"Suomenkielinen nimi puuttuu, katso\n$url?kieli=fi\n") {
            koodit.filterNot(hasFinnishName).map(_.koodiUri).sorted shouldBe empty
          }
        }
        if (koodistoAsetus.vaadiRuotsinkielinenNimi) {
          withClue(s"Ruotsinkielinen nimi puuttuu, katso\n$url?kieli=sv\n") {
            val eiPuutuEnää = koodit.filter(hasSwedishName).map(_.koodiUri).toSet.intersect(ruotsinkielinenKäännösPuuttuuMuttaTuskinTullaanKääntämäänkään)
            if (eiPuutuEnää.nonEmpty) {
              println(s"Ruotsinkielinen nimi ei puutu enää! $eiPuutuEnää")
            }
            koodit.filterNot(hasSwedishName).map(_.koodiUri).toSet.diff(ruotsinkielinenKäännösPuuttuuMuttaTuskinTullaanKääntämäänkään).toList.sorted shouldBe empty
          }
        }
      }
    }
  }

  private val ruotsinkielinenKäännösPuuttuuMuttaTuskinTullaanKääntämäänkään: Set[String] = (
    List(
      "aikuistenperusopetuksenkurssit2015_ai1", "aikuistenperusopetuksenkurssit2015_ai2", "aikuistenperusopetuksenkurssit2015_ai3",
      "aikuistenperusopetuksenkurssit2015_ai4", "aikuistenperusopetuksenkurssit2015_ai5", "aikuistenperusopetuksenkurssit2015_eaa1",
      "aikuistenperusopetuksenkurssit2015_eaa2", "aikuistenperusopetuksenkurssit2015_eaa3", "aikuistenperusopetuksenkurssit2015_eaa4",
      "aikuistenperusopetuksenkurssit2015_eaa5", "aikuistenperusopetuksenkurssit2015_eaa6", "aikuistenperusopetuksenkurssit2015_eaa7",
      "aikuistenperusopetuksenkurssit2015_eaa8", "aikuistenperusopetuksenkurssit2015_iaa1", "aikuistenperusopetuksenkurssit2015_iaa2",
      "aikuistenperusopetuksenkurssit2015_iaa3", "aikuistenperusopetuksenkurssit2015_iaa4", "aikuistenperusopetuksenkurssit2015_iaa5",
      "aikuistenperusopetuksenkurssit2015_iaa6", "aikuistenperusopetuksenkurssit2015_iaa7", "aikuistenperusopetuksenkurssit2015_iaa8",
      "aikuistenperusopetuksenkurssit2015_kxa1", "aikuistenperusopetuksenkurssit2015_kxa2", "aikuistenperusopetuksenkurssit2015_kxa3",
      "aikuistenperusopetuksenkurssit2015_kxa4", "aikuistenperusopetuksenkurssit2015_kxa5", "aikuistenperusopetuksenkurssit2015_kxa6",
      "aikuistenperusopetuksenkurssit2015_kxa7", "aikuistenperusopetuksenkurssit2015_kxa8", "aikuistenperusopetuksenkurssit2015_laa1",
      "aikuistenperusopetuksenkurssit2015_laa2", "aikuistenperusopetuksenkurssit2015_laa3", "aikuistenperusopetuksenkurssit2015_laa4",
      "aikuistenperusopetuksenkurssit2015_laa5", "aikuistenperusopetuksenkurssit2015_laa6", "aikuistenperusopetuksenkurssit2015_laa7",
      "aikuistenperusopetuksenkurssit2015_laa8", "aikuistenperusopetuksenkurssit2015_poa1", "aikuistenperusopetuksenkurssit2015_poa2",
      "aikuistenperusopetuksenkurssit2015_poa3", "aikuistenperusopetuksenkurssit2015_poa4", "aikuistenperusopetuksenkurssit2015_poa5",
      "aikuistenperusopetuksenkurssit2015_poa6", "aikuistenperusopetuksenkurssit2015_poa7", "aikuistenperusopetuksenkurssit2015_poa8",
      "aikuistenperusopetuksenkurssit2015_raa1", "aikuistenperusopetuksenkurssit2015_raa2", "aikuistenperusopetuksenkurssit2015_raa3",
      "aikuistenperusopetuksenkurssit2015_raa4", "aikuistenperusopetuksenkurssit2015_raa5", "aikuistenperusopetuksenkurssit2015_raa6",
      "aikuistenperusopetuksenkurssit2015_raa7", "aikuistenperusopetuksenkurssit2015_raa8", "aikuistenperusopetuksenkurssit2015_rub1",
      "aikuistenperusopetuksenkurssit2015_rub2", "aikuistenperusopetuksenkurssit2015_rub3", "aikuistenperusopetuksenkurssit2015_rub4",
      "aikuistenperusopetuksenkurssit2015_rub5", "aikuistenperusopetuksenkurssit2015_rub6", "aikuistenperusopetuksenkurssit2015_rub7",
      "aikuistenperusopetuksenkurssit2015_rub8", "aikuistenperusopetuksenkurssit2015_saa1", "aikuistenperusopetuksenkurssit2015_saa2",
      "aikuistenperusopetuksenkurssit2015_saa3", "aikuistenperusopetuksenkurssit2015_saa4", "aikuistenperusopetuksenkurssit2015_saa5",
      "aikuistenperusopetuksenkurssit2015_saa6", "aikuistenperusopetuksenkurssit2015_saa7", "aikuistenperusopetuksenkurssit2015_saa8",
      "aikuistenperusopetuksenkurssit2015_sma1", "aikuistenperusopetuksenkurssit2015_sma2", "aikuistenperusopetuksenkurssit2015_sma3",
      "aikuistenperusopetuksenkurssit2015_sma4", "aikuistenperusopetuksenkurssit2015_sma5", "aikuistenperusopetuksenkurssit2015_sma6",
      "aikuistenperusopetuksenkurssit2015_sma7", "aikuistenperusopetuksenkurssit2015_sma8", "aikuistenperusopetuksenkurssit2015_vea1",
      "aikuistenperusopetuksenkurssit2015_vea2", "aikuistenperusopetuksenkurssit2015_vea3", "aikuistenperusopetuksenkurssit2015_vea4",
      "aikuistenperusopetuksenkurssit2015_vea5", "aikuistenperusopetuksenkurssit2015_vea6", "aikuistenperusopetuksenkurssit2015_vea7",
      "aikuistenperusopetuksenkurssit2015_vea8"
    ) ++ List(
      "lukionkurssit_ux1", "lukionkurssit_ux2", "lukionkurssit_ux3", "lukionkurssit_ux4", "lukionkurssit_ux5", "lukionkurssit_ux6"
    ) ++ List(
      "lukionkurssitops2003nuoret_sma3", "lukionkurssitops2003nuoret_sma5", "lukionkurssitops2003nuoret_sma7", "lukionkurssitops2003nuoret_sma8",
      "lukionkurssitops2003nuoret_smb23", "lukionkurssitops2003nuoret_smb25", "lukionkurssitops2003nuoret_smb27", "lukionkurssitops2003nuoret_smb28",
      "lukionkurssitops2003nuoret_smb32", "lukionkurssitops2003nuoret_smb33", "lukionkurssitops2003nuoret_smb35", "lukionkurssitops2003nuoret_smb36",
      "lukionkurssitops2003nuoret_smb38", "lukionkurssitops2003nuoret_ub1", "lukionkurssitops2003nuoret_ub2", "lukionkurssitops2003nuoret_ub3",
      "lukionkurssitops2003nuoret_ub4", "lukionkurssitops2003nuoret_ub5", "lukionkurssitops2003nuoret_ud1", "lukionkurssitops2003nuoret_ud2",
      "lukionkurssitops2003nuoret_ud3", "lukionkurssitops2003nuoret_ud4", "lukionkurssitops2003nuoret_ud5", "lukionkurssitops2003nuoret_uh1",
      "lukionkurssitops2003nuoret_uh2", "lukionkurssitops2003nuoret_uh3", "lukionkurssitops2003nuoret_uh4", "lukionkurssitops2003nuoret_uh5",
      "lukionkurssitops2003nuoret_ui1", "lukionkurssitops2003nuoret_ui2", "lukionkurssitops2003nuoret_ui3", "lukionkurssitops2003nuoret_ui4",
      "lukionkurssitops2003nuoret_ui5", "lukionkurssitops2003nuoret_uj1", "lukionkurssitops2003nuoret_uj2", "lukionkurssitops2003nuoret_uj3",
      "lukionkurssitops2003nuoret_uj4", "lukionkurssitops2003nuoret_uj5", "lukionkurssitops2003nuoret_uk1", "lukionkurssitops2003nuoret_uk2",
      "lukionkurssitops2003nuoret_uk3", "lukionkurssitops2003nuoret_uk4", "lukionkurssitops2003nuoret_uk5", "lukionkurssitops2003nuoret_um1",
      "lukionkurssitops2003nuoret_um2", "lukionkurssitops2003nuoret_um3", "lukionkurssitops2003nuoret_um4", "lukionkurssitops2003nuoret_um5",
      "lukionkurssitops2003nuoret_ur1", "lukionkurssitops2003nuoret_ur2", "lukionkurssitops2003nuoret_ur3", "lukionkurssitops2003nuoret_ur4",
      "lukionkurssitops2003nuoret_ur5"
    ) ++ List(
      "lukionkurssitops2004aikuiset_air1", "lukionkurssitops2004aikuiset_air2", "lukionkurssitops2004aikuiset_air3",
      "lukionkurssitops2004aikuiset_air4", "lukionkurssitops2004aikuiset_air5", "lukionkurssitops2004aikuiset_air6",
      "lukionkurssitops2004aikuiset_air7", "lukionkurssitops2004aikuiset_air8", "lukionkurssitops2004aikuiset_air9",
      "lukionkurssitops2004aikuiset_ais1", "lukionkurssitops2004aikuiset_ais2", "lukionkurssitops2004aikuiset_ais3",
      "lukionkurssitops2004aikuiset_ais4", "lukionkurssitops2004aikuiset_ais5", "lukionkurssitops2004aikuiset_ais6",
      "lukionkurssitops2004aikuiset_ais7", "lukionkurssitops2004aikuiset_ais8", "lukionkurssitops2004aikuiset_ais9",
      "lukionkurssitops2004aikuiset_aiv1", "lukionkurssitops2004aikuiset_aiv2", "lukionkurssitops2004aikuiset_aiv3",
      "lukionkurssitops2004aikuiset_aiv4", "lukionkurssitops2004aikuiset_aiv5", "lukionkurssitops2004aikuiset_aiv6",
      "lukionkurssitops2004aikuiset_aiv7", "lukionkurssitops2004aikuiset_aiv8", "lukionkurssitops2004aikuiset_aiv9",
      "lukionkurssitops2004aikuiset_op1", "lukionkurssitops2004aikuiset_op2", "lukionkurssitops2004aikuiset_sma3",
      "lukionkurssitops2004aikuiset_sma4", "lukionkurssitops2004aikuiset_sma5", "lukionkurssitops2004aikuiset_sma6",
      "lukionkurssitops2004aikuiset_sma7", "lukionkurssitops2004aikuiset_sma8", "lukionkurssitops2004aikuiset_smb17",
      "lukionkurssitops2004aikuiset_sus1", "lukionkurssitops2004aikuiset_sus2", "lukionkurssitops2004aikuiset_sus3",
      "lukionkurssitops2004aikuiset_sus4", "lukionkurssitops2004aikuiset_sus5", "lukionkurssitops2004aikuiset_sus6",
      "lukionkurssitops2004aikuiset_sus7", "lukionkurssitops2004aikuiset_sus8", "lukionkurssitops2004aikuiset_sus9",
      "lukionkurssitops2004aikuiset_tald7", "lukionkurssitops2004aikuiset_ub1", "lukionkurssitops2004aikuiset_ub2",
      "lukionkurssitops2004aikuiset_ub3", "lukionkurssitops2004aikuiset_ub4", "lukionkurssitops2004aikuiset_ub5",
      "lukionkurssitops2004aikuiset_ud1", "lukionkurssitops2004aikuiset_ud2", "lukionkurssitops2004aikuiset_ud3",
      "lukionkurssitops2004aikuiset_ud4", "lukionkurssitops2004aikuiset_ud5", "lukionkurssitops2004aikuiset_uh1",
      "lukionkurssitops2004aikuiset_uh2", "lukionkurssitops2004aikuiset_uh3", "lukionkurssitops2004aikuiset_uh4",
      "lukionkurssitops2004aikuiset_uh5", "lukionkurssitops2004aikuiset_ui1", "lukionkurssitops2004aikuiset_ui2",
      "lukionkurssitops2004aikuiset_ui3", "lukionkurssitops2004aikuiset_ui4", "lukionkurssitops2004aikuiset_ui5",
      "lukionkurssitops2004aikuiset_uj1", "lukionkurssitops2004aikuiset_uj2", "lukionkurssitops2004aikuiset_uj3",
      "lukionkurssitops2004aikuiset_uj4", "lukionkurssitops2004aikuiset_uj5", "lukionkurssitops2004aikuiset_uk1",
      "lukionkurssitops2004aikuiset_uk2", "lukionkurssitops2004aikuiset_uk3", "lukionkurssitops2004aikuiset_uk4",
      "lukionkurssitops2004aikuiset_uk5", "lukionkurssitops2004aikuiset_um1", "lukionkurssitops2004aikuiset_um2",
      "lukionkurssitops2004aikuiset_um3", "lukionkurssitops2004aikuiset_um4", "lukionkurssitops2004aikuiset_um5",
      "lukionkurssitops2004aikuiset_ur1", "lukionkurssitops2004aikuiset_ur2", "lukionkurssitops2004aikuiset_ur3",
      "lukionkurssitops2004aikuiset_ur4", "lukionkurssitops2004aikuiset_ur5"
    ) ++ List(
      "suorituksentyyppi_aikuistenperusopetuksenalkuvaiheenkurssi", "suorituksentyyppi_aikuistenperusopetuksenoppiaine", "suorituksentyyppi_ammatillisentutkinnonosanosaalue"
    )
  ).toSet
}
