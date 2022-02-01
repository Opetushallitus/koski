package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.http.{JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.valpas.log.{ValpasAuditLogMessageField, ValpasOperation}
import fi.oph.koski.valpas.opiskeluoikeusfixture.{FixtureUtil, ValpasMockOppijat}
import fi.oph.koski.valpas.valpasuser.{ValpasMockUser, ValpasMockUsers}
import fi.oph.koski.valpas.ytl.YtlMaksuttomuustieto
import fi.oph.koski.ytl.YtlBulkRequest
import org.scalatest.BeforeAndAfterEach

import java.time.LocalDate

class ValpasYtlServletSpec  extends ValpasTestBase with BeforeAndAfterEach {
  val tarkastelupäivä = LocalDate.of(2021, 12, 1)

  override protected def beforeAll(): Unit = {
    FixtureUtil.resetMockData(KoskiApplicationForTests, tarkastelupäivä)
  }

  override protected def beforeEach() {
    AuditLogTester.clearMessages
  }

  "YTL-luovutuspalvelukäyttäjä" - {
    "Oidit" - {
      "Oikea tulos, jos oppijalla oikeus maksuttomaan koulutukseen" in {
        val expectedData = List(
          YtlMaksuttomuustieto(
            oppijaOid = ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid,
            oikeusMaksuttomaanKoulutukseenVoimassaAsti = Some(LocalDate.of(2025,12,31)),
            maksuttomuudenPiirissä = Some(true),
          ),
          YtlMaksuttomuustieto(
            oppijaOid = ValpasMockOppijat.maksuttomuuttaPidennetty.oid,
            oikeusMaksuttomaanKoulutukseenVoimassaAsti = Some(LocalDate.of(2024,12,31)),
            maksuttomuudenPiirissä = Some(true),
          ),
        )

        doQuery(oidit = Some(expectedData.map(_.oppijaOid))) {
          verifyResponseStatusOk()
          sort(parsedResponse) shouldBe sort(expectedData)
        }
      }

      "Oikea tulos, jos oppijalla ei ole oikeutta maksuttomaan koulutukseen" in {
        val expectedData = List(
          YtlMaksuttomuustieto(
            oppijaOid = ValpasMockOppijat.eiOppivelvollinenSyntynytEnnen2004.oid,
            maksuttomuudenPiirissä = Some(false),
          ),
        )

        doQuery(oidit = Some(expectedData.map(_.oppijaOid))) {
          verifyResponseStatusOk()
          sort(parsedResponse) shouldBe sort(expectedData)
        }
      }

      "Tyhjä vastaus, jois oppijaa ei löydy Koskesta (maksuttomuutta ei voida päätellä)" in {
        val oids = List(
          ValpasMockOppijat.eiKoskessaOppivelvollinen.oid,
          ValpasMockOppijat.eiOppivelvollinenLiianNuori.oid,
        )

        doQuery(oidit = Some(oids)) {
          verifyResponseStatusOk()
          parsedResponse shouldBe List.empty
        }
      }

      "Tyhjä vastaus, jos oppijaa ei löydy Opintopolusta (maksuttomuutta ei voida päätellä)" in {
        val oids = List("1.2.246.562.24.99000000000", "1.2.246.562.24.99000000001")

        doQuery(oidit = Some(oids)) {
          verifyResponseStatusOk()
          parsedResponse shouldBe List.empty
        }
      }

      "Bad request epävalideilla oideilla" in {
        val oid = "1.1.111.111.11.00000000000"
        doQuery(oidit = Some(List(oid))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.virheellinenHenkilöOid(s"Virheellinen oid: $oid. Esimerkki oikeasta muodosta: 1.2.246.562.24.00000000001."))
        }
      }
    }

    "Hetut" - {
      "Oikea tulos, jos oppijalla oikeus maksuttomaan koulutukseen" in {
        val expectedData = List(
          YtlMaksuttomuustieto(
            oppijaOid = ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.oid,
            hetu = ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.hetu,
            oikeusMaksuttomaanKoulutukseenVoimassaAsti = Some(LocalDate.of(2025,12,31)),
            maksuttomuudenPiirissä = Some(true),
          ),
          YtlMaksuttomuustieto(
            oppijaOid = ValpasMockOppijat.maksuttomuuttaPidennetty.oid,
            hetu = ValpasMockOppijat.maksuttomuuttaPidennetty.hetu,
            oikeusMaksuttomaanKoulutukseenVoimassaAsti = Some(LocalDate.of(2024,12,31)),
            maksuttomuudenPiirissä = Some(true),
          ),
        )

        doQuery(hetut = Some(expectedData.map(_.hetu.get))) {
          verifyResponseStatusOk()
          sort(parsedResponse) shouldBe sort(expectedData)
        }
      }

      "Oikea tulos, jos oppijalla ei ole oikeutta maksuttomaan koulutukseen" in {
        val expectedData = List(
          YtlMaksuttomuustieto(
            oppijaOid = ValpasMockOppijat.eiOppivelvollinenSyntynytEnnen2004.oid,
            hetu = ValpasMockOppijat.eiOppivelvollinenSyntynytEnnen2004.hetu,
            maksuttomuudenPiirissä = Some(false),
          ),
        )

        doQuery(hetut = Some(expectedData.map(_.hetu.get))) {
          verifyResponseStatusOk()
          sort(parsedResponse) shouldBe sort(expectedData)
        }
      }

      "Tyhjä vastaus, jois oppijaa ei löydy Koskesta (maksuttomuutta ei voida päätellä)" in {
        val hetut = List(
          ValpasMockOppijat.eiKoskessaOppivelvollinen.hetu.get,
          ValpasMockOppijat.eiOppivelvollinenLiianNuori.hetu.get,
        )

        doQuery(hetut = Some(hetut)) {
          verifyResponseStatusOk()
          parsedResponse shouldBe List.empty
        }
      }

      "Tyhjä vastaus, jos oppijaa ei löydy Opintopolusta (maksuttomuutta ei voida päätellä)" in {
        val hetut = List("140405A6705", "270405A611M")

        doQuery(hetut = Some(hetut)) {
          verifyResponseStatusOk()
          parsedResponse shouldBe List.empty
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

  "Audit-logitus" - {
    "Pyynnöistä jää jäljet audit-logiin" in {
      val oppijat = List(
        ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
        ValpasMockOppijat.maksuttomuuttaPidennetty,
      )

      doQuery(oidit = Some(oppijat.map(_.oid))) {
        verifyResponseStatusOk()
        AuditLogTester
          .getLogMessages
          .takeRight(oppijat.size)
          .zip(oppijat)
          .foreach { msg_oppija =>
            AuditLogTester.verifyAuditLogMessage(
              msg_oppija._1,
              Map(
                "operation" -> ValpasOperation.OPPIVELVOLLISUUSREKISTERI_LUOVUTUS.toString,
                "target" -> Map(
                  ValpasAuditLogMessageField.oppijaHenkilöOid.toString -> msg_oppija._2.oid,
                )
              )
            )
          }
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
    post("/valpas/api/luovutuspalvelu/ytl/oppijat", body = query, headers = authHeaders(user) ++ jsonContent) {
      f
    }
  }

  private def parsedResponse: Seq[YtlMaksuttomuustieto] =
    JsonSerializer.parse[Seq[YtlMaksuttomuustieto]](response.body)

  private def sort(ts: Seq[YtlMaksuttomuustieto]): Seq[YtlMaksuttomuustieto] =
    ts.sorted(YtlMaksuttomuustieto.oidOrder)
}
