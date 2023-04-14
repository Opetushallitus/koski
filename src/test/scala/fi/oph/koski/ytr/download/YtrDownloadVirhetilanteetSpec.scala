package fi.oph.koski.ytr.download

import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.koski.api.OpiskeluoikeusTestMethods
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, VerifiedHenkilöOid}
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class YtrDownloadVirhetilanteetSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with Matchers
    with YtrDownloadTestMethods
    with OpiskeluoikeusTestMethods
    with BeforeAndAfterEach
{

  override protected def beforeEach() {
    super.beforeEach()
  }

  override protected def afterEach() {
    super.afterEach()
    // Testit rikkovat tietokannan eheyttä, joten on turvallisinta resetoida kanta aina testien jälkeen
    resetFixtures()
  }

  "YTR download huomaa konsistentisti tilanteen, missä samalla oppijalla on useampi YTR-opiskeluoikeus ennestään" in {
    // Näin saattaa tapahtua, jos oppijoita yhdistetään oppijanumerorekisterissä

    clearYtrData()

    val opiskeluoikeus = luoYtrTestiOpiskeluoikeus()

    implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUserTallennetutYlioppilastutkinnonOpiskeluoikeudet
    implicit val accessType: AccessType.Value = AccessType.write

    // Lisää YTR-opiskeluoikeus kahdelle samaan master-oppijaan liitetylle oppijalle ohi normaalin prosessin
    KoskiApplicationForTests.ytrPossu
      .createOrUpdate(VerifiedHenkilöOid(KoskiSpecificMockOppijat.master), opiskeluoikeus)
      .isRight should be(true)
    KoskiApplicationForTests.ytrPossu
      .actions.createOpiskeluoikeusBypassingUpdateCheckForTests(KoskiSpecificMockOppijat.slave, opiskeluoikeus)
      .isRight should be(true)

    // Koita tehdä download, jossa tulee uusi YTR-opiskeluikeus kyseisen oppijan hetulla
    downloadYtrData("1997-10", "1997-11", force = true)

    // Varmista, että tästä syntyi virhe
    verifyDownloadCounts(expectedTotalCount = 1, expectedErrorCount = 1)
  }

  "YTR download selviää virheellisistä hetuista" in {
    clearYtrData()
    downloadYtrData("2023-02", "2023-03", force = true)
    verifyDownloadCounts(expectedTotalCount = 0, expectedErrorCount = 0)
    downloadYtrData("2023-03", "2023-04", force = true)
    verifyDownloadCounts(expectedTotalCount = 0, expectedErrorCount = 0)
  }

  "YTR download selviää puuttuvista etu- ja sukunimistä" in {
    clearYtrData()
    downloadYtrData("2022-01", "2022-02", force = true)
    verifyDownloadCounts(expectedTotalCount = 0, expectedErrorCount = 0)
  }

  "YTR download luo uuden oidin, jos tulee duplikaatti" in {
    clearYtrData()

    val opiskeluoikeus = luoYtrTestiOpiskeluoikeus()

    implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUserTallennetutYlioppilastutkinnonOpiskeluoikeudet
    implicit val accessType: AccessType.Value = AccessType.write

    KoskiApplicationForTests.ytrPossu
      .createOrUpdate(VerifiedHenkilöOid(KoskiSpecificMockOppijat.opiskeluoikeudenOidKonflikti), opiskeluoikeus)
      .isRight should be(true)
    KoskiApplicationForTests.ytrPossu
      .createOrUpdate(VerifiedHenkilöOid(KoskiSpecificMockOppijat.opiskeluoikeudenOidKonflikti2), opiskeluoikeus)
      .isRight should be(true)
  }

  private def luoYtrTestiOpiskeluoikeus() = {
    val oppijaConverter = new YtrDownloadOppijaConverter(
      KoskiApplicationForTests.koodistoViitePalvelu,
      KoskiApplicationForTests.organisaatioRepository,
      KoskiApplicationForTests.koskiLocalizationRepository,
      KoskiApplicationForTests.validatingAndResolvingExtractor
    )

    val laajaOppija =
      KoskiApplicationForTests.ytrClient.oppijatByHetut(YtrSsnData(Some(List("080380-2432"))))
        .head
    val opiskeluoikeus =
      oppijaConverter.convertOppijastaOpiskeluoikeus(laajaOppija).head
    opiskeluoikeus
  }

  private def verifyDownloadCounts(
    expectedTotalCount: Int,
    expectedErrorCount: Int
  ) = {
    totalCount should be(expectedTotalCount)
    errorCount should be(expectedErrorCount)
  }
}
