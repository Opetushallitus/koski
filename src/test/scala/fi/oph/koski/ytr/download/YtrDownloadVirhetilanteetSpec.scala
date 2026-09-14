package fi.oph.koski.ytr.download

import fi.oph.koski.api.misc.OpiskeluoikeusTestMethods
import fi.oph.koski.{DatabaseTestMethods, KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.koski.db.KoskiTables.YtrOpiskeluOikeudet
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.YtrOpiskeluoikeusRow
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, VerifiedHenkilöOid}
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.ytr.YtrSsnWithPreviousSsns
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class YtrDownloadVirhetilanteetSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with Matchers
    with YtrDownloadTestMethods
    with OpiskeluoikeusTestMethods
    with DatabaseTestMethods
    with BeforeAndAfterEach
{

  override protected def beforeEach(): Unit = {
    super.beforeEach()
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    // Testit rikkovat tietokannan eheyttä, joten on turvallisinta resetoida kanta aina testien jälkeen
    resetFixtures()
  }

  "YTR download mitätöi duplikaatin, jos samalla oppijalla on ennestään useampi YTR-opiskeluoikeus" in {
    // Näin saattaa tapahtua, jos oppijoita yhdistetään oppijanumerorekisterissä vasta sen jälkeen, kun kummallekin
    // oppijanumerolle on ehditty tallentaa YTR-opiskeluoikeus

    clearYtrData()

    val opiskeluoikeus = luoYtrTestiOpiskeluoikeus()

    implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUserTallennetutYlioppilastutkinnonOpiskeluoikeudet
    implicit val accessType: AccessType.Value = AccessType.write

    // Lisää YTR-opiskeluoikeus kahdelle samaan master-oppijaan liitetylle oppijalle ohi normaalin prosessin
    val masterinTallennus = KoskiApplicationForTests.ytrPossu
      .createOrUpdate(VerifiedHenkilöOid(KoskiSpecificMockOppijat.master), opiskeluoikeus)
    masterinTallennus.isRight should be(true)
    val slavenTallennus = KoskiApplicationForTests.ytrPossu
      .actions.createOpiskeluoikeusBypassingUpdateCheckForTests(KoskiSpecificMockOppijat.slave, opiskeluoikeus)
    slavenTallennus.isRight should be(true)

    // Koita tehdä download, jossa tulee uusi YTR-opiskeluikeus kyseisen oppijan hetulla
    downloadYtrData("1997-10", "1997-11", force = true)

    // Varmista, että lataus onnistui: duplikaatti on mitätöity eikä virhettä syntynyt
    verifyDownloadCounts(expectedTotalCount = 1, expectedErrorCount = 0)

    val rivit = ytrOpiskeluoikeusRivit(
      KoskiSpecificMockOppijat.master.oid,
      KoskiSpecificMockOppijat.slave.henkilö.oid
    )

    // Lataus ratkeaa master-oppijaan, joten hänen rivinsä säilyy ja slaven rivi mitätöidään
    rivit.filterNot(_.mitätöity).map(rivi => (rivi.oid, rivi.oppijaOid)) should equal(List(
      (masterinTallennus.toOption.get.oid, KoskiSpecificMockOppijat.master.oid)
    ))
    rivit.filter(_.mitätöity).map(rivi => (rivi.oid, rivi.oppijaOid)) should equal(List(
      (slavenTallennus.toOption.get.oid, KoskiSpecificMockOppijat.slave.henkilö.oid)
    ))
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
      KoskiApplicationForTests.ytrClient.oppijatByHetut(YtrSsnDataWithPreviousSsns(Some(List("080380-2432").map(ssn => YtrSsnWithPreviousSsns(ssn)))))
        .head
    val opiskeluoikeus =
      oppijaConverter.convertOppijastaOpiskeluoikeus(laajaOppija).head
    opiskeluoikeus
  }

  private def ytrOpiskeluoikeusRivit(oppijaOidit: String*): List[YtrOpiskeluoikeusRow] =
    runDbSync(YtrOpiskeluOikeudet.filter(_.oppijaOid inSetBind oppijaOidit).sortBy(_.id).result).toList

  private def verifyDownloadCounts(
    expectedTotalCount: Int,
    expectedErrorCount: Int
  ) = {
    totalCount should be(expectedTotalCount)
    errorCount should be(expectedErrorCount)
  }
}
