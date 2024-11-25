package fi.oph.koski.suoritusjako.suoritetuttutkinnot

import fi.oph.koski.schema
import org.scalatest.matchers.should.Matchers

trait SuoritetutTutkinnotVerifiers extends Matchers
{
  protected def verifyOpiskeluoikeusJaSuoritus(
    actualOo: SuoritetutTutkinnotOpiskeluoikeus,
    actualSuoritus: Suoritus,
    expectedOoData: schema.Opiskeluoikeus,
    expectedSuoritusData: schema.Suoritus
  ): Unit = {
    (actualOo, actualSuoritus, expectedOoData, expectedSuoritusData) match {
      case (
        actualOo: SuoritetutTutkinnotAmmatillinenOpiskeluoikeus,
        actualSuoritus: SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus,
        expectedOoData: schema.AmmatillinenOpiskeluoikeus,
        expectedSuoritusData: schema.AmmatillisenTutkinnonSuoritus
        ) => verifyAmmatillinenTutkinto(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
      case (
        actualOo: SuoritetutTutkinnotAmmatillinenOpiskeluoikeus,
        actualSuoritus: SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus,
        expectedOoData: schema.AmmatillinenOpiskeluoikeus,
        expectedSuoritusData: schema.AmmatillisenTutkinnonOsittainenSuoritus
        ) => verifyAmmatillinenOsittainen(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
      case (
        actualOo: SuoritetutTutkinnotAmmatillinenOpiskeluoikeus,
        actualSuoritus: SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus,
        expectedOoData: schema.AmmatillinenOpiskeluoikeus,
        expectedSuoritusData: schema.MuunAmmatillisenKoulutuksenSuoritus
        ) => verifyMuuAmmatillinen(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
      case (
        actualOo: SuoritetutTutkinnotEBTutkinnonOpiskeluoikeus,
        actualSuoritus: SuoritetutTutkinnotEBTutkinnonSuoritus,
        expectedOoData: schema.EBOpiskeluoikeus,
        expectedSuoritusData: schema.EBTutkinnonSuoritus
        ) => verifyEBTutkinto(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
      case (
        actualOo: SuoritetutTutkinnotDIAOpiskeluoikeus,
        actualSuoritus: SuoritetutTutkinnotDIATutkinnonSuoritus,
        expectedOoData: schema.DIAOpiskeluoikeus,
        expectedSuoritusData: schema.DIATutkinnonSuoritus
        ) => verifyDIATutkinto(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
      case (
        actualOo: SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus,
        actualSuoritus: SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus,
        expectedOoData: schema.YlioppilastutkinnonOpiskeluoikeus,
        expectedSuoritusData: schema.YlioppilastutkinnonSuoritus
        ) => verifyYlioppilastutkinto(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
      case (
        actualOo: SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus,
        actualSuoritus: SuoritetutTutkinnotKorkeakoulututkinnonSuoritus,
        expectedOoData: schema.KorkeakoulunOpiskeluoikeus,
        expectedSuoritusData: schema.KorkeakoulututkinnonSuoritus
        ) => verifyKorkeakoulututkinto(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
      case _ => fail("Palautettiin tunnistamattoman tyyppistä dataa")
    }
  }

  private def verifyAmmatillinenTutkinto(
    actualOo: SuoritetutTutkinnotAmmatillinenOpiskeluoikeus,
    actualSuoritus: SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus,
    expectedOoData: schema.AmmatillinenOpiskeluoikeus,
    expectedSuoritusData: schema.AmmatillisenTutkinnonSuoritus
  ): Unit = {
    verifyAmmatillinenOpiskeluoikeudenKentät(actualOo, expectedOoData)
    verifyAmmatillisenTutkinnonOsittainenTaiKokoSuoritus(actualSuoritus, expectedSuoritusData)

    actualSuoritus.osaamisala.map(_.length) should equal(expectedSuoritusData.osaamisala.map(_.length))
    actualSuoritus.tutkintonimike.map(_.length) should equal(expectedSuoritusData.tutkintonimike.map(_.length))
  }

  private def verifyAmmatillinenOsittainen(
    actualOo: SuoritetutTutkinnotAmmatillinenOpiskeluoikeus,
    actualSuoritus: SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus,
    expectedOoData: schema.AmmatillinenOpiskeluoikeus,
    expectedSuoritusData: schema.AmmatillisenTutkinnonOsittainenSuoritus
  ): Unit = {
    verifyAmmatillinenOpiskeluoikeudenKentät(actualOo, expectedOoData)
    verifyAmmatillisenTutkinnonOsittainenTaiKokoSuoritus(actualSuoritus, expectedSuoritusData)

    actualSuoritus.toinenOsaamisala should equal(Some(expectedSuoritusData.toinenOsaamisala))
    actualSuoritus.toinenTutkintonimike should equal(Some(expectedSuoritusData.toinenTutkintonimike))
    actualSuoritus.korotettuOpiskeluoikeusOid should equal(expectedSuoritusData.korotettuOpiskeluoikeusOid)

    actualSuoritus.osaamisala.map(_.length) should equal(
      if (expectedSuoritusData.toinenOsaamisala) {
        expectedSuoritusData.osaamisala.map(_.length)
      } else {
        None
      }
    )
    actualSuoritus.tutkintonimike.map(_.length) should equal(
      if (expectedSuoritusData.toinenTutkintonimike) {
        expectedSuoritusData.tutkintonimike.map(_.length)
      } else {
        None
      }
    )
  }

  private def verifyMuuAmmatillinen(
    actualOo: SuoritetutTutkinnotAmmatillinenOpiskeluoikeus,
    actualSuoritus: SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus,
    expectedOoData: schema.AmmatillinenOpiskeluoikeus,
    expectedSuoritusData: schema.MuunAmmatillisenKoulutuksenSuoritus
  ): Unit = {
    verifyAmmatillinenOpiskeluoikeudenKentät(actualOo, expectedOoData)
    verifyMuunAmmatillisenTutkinnonSuoritus(actualSuoritus, expectedSuoritusData)
  }

  private def verifyAmmatillisenTutkinnonOsittainenTaiKokoSuoritus(
    actualSuoritus: SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenTaiKokoSuoritus,
    expectedSuoritusData: schema.AmmatillisenTutkinnonOsittainenTaiKokoSuoritus
  ): Unit = {
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)

    actualSuoritus.koulutusmoduuli.perusteenDiaarinumero should equal(expectedSuoritusData.koulutusmoduuli.perusteenDiaarinumero)
    actualSuoritus.koulutusmoduuli.perusteenNimi should equal(expectedSuoritusData.koulutusmoduuli.perusteenNimi)
    actualSuoritus.koulutusmoduuli.koulutustyyppi.map(_.koodiarvo) should equal(expectedSuoritusData.koulutusmoduuli.koulutustyyppi.map(_.koodiarvo))

    actualSuoritus.toimipiste.map(_.oid) should equal(Some(expectedSuoritusData.toimipiste.oid))
    actualSuoritus.vahvistus.map(_.päivä) should equal(expectedSuoritusData.vahvistus.map(_.päivä))
    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.suorituskieli.map(_.koodiarvo) should equal(Some(expectedSuoritusData.suorituskieli.koodiarvo))
  }

  private def verifyMuunAmmatillisenTutkinnonSuoritus(
    actualSuoritus: SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus,
    expectedSuoritusData: schema.MuunAmmatillisenKoulutuksenSuoritus
  ): Unit = {
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)

    actualSuoritus.koulutusmoduuli.laajuus.map(_.arvo) should equal(expectedSuoritusData.koulutusmoduuli.laajuus.map(_.arvo))
    actualSuoritus.koulutusmoduuli.laajuus.map(_.yksikkö.koodiarvo) should equal(expectedSuoritusData.koulutusmoduuli.laajuus.map(_.yksikkö.koodiarvo))
    actualSuoritus.koulutusmoduuli.laajuus.map(_.yksikkö.koodistoUri) should equal(expectedSuoritusData.koulutusmoduuli.laajuus.map(_.yksikkö.koodistoUri))
    actualSuoritus.toimipiste.map(_.oid) should equal(Some(expectedSuoritusData.toimipiste.oid))
    actualSuoritus.vahvistus.map(_.päivä) should equal(expectedSuoritusData.vahvistus.map(_.päivä))
    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.suorituskieli.map(_.koodiarvo) should equal(Some(expectedSuoritusData.suorituskieli.koodiarvo))
  }

  private def verifyAmmatillinenOpiskeluoikeudenKentät(
    actualOo: SuoritetutTutkinnotAmmatillinenOpiskeluoikeus,
    expectedOoData: schema.AmmatillinenOpiskeluoikeus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)
    actualOo.suoritukset.length should equal(1)
  }

  private def verifyYlioppilastutkinto(
    actualOo: SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus,
    actualSuoritus: SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus,
    expectedOoData: schema.YlioppilastutkinnonOpiskeluoikeus,
    expectedSuoritusData: schema.YlioppilastutkinnonSuoritus
  ): Unit = {
    verifyOpiskeluoikeudenKentät(actualOo, expectedOoData)
    actualOo.suoritukset.length should equal(1)

    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
    actualSuoritus.toimipiste.map(_.oid) should equal(Some(expectedSuoritusData.toimipiste.oid))
    actualSuoritus.vahvistus.map(_.päivä) should equal(expectedSuoritusData.vahvistus.map(_.päivä))
    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
  }

  private def verifyKorkeakoulututkinto(
    actualOo: SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus,
    actualSuoritus: SuoritetutTutkinnotKorkeakoulututkinnonSuoritus,
    expectedOoData: schema.KorkeakoulunOpiskeluoikeus,
    expectedSuoritusData: schema.KorkeakoulututkinnonSuoritus
  ): Unit = {
    verifyOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualOo.lisätiedot.map(_.virtaOpiskeluoikeudenTyyppi.map(_.koodiarvo)) should be(expectedOoData.lisätiedot.map(_.virtaOpiskeluoikeudenTyyppi.map(_.koodiarvo)))

    actualOo.suoritukset.length should equal(1)

    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
    actualSuoritus.koulutusmoduuli.koulutustyyppi.map(_.koodiarvo) should equal(expectedSuoritusData.koulutusmoduuli.koulutustyyppi.map(_.koodiarvo))
    actualSuoritus.koulutusmoduuli.virtaNimi should equal(expectedSuoritusData.koulutusmoduuli.virtaNimi)
    actualSuoritus.toimipiste.map(_.oid) should equal(Some(expectedSuoritusData.toimipiste.oid))
    actualSuoritus.vahvistus.map(_.päivä) should equal(expectedSuoritusData.vahvistus.map(_.päivä))
    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
  }

  private def verifyEBTutkinto(
    actualOo: SuoritetutTutkinnotEBTutkinnonOpiskeluoikeus,
    actualSuoritus: SuoritetutTutkinnotEBTutkinnonSuoritus,
    expectedOoData: schema.EBOpiskeluoikeus,
    expectedSuoritusData: schema.EBTutkinnonSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)
    actualOo.suoritukset.length should equal(1)

    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
    actualSuoritus.koulutusmoduuli.curriculum.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.curriculum.koodiarvo)
    actualSuoritus.toimipiste.map(_.oid) should equal(Some(expectedSuoritusData.toimipiste.oid))
    actualSuoritus.vahvistus.map(_.päivä) should equal(expectedSuoritusData.vahvistus.map(_.päivä))
    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
  }

  private def verifyDIATutkinto(
    actualOo: SuoritetutTutkinnotDIAOpiskeluoikeus,
    actualSuoritus: SuoritetutTutkinnotDIATutkinnonSuoritus,
    expectedOoData: schema.DIAOpiskeluoikeus,
    expectedSuoritusData: schema.DIATutkinnonSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)
    actualOo.suoritukset.length should equal(1)

    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
    actualSuoritus.toimipiste.map(_.oid) should equal(Some(expectedSuoritusData.toimipiste.oid))
    actualSuoritus.vahvistus.map(_.päivä) should equal(expectedSuoritusData.vahvistus.map(_.päivä))
    actualSuoritus.suorituskieli.map(_.koodiarvo) should equal(Some(expectedSuoritusData.suorituskieli.koodiarvo))
    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
  }

  private def verifyKoskiOpiskeluoikeudenKentät(
    actualOo: SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus,
    expectedOoData: schema.KoskeenTallennettavaOpiskeluoikeus
  ): Unit = {
    verifyOpiskeluoikeudenKentät(actualOo, expectedOoData)
    actualOo.oid should be(expectedOoData.oid)
    actualOo.versionumero should be(expectedOoData.versionumero)
    actualOo.sisältyyOpiskeluoikeuteen.map(_.oid) should equal(None)
  }

  private def verifyOpiskeluoikeudenKentät(
    actualOo: SuoritetutTutkinnotOpiskeluoikeus,
    expectedOoData: schema.Opiskeluoikeus
  ): Unit = {
    actualOo.oppilaitos.map(_.oid) should equal(expectedOoData.oppilaitos.map(_.oid))
    actualOo.koulutustoimija.map(_.oid) should equal(expectedOoData.koulutustoimija.map(_.oid))
    actualOo.tyyppi.koodiarvo should equal(expectedOoData.tyyppi.koodiarvo)
  }

}
