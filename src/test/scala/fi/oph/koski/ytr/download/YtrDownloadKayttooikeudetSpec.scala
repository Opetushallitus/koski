package fi.oph.koski.ytr.download

import fi.oph.koski.api.misc.OpiskeluoikeusTestMethods
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
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
    with YtrDownloadTestMethods
{

  private val converter = new YtrDownloadOppijaConverter(
    KoskiApplicationForTests.koodistoViitePalvelu,
    KoskiApplicationForTests.organisaatioRepository,
    KoskiApplicationForTests.koskiLocalizationRepository,
    KoskiApplicationForTests.validatingAndResolvingExtractor
  )

  val birthmonthStart = "1980-03"
  val birthmonthEnd = "1981-10"

  private val hetu = "140380-336X"

  lazy val oppijaOid =
    KoskiApplicationForTests.opintopolkuHenkilöFacade.findOppijaByHetu(hetu).get.oid

  private lazy val ytrOppijat = MockYrtClient.oppijatByHetut(YtrSsnData(ssns = Some(List("080380-2432", "140380-336X", "220680-7850", "240680-087S", "060807A7787", "300805A756F"))))
  private lazy val ytrOppija = ytrOppijat.find(_.ssn == hetu).get

  private lazy val henkilö = UusiHenkilö(
    hetu = ytrOppija.ssn,
    etunimet = ytrOppija.firstNames.get,
    sukunimi = ytrOppija.lastName.get,
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

  "Pääkäyttäjä pystyy lukemaan" - {

    "Jsonia" in {
      downloadYtrData(birthmonthStart, birthmonthEnd, force = true)

      authGet("api/oppija/" + oppijaOid + "/ytr-json", MockUsers.paakayttaja) {
        verifyResponseStatusOk()
      }
    }

    "Versioitua jsonia" in {
      downloadYtrData(birthmonthStart, birthmonthEnd, force = true)

      authGet("api/oppija/" + oppijaOid + "/ytr-json/" + 1, MockUsers.paakayttaja) {
        verifyResponseStatusOk()
      }
    }

    "Tallennettua alkuperäistä jsonia" in {
      downloadYtrData(birthmonthStart, birthmonthEnd, force = true)

      authGet("api/oppija/" + oppijaOid + "/ytr-saved-original-json", MockUsers.paakayttaja) {
        verifyResponseStatusOk()
      }
    }

    "YTR:stä haettua alkuperäistä jsonia" in {
      downloadYtrData(birthmonthStart, birthmonthEnd, force = true)

      authGet("api/oppija/" + oppijaOid + "/ytr-current-original-json", MockUsers.paakayttaja) {
        verifyResponseStatusOk()
      }
    }
  }

  "OPH-katselija pystyy lukemaan" - {

    "Jsonia" in {
      downloadYtrData(birthmonthStart, birthmonthEnd, force = true)

      authGet("api/oppija/" + oppijaOid + "/ytr-json", MockUsers.ophkatselija) {
        verifyResponseStatusOk()
      }
    }

    "Versioitua jsonia" in {
      downloadYtrData(birthmonthStart, birthmonthEnd, force = true)

      authGet("api/oppija/" + oppijaOid + "/ytr-json/" + 1, MockUsers.ophkatselija) {
        verifyResponseStatusOk()
      }
    }

    "Tallennettua alkuperäistä jsonia" in {
      downloadYtrData(birthmonthStart, birthmonthEnd, force = true)

      authGet("api/oppija/" + oppijaOid + "/ytr-saved-original-json", MockUsers.ophkatselija) {
        verifyResponseStatusOk()
      }
    }

    "YTR:stä haettua alkuperäistä jsonia" in {
      downloadYtrData(birthmonthStart, birthmonthEnd, force = true)

      authGet("api/oppija/" + oppijaOid + "/ytr-current-original-json", MockUsers.ophkatselija) {
        verifyResponseStatusOk()
      }
    }
  }

  "Viranomaiskatselija ei pysty lukemaan" - {

    "Jsonia" in {
      downloadYtrData(birthmonthStart, birthmonthEnd, force = true)

      authGet("api/oppija/" + oppijaOid + "/ytr-json", MockUsers.viranomainen) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus())
      }
    }

    "Versioitua jsonia" in {
      downloadYtrData(birthmonthStart, birthmonthEnd, force = true)

      authGet("api/oppija/" + oppijaOid + "/ytr-json/" + 1, MockUsers.viranomainen) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus())
      }
    }

    "Tallennettua alkuperäistä jsonia" in {
      downloadYtrData(birthmonthStart, birthmonthEnd, force = true)

      authGet("api/oppija/" + oppijaOid + "/ytr-saved-original-json", MockUsers.viranomainen) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus())
      }
    }

    "YTR:stä haettua alkuperäistä jsonia" in {
      downloadYtrData(birthmonthStart, birthmonthEnd, force = true)

      authGet("api/oppija/" + oppijaOid + "/ytr-current-original-json", MockUsers.viranomainen) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus())
      }
    }
  }

  "Tavallinen käyttäjä ei pysty lukemaan" - {

    "Jsonia" in {
      downloadYtrData(birthmonthStart, birthmonthEnd, force = true)

      authGet("api/oppija/" + oppijaOid + "/ytr-json", MockUsers.kalle) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus())
      }
    }

    "Versioitua jsonia" in {
      downloadYtrData(birthmonthStart, birthmonthEnd, force = true)

      authGet("api/oppija/" + oppijaOid + "/ytr-json/" + 1, MockUsers.kalle) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus())
      }
    }

    "Tallennettua alkuperäistä jsonia" in {
      downloadYtrData(birthmonthStart, birthmonthEnd, force = true)

      authGet("api/oppija/" + oppijaOid + "/ytr-saved-original-json", MockUsers.kalle) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus())
      }
    }

    "YTR:stä haettua alkuperäistä jsonia" in {
      downloadYtrData(birthmonthStart, birthmonthEnd, force = true)

      authGet("api/oppija/" + oppijaOid + "/ytr-current-original-json", MockUsers.kalle) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus())
      }
    }
  }

}
