package fi.oph.koski.localization

import fi.oph.koski.{KoskiApplicationForTests, TestEnvironment}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class LocalizationReaderSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  private def lokalisointi(kieli: String) =
    new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, kieli, Some(KoskiApplicationForTests.koodistoViitePalvelu))

  "Koodiarvo" - {
    "ruotsiksi näytetään koodin ruotsinkielinen nimi" in {
      val sv = lokalisointi("sv")
      sv.koodiarvo("koskiopiskeluoikeudentila", "lasna") should equal("Närvarande")
      sv.koodiarvo("suorituksentyyppi", "lukionoppimaara") should equal("Gymnasiets lärokurs")
      sv.koodiarvo("opiskeluoikeudentyyppi", "ammatillinenkoulutus") should equal("Yrkesutbildning")
      sv.koodiarvo("lukionkurssintyyppi", "pakollinen") should equal("Obligatorisk")
    }

    "ruotsiksi näytetään koodiarvo, jos ruotsinkielinen nimi on sama kuin suomenkielinen" in {
      lokalisointi("sv").koodiarvo("suorituksentyyppi", "preiboppimaara") should equal("preiboppimaara")
    }

    "tuntematon koodiarvo näytetään sellaisenaan" in {
      lokalisointi("sv").koodiarvo("koskiopiskeluoikeudentila", "-") should equal("-")
    }

    "suomeksi ja englanniksi näytetään koodiarvo" in {
      lokalisointi("fi").koodiarvo("koskiopiskeluoikeudentila", "lasna") should equal("lasna")
      lokalisointi("en").koodiarvo("koskiopiskeluoikeudentila", "lasna") should equal("lasna")
    }

    "ilman koodistoa näytetään koodiarvo" in {
      new LocalizationReader(KoskiApplicationForTests.koskiLocalizationRepository, "sv")
        .koodiarvo("koskiopiskeluoikeudentila", "lasna") should equal("lasna")
    }
  }
}
