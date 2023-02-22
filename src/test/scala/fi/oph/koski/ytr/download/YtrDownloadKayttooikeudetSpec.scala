package fi.oph.koski.ytr.download

import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.koski.api.OpiskeluoikeusTestMethods
import fi.oph.koski.http.{ErrorDetail, HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession, MockUsers, UserWithPassword}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema.{Oppija, UusiHenkilö, YlioppilastutkinnonOpiskeluoikeus}
import fi.oph.koski.ytr.MockYrtClient
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class YtrDownloadKäyttöoikeudetSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with Matchers
    with BeforeAndAfterEach
{

  private val converter = new YtrDownloadOppijaConverter(
    KoskiApplicationForTests.koodistoViitePalvelu,
    KoskiApplicationForTests.organisaatioRepository,
    KoskiApplicationForTests.koskiLocalizationRepository
  )

  private val hetu = "140380-336X"

  private lazy val ytrOppijat = MockYrtClient.oppijatByHetut(YtrSsnData(ssns = Some(List("080380-2432", "140380-336X", "220680-7850", "240680-087S"))))
  private lazy val ytrOppija = ytrOppijat.find(_.ssn == hetu).get

  private lazy val henkilö = UusiHenkilö(
    hetu = ytrOppija.ssn,
    etunimet = ytrOppija.firstNames,
    sukunimi = ytrOppija.lastName,
    kutsumanimi = None
  )
  private lazy val opiskeluoikeus = converter.convertOppijastaOpiskeluoikeus(ytrOppija).get

  "Tavallinen systemUser ei pysty tallentamaan" in {
    implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
    implicit val accessType: AccessType.Value = AccessType.write

    val result = KoskiApplicationForTests.ytrDownloadService.createOrUpdate(henkilö, opiskeluoikeus)

    result should be(Left(KoskiErrorCategory.notImplemented.readOnly("Korkeakoulutuksen opiskeluoikeuksia ja ylioppilastutkintojen tietoja ei voi päivittää Koski-järjestelmässä")))
  }

  "Tavallinen käyttäjä ei pysty tallentamaan" in {
    implicit val session: KoskiSpecificSession = MockUsers.kalle.toKoskiSpecificSession(KoskiApplicationForTests.käyttöoikeusRepository)
    implicit val accessType: AccessType.Value = AccessType.write

    val result = KoskiApplicationForTests.ytrDownloadService.createOrUpdate(henkilö, opiskeluoikeus)

    result should be(Left(KoskiErrorCategory.notImplemented.readOnly("Korkeakoulutuksen opiskeluoikeuksia ja ylioppilastutkintojen tietoja ei voi päivittää Koski-järjestelmässä")))
  }
}
