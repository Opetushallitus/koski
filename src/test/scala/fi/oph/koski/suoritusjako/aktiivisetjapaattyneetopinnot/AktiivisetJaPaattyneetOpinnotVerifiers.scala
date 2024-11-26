package fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot

import fi.oph.koski.aktiivisetjapaattyneetopinnot.{AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus, AktiivisetJaPäättyneetOpinnotDIAOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotEBTutkinnonOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotEBTutkinnonPäätasonSuoritus, AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus, AktiivisetJaPäättyneetOpinnotIBOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus, AktiivisetJaPäättyneetOpinnotKorkeakouluSuoritus, AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus, AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus, AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotLukionOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus, AktiivisetJaPäättyneetOpinnotMuunAmmatillisenKoulutuksenSuoritus, AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus, AktiivisetJaPäättyneetOpinnotOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotOsaamisenHankkimistavallinen, AktiivisetJaPäättyneetOpinnotPäätasonSuoritus, AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus, AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonOpiskeluoikeus, AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonPäätasonSuoritus, Suoritus}
import fi.oph.koski.schema
import org.scalatest.matchers.should.Matchers

trait AktiivisetJaPäättyneetOpinnotVerifiers extends Matchers {

  protected def verifyOpiskeluoikeusJaSuoritus(
    actualOo: AktiivisetJaPäättyneetOpinnotOpiskeluoikeus,
    actualSuoritukset: Seq[Suoritus],
    expectedOoData: schema.Opiskeluoikeus,
    expectedSuoritusDatat: Seq[schema.Suoritus]
  ): Unit = {
    actualSuoritukset.length should equal(expectedSuoritusDatat.length)

    actualSuoritukset.zip(expectedSuoritusDatat).foreach {
      case (actualSuoritus, expectedSuoritusData) =>
        (actualOo, actualSuoritus, expectedOoData, expectedSuoritusData) match {
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus,
            expectedOoData: schema.AmmatillinenOpiskeluoikeus,
            expectedSuoritusData: schema.AmmatillinenPäätasonSuoritus
            ) => verifyAmmatillinen(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotKorkeakouluSuoritus,
            expectedOoData: schema.KorkeakoulunOpiskeluoikeus,
            expectedSuoritusData: schema.KorkeakouluSuoritus
            ) => verifyKorkeakoulu(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotPäätasonSuoritus,
            expectedOoData: schema.AikuistenPerusopetuksenOpiskeluoikeus,
            expectedSuoritusData: schema.AikuistenPerusopetuksenPäätasonSuoritus
            ) => verifyAikuistenPerusopetus(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotDIAOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotPäätasonSuoritus,
            expectedOoData: schema.DIAOpiskeluoikeus,
            expectedSuoritusData: schema.DIAPäätasonSuoritus
            ) => verifyDIA(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotIBOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotPäätasonSuoritus,
            expectedOoData: schema.IBOpiskeluoikeus,
            expectedSuoritusData: schema.IBPäätasonSuoritus
            ) => verifyIB(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotLukionOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotPäätasonSuoritus,
            expectedOoData: schema.LukionOpiskeluoikeus,
            expectedSuoritusData: schema.LukionPäätasonSuoritus
            ) => verifyLukio(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotPäätasonSuoritus,
            expectedOoData: schema.TutkintokoulutukseenValmentavanOpiskeluoikeus,
            expectedSuoritusData: schema.TutkintokoulutukseenValmentavanKoulutuksenSuoritus
            ) => verifyTuva(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus,
            expectedOoData: schema.EuropeanSchoolOfHelsinkiOpiskeluoikeus,
            expectedSuoritusData: schema.EuropeanSchoolOfHelsinkiPäätasonSuoritus
            ) => verifyESH(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotEBTutkinnonOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotEBTutkinnonPäätasonSuoritus,
            expectedOoData: schema.EBOpiskeluoikeus,
            expectedSuoritusData: schema.EBTutkinnonSuoritus,
            ) => verifyEB(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus,
            expectedOoData: schema.InternationalSchoolOpiskeluoikeus,
            expectedSuoritusData: schema.InternationalSchoolVuosiluokanSuoritus
            ) => verifyISH(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus,
            expectedOoData: schema.MuunKuinSäännellynKoulutuksenOpiskeluoikeus,
            expectedSuoritusData: schema.MuunKuinSäännellynKoulutuksenPäätasonSuoritus
            ) => verifyMUKS(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus,
            expectedOoData: schema.VapaanSivistystyönOpiskeluoikeus,
            expectedSuoritusData: schema.VapaanSivistystyönKoulutuksenPäätasonSuoritus
            ) => verifyVST(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case (
            actualOo: AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonOpiskeluoikeus,
            actualSuoritus: AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonPäätasonSuoritus,
            expectedOoData: schema.YlioppilastutkinnonOpiskeluoikeus,
            expectedSuoritusData: schema.YlioppilastutkinnonSuoritus
            ) => verifyYO(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
          case _ => fail(s"Palautettiin tunnistamattoman tyyppistä dataa actual: (${actualOo.getClass.getName},${actualSuoritus.getClass.getName}), expected:(${expectedOoData.getClass.getName},${expectedSuoritusData.getClass.getName})")
        }
    }

  }

  private def verifyAikuistenPerusopetus(
    actualOo: AktiivisetJaPäättyneetOpinnotAikuistenPerusopetuksenOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotPäätasonSuoritus,
    expectedOoData: schema.AikuistenPerusopetuksenOpiskeluoikeus,
    expectedSuoritusData: schema.AikuistenPerusopetuksenPäätasonSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)

    verifyPäätasonSuoritus(actualSuoritus, expectedSuoritusData)
  }

