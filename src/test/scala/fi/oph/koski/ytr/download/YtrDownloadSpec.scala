package fi.oph.koski.ytr.download

import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.koski.api.OpiskeluoikeusTestMethods
import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema.Oppija
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class YtrDownloadSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with Matchers
    with YtrDownloadTestMethods
    with OpiskeluoikeusTestMethods
    with BeforeAndAfterEach
{

  override protected def beforeEach() {
    super.beforeEach()
    AuditLogTester.clearMessages
  }

  val birthmonthStart = "1980-03"
  val birthmonthEnd = "1981-10"

  val modifiedSince = LocalDate.of(2023, 1, 1)

  val oppijahetut = List(
    "080380-2432",
    "140380-336X",
    "220680-7850",
    "240680-087S"
  )

  lazy val oppijaOidEnnestäänKoskessa1 =
    KoskiApplicationForTests.opintopolkuHenkilöFacade.findOppijaByHetu(oppijahetut(0)).get.oid

  "Käyttöoikeudet" - {

    "OPH:n pääkäyttäjä ei voi ladata YTR:stä ladattua opiskeluoikeutta ennenkuin niitä on tallennettu" in {
      clearYtrData()
      authGet("api/oppija/" + oppijaOidEnnestäänKoskessa1 + "/ytr-json", MockUsers.paakayttaja) {
        verifyResponseStatus(404)
      }
    }

    "OPH:n pääkäyttäjä voi ladata YTR:stä ladatun opiskeluoikeuden" in {
      downloadYtrData(birthmonthStart, birthmonthEnd, force = true)
      getYtrOppija(oppijaOidEnnestäänKoskessa1, MockUsers.paakayttaja)
    }

    "Oppilaitoksen käyttäjä ei voi ladata YTR:stä ladattua opiskeluoikeutta" in {
      downloadYtrData(birthmonthStart, birthmonthEnd, force = true)
      authGet("api/oppija/" + oppijaOidEnnestäänKoskessa1 + "/ytr-json", MockUsers.kalle) {
        verifyResponseStatus(403)
      }
    }

    "Palvelukäyttäjä ei voi ladata YTR:stä ladattua opiskeluoikeutta" in {
      downloadYtrData(birthmonthStart, birthmonthEnd, force = true)
      authGet("api/oppija/" + oppijaOidEnnestäänKoskessa1 + "/ytr-json", MockUsers.kahdenOrganisaatioPalvelukäyttäjä) {
        verifyResponseStatus(403)
      }
    }
  }

  "Audit-logit" - {
    // TODO: TOR-1639 lisää testit ja toteutus
  }

  "YTR:stä ladattu opiskeluoikeus tallennetaan oikein, vaikka kaikki oppijat eivät vielä löytyisi Koskesta" in {
    // Täysi reset tarvitaan, jotta vain YTR-datassa esiintyvien oppijoiden tiedot eivät ole jo Koskessa esim. aiempien testien takia
    resetFixtures()

    downloadYtrData(birthmonthStart, birthmonthEnd, force = true)

    verifyOppijat(expectedOppijat())
  }

  "YTR:stä ladattuja opiskeluoikeksia ei tallenneta uudestaan uusilla versionumeroilla, kun ne ladataan useamman kerran" in {
    clearYtrData()

    downloadYtrData(birthmonthStart, birthmonthEnd, force = true)
    downloadYtrData(birthmonthStart, birthmonthEnd, force = true)

    verifyOppijat(expectedOppijat())
  }


  "YTR download peräkkäin päivittyvällä sisällöllä" in {
    clearYtrData()

    downloadYtrData(birthmonthStart, birthmonthEnd, force = true)
    downloadYtrData(modifiedSince, force = true)

    verifyOppijat(
      expectedOppijat(
        expectedOsasuorituksetLkm = Seq(12, 3, 12, 24),
        expectedVersionumerot = Seq(1, 2, 1, 1)
      )
    )
  }

  "YTR download modified since" in {
    clearYtrData()

    downloadYtrData(modifiedSince, force = true)

    verifyOppijat(
      expectedOppijat(
        hetut = Seq(oppijahetut(0), oppijahetut(1)),
        expectedOsasuorituksetLkm = Seq(12, 3),
        expectedVersionumerot = Seq(1, 1)
      )
    )
  }

  private def expectedOppijat(
    hetut: Seq[String] = oppijahetut,
    expectedOsasuorituksetLkm: Seq[Int] = Seq(12, 5, 12, 24),
    expectedVersionumerot: Seq[Int] = Seq.fill(4)(1),
  ): Seq[ExpectedOppijaData] = {
    hetut.zipWithIndex.map {
      case (hetu, i) =>
        ExpectedOppijaData(
          hetu = hetu,
          osasuorituksetLkm = expectedOsasuorituksetLkm(i),
          versionumero = expectedVersionumerot(i)
        )
    }
  }

  private def verifyOppijat(
    expected: Seq[ExpectedOppijaData]
  ): Unit =
    expected.foreach(verifyOppija)

  private def verifyOppija(expected: ExpectedOppijaData): Unit = {
    val oppija = getYtrOppija(
      KoskiApplicationForTests.opintopolkuHenkilöFacade.findOppijaByHetu(expected.hetu).get.oid,
      MockUsers.paakayttaja
    )
    oppija.opiskeluoikeudet should have length (1)
    oppija.opiskeluoikeudet(0).suoritukset(0).osasuoritukset.get should have length (expected.osasuorituksetLkm)
    oppija.opiskeluoikeudet(0).versionumero should be(Some(expected.versionumero))
  }

  private def getYtrOppija(oppijaOid: String, user: UserWithPassword = defaultUser): Oppija = {
    authGet("api/oppija/" + oppijaOid + "/ytr-json", user) {
      verifyResponseStatusOk()
      readOppija
    }
  }
}

case class ExpectedOppijaData(
  hetu: String,
  osasuorituksetLkm: Int,
  versionumero: Int
)
