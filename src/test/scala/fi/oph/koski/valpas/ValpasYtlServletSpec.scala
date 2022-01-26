package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.http.{JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.valpas.opiskeluoikeusfixture.{FixtureUtil, ValpasMockOppijat}
import fi.oph.koski.valpas.valpasuser.{ValpasMockUser, ValpasMockUsers}
import fi.oph.koski.ytl.YtlBulkRequest
import org.scalatest.BeforeAndAfterEach

class ValpasYtlServletSpec  extends ValpasTestBase with BeforeAndAfterEach {
  override protected def beforeAll(): Unit = {
    FixtureUtil.resetMockData(KoskiApplicationForTests, ValpasKuntarouhintaSpec.tarkastelupäivä)
  }

  override protected def beforeEach() {
    AuditLogTester.clearMessages
  }

  "YTL-luovutuspalvelukäyttäjä" - {
    "Oidit" - {
      "OK valideilla oideilla" in {
        val oidit = List(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid)
        doQuery(oidit = Some(oidit)) {
          verifyResponseStatusOk()
        }
      }

      "Bad request epävalideilla oideilla" in {
        val oidit = List("XYZ")
        doQuery(oidit = Some(oidit)) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.virheellinenHenkilöOid("Virheellinen oid: XYZ. Esimerkki oikeasta muodosta: 1.2.246.562.24.00000000001."))
        }
      }
    }

    "Hetut" - {
      "OK valideilla hetuilla" in {
        val hetut = List(ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.hetu.get)
        doQuery(hetut = Some(hetut)) {
          verifyResponseStatusOk()
        }
      }

      "Bad request epävalideilla hetuilla" in {
        val hetut = List("XYZ")
        doQuery(hetut = Some(hetut)) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Virheellinen muoto hetulla: XYZ"))
        }
      }
    }
  }

  "Muut käyttäjät" - {
    "Ei salli käyttöä ilman ytl-luovutuspalveluoikeuksia" in {
      doQuery(user = ValpasMockUsers.valpasMonta) {
        verifyResponseStatus(403, ValpasErrorCategory.forbidden())
      }
    }
  }

  private def doQuery(
    user: ValpasMockUser = ValpasMockUsers.valpasYtl,
    oidit: Option[List[String]] = None,
    hetut: Option[List[String]] = None,
  )(
    f: => Unit
  ): Unit = {
    val query = JsonSerializer.writeWithRoot(YtlBulkRequest(oidit = oidit, hetut = hetut, opiskeluoikeuksiaMuuttunutJälkeen = None))
    post("/valpas/api/luovutuspalvelu/ytl/maksuttomuus", body = query, headers = authHeaders(user) ++ jsonContent) {
      f
    }
  }

  private def parsedResponse = JsonSerializer.parse[String](response.body)
}