  private def verifyDIA(
    actualOo: AktiivisetJaPäättyneetOpinnotDIAOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotPäätasonSuoritus,
    expectedOoData: schema.DIAOpiskeluoikeus,
    expectedSuoritusData: schema.DIAPäätasonSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)

    verifyPäätasonSuoritus(actualSuoritus, expectedSuoritusData)
  }

  private def verifyIB(
    actualOo: AktiivisetJaPäättyneetOpinnotIBOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotPäätasonSuoritus,
    expectedOoData: schema.IBOpiskeluoikeus,
    expectedSuoritusData: schema.IBPäätasonSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)

    verifyPäätasonSuoritus(actualSuoritus, expectedSuoritusData)
  }

  private def verifyESH(
    actualOo: AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotEuropeanSchoolOfHelsinkiPäätasonSuoritus,
    expectedOoData: schema.EuropeanSchoolOfHelsinkiOpiskeluoikeus,
    expectedSuoritusData: schema.EuropeanSchoolOfHelsinkiPäätasonSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
  }

  private def verifyEB(
    actualOo: AktiivisetJaPäättyneetOpinnotEBTutkinnonOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotEBTutkinnonPäätasonSuoritus,
    expectedOoData: schema.EBOpiskeluoikeus,
    expectedSuoritusData: schema.EBTutkinnonSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
  }

  private def verifyISH(
    actualOo: AktiivisetJaPäättyneetOpinnotInternationalSchoolOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotInternationalSchoolVuosiluokanSuoritus,
    expectedOoData: schema.InternationalSchoolOpiskeluoikeus,
    expectedSuoritusData: schema.InternationalSchoolVuosiluokanSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
  }

  private def verifyMUKS(
    actualOo: AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus,
    expectedOoData: schema.MuunKuinSäännellynKoulutuksenOpiskeluoikeus,
    expectedSuoritusData: schema.MuunKuinSäännellynKoulutuksenPäätasonSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
    actualSuoritus.koulutusmoduuli.koulutustyyppi.map(_.koodiarvo) should equal(expectedSuoritusData.koulutusmoduuli.koulutustyyppi.map(_.koodiarvo))
    actualSuoritus.koulutusmoduuli.opintokokonaisuus.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.opintokokonaisuus.koodiarvo)
  }

  private def verifyVST(
    actualOo: AktiivisetJaPäättyneetOpinnotVapaanSivistystyönOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus,
    expectedOoData: schema.VapaanSivistystyönOpiskeluoikeus,
    expectedSuoritusData: schema.VapaanSivistystyönKoulutuksenPäätasonSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
  }

  private def verifyYO(
    actualOo: AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonPäätasonSuoritus,
    expectedOoData: schema.YlioppilastutkinnonOpiskeluoikeus,
    expectedSuoritusData: schema.YlioppilastutkinnonSuoritus
  ): Unit = {
    verifyOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
    actualSuoritus.vahvistus.map(_.päivä) should equal(expectedSuoritusData.vahvistus.map(_.päivä))
    actualSuoritus.vahvistus.isDefined should be(true)
  }

  private def verifyLukio(
    actualOo: AktiivisetJaPäättyneetOpinnotLukionOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotPäätasonSuoritus,
    expectedOoData: schema.LukionOpiskeluoikeus,
    expectedSuoritusData: schema.LukionPäätasonSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)

    verifyPäätasonSuoritus(actualSuoritus, expectedSuoritusData)
  }

  private def verifyTuva(
    actualOo: AktiivisetJaPäättyneetOpinnotTutkintokoulutukseenValmentavanOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotPäätasonSuoritus,
    expectedOoData: schema.TutkintokoulutukseenValmentavanOpiskeluoikeus,
    expectedSuoritusData: schema.TutkintokoulutukseenValmentavanKoulutuksenSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)

    expectedOoData.lisätiedot match {
      case Some(lt: schema.TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot) =>
        actualOo.lisätiedot.map(_.osaAikaisuusjaksot.map(_.length)) should equal(Some(lt.osaAikaisuusjaksot.map(_.length)))
        actualOo.lisätiedot.map(_.osaAikaisuusjaksot.map(_.map(_.alku))) should equal(Some(lt.osaAikaisuusjaksot.map(_.map(_.alku))))
        actualOo.lisätiedot.map(_.osaAikaisuusjaksot.map(_.map(_.loppu))) should equal(Some(lt.osaAikaisuusjaksot.map(_.map(_.loppu))))
        actualOo.lisätiedot.map(_.osaAikaisuusjaksot.map(_.map(_.osaAikaisuus))) should equal(Some(lt.osaAikaisuusjaksot.map(_.map(_.osaAikaisuus))))
      case _ =>
        actualOo.lisätiedot.flatMap(_.osaAikaisuusjaksot) should be(None)
    }

    verifyPäätasonSuoritus(actualSuoritus, expectedSuoritusData)
    actualSuoritus.koulutusmoduuli.koulutustyyppi.map(_.koodiarvo) should equal(expectedSuoritusData.koulutusmoduuli.koulutustyyppi.map(_.koodiarvo))
    actualSuoritus.koulutusmoduuli.perusteenDiaarinumero should equal(expectedSuoritusData.koulutusmoduuli.perusteenDiaarinumero)
  }

  private def verifyAmmatillinen(
    actualOo: AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus,
    expectedOoData: schema.AmmatillinenOpiskeluoikeus,
    expectedSuoritusData: schema.AmmatillinenPäätasonSuoritus
  ): Unit = {
    verifyAmmatillinenOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    expectedSuoritusData match {
      case expectedSuoritusData: schema.AmmatillisenTutkinnonSuoritus =>
        verifyAmmatillisenTutkinnonSuoritus(actualSuoritus, expectedSuoritusData)
      case expectedSuoritusData: schema.AmmatillisenTutkinnonOsittainenSuoritus =>
        verifyAmmatillisenTutkinnonOsittainenSuoritus(actualSuoritus, expectedSuoritusData)
      case expectedSuoritusData: schema.TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus =>
        verifyTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus(actualSuoritus, expectedSuoritusData)
      case expectedSuoritusData: schema.MuunAmmatillisenKoulutuksenSuoritus =>
        verifyMuunAmmatillisenKoulutuksenSuoritus(actualSuoritus.asInstanceOf[AktiivisetJaPäättyneetOpinnotMuunAmmatillisenKoulutuksenSuoritus], expectedSuoritusData)
      case expectedSuoritusData: schema.NäyttötutkintoonValmistavanKoulutuksenSuoritus =>
        verifyNäyttötutkintoonValmistavanKoulutuksenSuoritus(actualSuoritus, expectedSuoritusData)
      case expectedSuoritusData: schema.TelmaKoulutuksenSuoritus =>
        verifyTelmaKoulutuksenSuoritus(actualSuoritus, expectedSuoritusData)
      case expectedSuoritusData: schema.ValmaKoulutuksenSuoritus =>
        verifyValmaKoulutuksenSuoritus(actualSuoritus, expectedSuoritusData)
      case _ => fail(s"Palautettiin tunnistamattoman tyyppistä suoritusdataa actual: (${actualSuoritus.getClass.getName}), expected:(${expectedSuoritusData.getClass.getName})")
    }
  }

  private def verifyAmmatillisenTutkinnonSuoritus(
    actualSuoritus: AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus,
    expectedSuoritusData: schema.AmmatillisenTutkinnonSuoritus
  ): Unit = {
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should be(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)

    verifyOsaamisenHankkimistavallinen(actualSuoritus, expectedSuoritusData)
    verifyKoulutussopimuksellinen(actualSuoritus, expectedSuoritusData)
  }

  private def verifyAmmatillisenTutkinnonOsittainenSuoritus(
    actualSuoritus: AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus,
    expectedSuoritusData: schema.AmmatillisenTutkinnonOsittainenSuoritus
  ): Unit = {
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should be(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)

    verifyOsaamisenHankkimistavallinen(actualSuoritus, expectedSuoritusData)
    verifyKoulutussopimuksellinen(actualSuoritus, expectedSuoritusData)
  }

  private def verifyTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus(
    actualSuoritus: AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus,
    expectedSuoritusData: schema.TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus
  ): Unit = {
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should be(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)

    verifyOsaamisenHankkimistavallinen(actualSuoritus, expectedSuoritusData)
    verifyKoulutussopimuksellinen(actualSuoritus, expectedSuoritusData)
  }

  private def verifyMuunAmmatillisenKoulutuksenSuoritus(
    actualSuoritus: AktiivisetJaPäättyneetOpinnotMuunAmmatillisenKoulutuksenSuoritus,
    expectedSuoritusData: schema.MuunAmmatillisenKoulutuksenSuoritus
  ): Unit = {
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should be(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
    actualSuoritus.täydentääTutkintoa.map(_.tunniste.koodiarvo) should be(expectedSuoritusData.täydentääTutkintoa.map(_.tunniste.koodiarvo))

    verifyOsaamisenHankkimistavallinen(actualSuoritus, expectedSuoritusData)
    verifyKoulutussopimuksellinen(actualSuoritus, expectedSuoritusData)
  }

  private def verifyNäyttötutkintoonValmistavanKoulutuksenSuoritus(
    actualSuoritus: AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus,
    expectedSuoritusData: schema.NäyttötutkintoonValmistavanKoulutuksenSuoritus
  ): Unit = {
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should be(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)

    verifyOsaamisenHankkimistavallinen(actualSuoritus, expectedSuoritusData)
    verifyKoulutussopimuksellinen(actualSuoritus, expectedSuoritusData)
  }

  private def verifyTelmaKoulutuksenSuoritus(
    actualSuoritus: AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus,
    expectedSuoritusData: schema.TelmaKoulutuksenSuoritus
  ): Unit = {
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should be(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)

    verifyKoulutussopimuksellinen(actualSuoritus, expectedSuoritusData)
  }

  private def verifyValmaKoulutuksenSuoritus(
    actualSuoritus: AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus,
    expectedSuoritusData: schema.ValmaKoulutuksenSuoritus
  ): Unit = {
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should be(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)

    verifyKoulutussopimuksellinen(actualSuoritus, expectedSuoritusData)
  }

  private def verifyOsaamisenHankkimistavallinen(
    actualSuoritus: AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus,
    expectedSuoritusData: schema.OsaamisenHankkimistavallinen
  ): Unit = {
    actualSuoritus match {
      case actualSuoritus: AktiivisetJaPäättyneetOpinnotOsaamisenHankkimistavallinen =>
        actualSuoritus.osaamisenHankkimistavat.map(_.length) should equal(expectedSuoritusData.osaamisenHankkimistavat.map(_.length))
        actualSuoritus.osaamisenHankkimistavat.map(_.map(_.osaamisenHankkimistapa.tunniste.koodiarvo)) should equal(expectedSuoritusData.osaamisenHankkimistavat.map(_.map(_.osaamisenHankkimistapa.tunniste.koodiarvo)))
        actualSuoritus.osaamisenHankkimistavat.map(_.map(_.alku)) should equal(expectedSuoritusData.osaamisenHankkimistavat.map(_.map(_.alku)))
        actualSuoritus.osaamisenHankkimistavat.map(_.map(_.loppu)) should equal(expectedSuoritusData.osaamisenHankkimistavat.map(_.map(_.loppu)))
    }
  }

  private def verifyKoulutussopimuksellinen(
    actualSuoritus: AktiivisetJaPäättyneetOpinnotAmmatillinenPäätasonSuoritus,
    expectedSuoritusData: schema.Koulutussopimuksellinen
  ): Unit = {
    actualSuoritus.koulutussopimukset.map(_.length) should equal(expectedSuoritusData.koulutussopimukset.map(_.length))
    actualSuoritus.koulutussopimukset.map(_.map(_.alku)) should equal(expectedSuoritusData.koulutussopimukset.map(_.map(_.alku)))
    actualSuoritus.koulutussopimukset.map(_.map(_.loppu)) should equal(expectedSuoritusData.koulutussopimukset.map(_.map(_.loppu)))
    actualSuoritus.koulutussopimukset.map(_.map(_.maa.koodiarvo)) should equal(expectedSuoritusData.koulutussopimukset.map(_.map(_.maa.koodiarvo)))
    actualSuoritus.koulutussopimukset.map(_.map(_.paikkakunta.koodiarvo)) should equal(expectedSuoritusData.koulutussopimukset.map(_.map(_.paikkakunta.koodiarvo)))
  }

  private def verifyAmmatillinenOpiskeluoikeudenKentät(
    actualOo: AktiivisetJaPäättyneetOpinnotAmmatillinenOpiskeluoikeus,
    expectedOoData: schema.AmmatillinenOpiskeluoikeus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualOo.lisätiedot.map(_.osaAikaisuusjaksot.map(_.length)) should equal(expectedOoData.lisätiedot.map(_.osaAikaisuusjaksot.map(_.length)))
    actualOo.lisätiedot.map(_.osaAikaisuusjaksot.map(_.map(_.alku))) should equal(expectedOoData.lisätiedot.map(_.osaAikaisuusjaksot.map(_.map(_.alku))))
    actualOo.lisätiedot.map(_.osaAikaisuusjaksot.map(_.map(_.loppu))) should equal(expectedOoData.lisätiedot.map(_.osaAikaisuusjaksot.map(_.map(_.loppu))))
    actualOo.lisätiedot.map(_.osaAikaisuusjaksot.map(_.map(_.osaAikaisuus))) should equal(expectedOoData.lisätiedot.map(_.osaAikaisuusjaksot.map(_.map(_.osaAikaisuus))))
  }

  private def verifyKorkeakoulu(
    actualOo: AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus,
    actualSuoritus: AktiivisetJaPäättyneetOpinnotKorkeakouluSuoritus,
    expectedOoData: schema.KorkeakoulunOpiskeluoikeus,
    expectedSuoritusData: schema.KorkeakouluSuoritus
  ): Unit = {
    verifyKorkeakouluOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)

    (actualSuoritus, expectedSuoritusData) match {
      case (actualSuoritus: AktiivisetJaPäättyneetOpinnotKorkeakoulututkinnonSuoritus, expectedSuoritusData: schema.KorkeakoulututkinnonSuoritus) =>
        actualSuoritus.suorituskieli.map(_.koodiarvo) should equal(expectedSuoritusData.suorituskieli.map(_.koodiarvo))
        actualSuoritus.koulutusmoduuli.koulutustyyppi should equal(expectedSuoritusData.koulutusmoduuli.koulutustyyppi)
        actualSuoritus.koulutusmoduuli.virtaNimi should equal(expectedSuoritusData.koulutusmoduuli.virtaNimi)
      case (actualSuoritus: AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunSuoritus, expectedSuoritusData: schema.MuuKorkeakoulunSuoritus) =>
        actualSuoritus.koulutusmoduuli.nimi should equal(expectedSuoritusData.koulutusmoduuli.nimi)
      case (actualSuoritus: AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojaksonSuoritus, expectedSuoritusData: schema.KorkeakoulunOpintojaksonSuoritus) =>
        actualSuoritus.koulutusmoduuli.nimi should equal(expectedSuoritusData.koulutusmoduuli.nimi)
      case _ => fail(s"Palautettiin tunnistamattoman tyyppistä suoritusdataa actual: (${actualSuoritus.getClass.getName}), expected:(${expectedSuoritusData.getClass.getName})")
    }
  }

  private def verifyPäätasonSuoritus(actualSuoritus: AktiivisetJaPäättyneetOpinnotPäätasonSuoritus, expectedSuoritusData: schema.PäätasonSuoritus) = {
    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
    expectedSuoritusData match {
      case es: schema.Suorituskielellinen => actualSuoritus.suorituskieli.koodiarvo should equal(es.suorituskieli.koodiarvo)
      case _ => fail(s"Yritettiin tutkita suorituskieletöntä päätason suoritustyyppiä: ${expectedSuoritusData.tyyppi.koodiarvo}")
    }
  }

  private def verifyKorkeakouluOpiskeluoikeudenKentät(
    actualOo: AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeus,
    expectedOoData: schema.KorkeakoulunOpiskeluoikeus
  ): Unit = {
    verifyOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualOo.luokittelu.map(_.length) should equal(expectedOoData.luokittelu.map(_.length))
    actualOo.luokittelu.map(_.map(_.koodiarvo)) should equal(expectedOoData.luokittelu.map(_.map(_.koodiarvo)))

    actualOo.suoritukset.length should equal(expectedOoData.suoritukset.length)

    actualOo.lisätiedot.map(_.virtaOpiskeluoikeudenTyyppi.map(_.koodiarvo)) should equal(expectedOoData.lisätiedot.map(_.virtaOpiskeluoikeudenTyyppi.map(_.koodiarvo)))
    actualOo.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.length)) should equal(expectedOoData.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.length)))
    actualOo.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.alku))) should equal(expectedOoData.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.alku))))
    actualOo.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.loppu))) should equal(expectedOoData.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.loppu))))
    actualOo.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.tila.koodiarvo))) should equal(expectedOoData.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.tila.koodiarvo))))
    actualOo.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.maksetutLukuvuosimaksut.map(_.maksettu)))) should equal(expectedOoData.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.maksetutLukuvuosimaksut.map(_.maksettu)))))
    actualOo.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.maksetutLukuvuosimaksut.map(_.summa)))) should equal(expectedOoData.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.maksetutLukuvuosimaksut.map(_.summa)))))
    actualOo.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.maksetutLukuvuosimaksut.map(_.apuraha)))) should equal(expectedOoData.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.maksetutLukuvuosimaksut.map(_.apuraha)))))
    actualOo.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.ylioppilaskunnanJäsen))) should equal(expectedOoData.lisätiedot.map(_.lukukausiIlmoittautuminen.map(_.ilmoittautumisjaksot.map(_.ylioppilaskunnanJäsen))))
    actualOo.lisätiedot.map(_.koulutuskuntaJaksot.map(_.alku)) should equal(expectedOoData.lisätiedot.map(_.koulutuskuntaJaksot.map(_.alku)))
    actualOo.lisätiedot.map(_.koulutuskuntaJaksot.map(_.loppu)) should equal(expectedOoData.lisätiedot.map(_.koulutuskuntaJaksot.map(_.loppu)))
    actualOo.lisätiedot.map(_.koulutuskuntaJaksot.map(_.koulutuskunta)) should equal(expectedOoData.lisätiedot.map(_.koulutuskuntaJaksot.map(_.koulutuskunta)))
  }

  private def verifyKoskiOpiskeluoikeudenKentät(
    actualOo: AktiivisetJaPäättyneetOpinnotKoskeenTallennettavaOpiskeluoikeus,
    expectedOoData: schema.KoskeenTallennettavaOpiskeluoikeus
  ): Unit = {
    verifyOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualOo.oid should be(expectedOoData.oid)
    actualOo.sisältyyOpiskeluoikeuteen.map(_.oid) should equal(None)
    actualOo.versionumero should be(expectedOoData.versionumero)

    actualOo.tila.opiskeluoikeusjaksot.zip(expectedOoData.tila.opiskeluoikeusjaksot).foreach {
      case (actual, expected: schema.KoskiOpiskeluoikeusjakso) =>
        actual.opintojenRahoitus.map(_.koodiarvo) should equal(expected.opintojenRahoitus.map(_.koodiarvo))
      case (actual, _) =>
        actual.opintojenRahoitus should equal(None)
    }
  }

  private def verifyOpiskeluoikeudenKentät(
    actualOo: AktiivisetJaPäättyneetOpinnotOpiskeluoikeus,
    expectedOoData: schema.Opiskeluoikeus
  ): Unit = {
    actualOo.oppilaitos.map(_.oid) should equal(expectedOoData.oppilaitos.map(_.oid))
    actualOo.koulutustoimija.map(_.oid) should equal(expectedOoData.koulutustoimija.map(_.oid))
    actualOo.tyyppi.koodiarvo should equal(expectedOoData.tyyppi.koodiarvo)

    actualOo.alkamispäivä should equal(expectedOoData.alkamispäivä)
    actualOo.päättymispäivä should equal(expectedOoData.päättymispäivä)

    actualOo.tila.opiskeluoikeusjaksot.length should equal(expectedOoData.tila.opiskeluoikeusjaksot.length)
    actualOo.tila.opiskeluoikeusjaksot.map(_.tila.koodiarvo) should equal(expectedOoData.tila.opiskeluoikeusjaksot.map(_.tila.koodiarvo))
    actualOo.tila.opiskeluoikeusjaksot.map(_.alku) should equal(expectedOoData.tila.opiskeluoikeusjaksot.map(_.alku))
  }

}

