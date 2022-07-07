package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.http.{JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.valpas.db.ValpasDatabaseFixtureLoader
import fi.oph.koski.valpas.log.{ValpasAuditLogMessageField, ValpasOperation}
import fi.oph.koski.valpas.opiskeluoikeusfixture.{FixtureUtil, ValpasMockOppijat}
import fi.oph.koski.valpas.opiskeluoikeusrepository.MockValpasRajapäivätService
import fi.oph.koski.valpas.oppija.ValpasErrorCategory
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
    super.beforeEach()
    KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
      .asetaMockTarkastelupäivä(FixtureUtil.DefaultTarkastelupäivä)
    new ValpasDatabaseFixtureLoader(KoskiApplicationForTests).reset()
    AuditLogTester.clearMessages
  }

  override protected def afterEach(): Unit = {
    KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
      .asetaMockTarkastelupäivä(FixtureUtil.DefaultTarkastelupäivä)
    new ValpasDatabaseFixtureLoader(KoskiApplicationForTests).reset()
    super.afterEach()
  }

  val oppijatOikeusMaksuttomuuteen = List(
    // Tuple: (oppija: LaajatOppijaHenkilöTiedot, maksuttomuusVoimassaAsti: LocalDate)
    (ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021, LocalDate.of(2025, 12, 31)),
    (ValpasMockOppijat.maksuttomuuttaPidennetty, LocalDate.of(2024, 12, 31)),
    (ValpasMockOppijat.turvakieltoOppijaTyhjälläKotikunnalla, LocalDate.of(2025, 12, 31)),
  )

  val oppijatEiOikeuttaMaksuttomuuteenKoskaOVLUlkopuolella = List(
    ValpasMockOppijat.eiOppivelvollinenSyntynytEnnen2004,
    ValpasMockOppijat.muuttanutUlkomaille,
  )

  val oppijatEiOikeuttaMaksuttomuuteenKoskaValmistunut = List(
    // Tuple: (oppija: LaajatOppijaHenkilöTiedot, maksuttomuusVoimassaAsti: LocalDate)
    (ValpasMockOppijat.ammattikoulustaValmistunutOpiskelija, LocalDate.of(2021, 9, 2)),
  )

  val oppijatEiKoskessa = List(
    ValpasMockOppijat.eiKoskessaOppivelvollinen,
    ValpasMockOppijat.eiOppivelvollinenLiianNuori,
    ValpasMockOppijat.eiKoskessaOppivelvollinenAhvenanmaalainen,
    ValpasMockOppijat.eiKoskessaAlle18VuotiasMuttaEiOppivelvollinenSyntymäajanPerusteella
  )

  "YTL-luovutuspalvelukäyttäjä" - {
    "Oidit" - {
      "Oikea tulos, jos oppijalla oikeus maksuttomaan koulutukseen" in {
        val expectedData = oppijatOikeusMaksuttomuuteen.map(o => YtlMaksuttomuustieto(
          oppijaOid = o._1.oid,
          oikeusMaksuttomaanKoulutukseenVoimassaAsti = Some(o._2),
          maksuttomuudenPiirissä = Some(true),
        ))

        doQuery(oidit = Some(expectedData.map(_.oppijaOid))) {
          verifyResponseStatusOk()
          sort(parsedResponse) shouldBe sort(expectedData)
        }
      }

      "Oikea tulos, jos oppijalla on ollut oikeus maksuttomaan koulutukseen aiemmin" in {
        KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
          .asetaMockTarkastelupäivä(LocalDate.of(2026, 3, 25))

        val expectedData = oppijatOikeusMaksuttomuuteen.map(o => YtlMaksuttomuustieto(
          oppijaOid = o._1.oid,
          oikeusMaksuttomaanKoulutukseenVoimassaAsti = Some(o._2),
          maksuttomuudenPiirissä = Some(false),
        ))

        doQuery(oidit = Some(expectedData.map(_.oppijaOid))) {
          verifyResponseStatusOk()
          sort(parsedResponse) shouldBe sort(expectedData)
        }
      }

      "Oikea tulos, jos oppijalla ei ole oikeutta maksuttomaan koulutukseen, koska koulutuksen maksuttomuuslain ulkopuolella" in {
        val expectedData = oppijatEiOikeuttaMaksuttomuuteenKoskaOVLUlkopuolella.map(o => YtlMaksuttomuustieto(
          oppijaOid = o.oid,
          maksuttomuudenPiirissä = Some(false),
        ))

        doQuery(oidit = Some(expectedData.map(_.oppijaOid))) {
          verifyResponseStatusOk()
          sort(parsedResponse) shouldBe sort(expectedData)
        }
      }

      "Oikea tulos, jos oppijalla ei ole oikeutta maksuttomaan koulutukseen, koska on Valppaan tietojen perusteella valmistunut toisen asteen koulutuksesta" in {
        val expectedData = oppijatEiOikeuttaMaksuttomuuteenKoskaValmistunut.map(o => YtlMaksuttomuustieto(
          oppijaOid = o._1.oid,
          oikeusMaksuttomaanKoulutukseenVoimassaAsti = Some(o._2),
          maksuttomuudenPiirissä = Some(false),
        ))

        doQuery(oidit = Some(expectedData.map(_.oppijaOid))) {
          verifyResponseStatusOk()
          sort(parsedResponse) shouldBe sort(expectedData)
        }
      }

      "Jos oppijaa ei löydy Koskesta, palauta ONR:ssä olevan iän perusteella päätelty maksuttomuustieto" in {
        val oids = oppijatEiKoskessa.map(_.oid)

        val expectedData = List(
          YtlMaksuttomuustieto(
            oppijaOid = ValpasMockOppijat.eiKoskessaOppivelvollinen.oid,
            oikeusMaksuttomaanKoulutukseenVoimassaAsti = Some( LocalDate.of(2025, 12, 31)),
            maksuttomuudenPiirissä = Some(true),
          ),
          YtlMaksuttomuustieto(
            oppijaOid = ValpasMockOppijat.eiKoskessaOppivelvollinenAhvenanmaalainen.oid,
            maksuttomuudenPiirissä = Some(false),
          ),
          YtlMaksuttomuustieto(
            oppijaOid = ValpasMockOppijat.eiKoskessaAlle18VuotiasMuttaEiOppivelvollinenSyntymäajanPerusteella.oid,
            maksuttomuudenPiirissä = Some(false),
          ),
        )

        doQuery(oidit = Some(oids)) {
          verifyResponseStatusOk()
          sort(parsedResponse) shouldBe sort(expectedData)
        }
      }

      "Tyhjä vastaus, jos oppija on ONR:ssä, mutta ei vielä olla elokuussa hänen 7-vuotisvuotenaan" in {
        KoskiApplicationForTests.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService]
          .asetaMockTarkastelupäivä(LocalDate.of(2021, 7, 31))

        val oids = List(ValpasMockOppijat.eiKoskessa7VuottaTäyttävä.oid)

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
        val expectedData = oppijatOikeusMaksuttomuuteen.map(o =>
          YtlMaksuttomuustieto(
            oppijaOid = o._1.oid,
            hetu = o._1.hetu,
            oikeusMaksuttomaanKoulutukseenVoimassaAsti = Some(o._2),
            maksuttomuudenPiirissä = Some(true),
          )
        )

        doQuery(hetut = Some(expectedData.map(_.hetu.get))) {
          verifyResponseStatusOk()
          sort(parsedResponse) shouldBe sort(expectedData)
        }
      }

      "Oikea tulos, jos oppijalla ei ole oikeutta maksuttomaan koulutukseen, koska koulutuksen maksuttomuuslain ulkopuolella" in {
        val expectedData = oppijatEiOikeuttaMaksuttomuuteenKoskaOVLUlkopuolella.map(o => YtlMaksuttomuustieto(
          oppijaOid = o.oid,
          hetu = o.hetu,
          maksuttomuudenPiirissä = Some(false),
        ))

        doQuery(hetut = Some(expectedData.map(_.hetu.get))) {
          verifyResponseStatusOk()
          sort(parsedResponse) shouldBe sort(expectedData)
        }
      }

      "Oikea tulos, jos oppijalla ei ole oikeutta maksuttomaan koulutukseen, koska on Valppaan tietojen perusteella valmistunut toisen asteen koulutuksesta" in {
        val expectedData = oppijatEiOikeuttaMaksuttomuuteenKoskaValmistunut.map(o => YtlMaksuttomuustieto(
          oppijaOid = o._1.oid,
          hetu = o._1.hetu,
          oikeusMaksuttomaanKoulutukseenVoimassaAsti = Some(o._2),
          maksuttomuudenPiirissä = Some(false),
        ))

        doQuery(hetut = Some(expectedData.flatMap(_.hetu))) {
          verifyResponseStatusOk()
          sort(parsedResponse) shouldBe sort(expectedData)
        }
      }

      "Jos oppijaa ei löydy Koskesta, palauta ONR:ssä olevan iän perusteella päätelty maksuttomuustieto" in {
        val hetut = oppijatEiKoskessa.flatMap(_.hetu)

        val expectedData = List(
          YtlMaksuttomuustieto(
            oppijaOid = ValpasMockOppijat.eiKoskessaOppivelvollinen.oid,
            hetu = ValpasMockOppijat.eiKoskessaOppivelvollinen.hetu,
            oikeusMaksuttomaanKoulutukseenVoimassaAsti = Some( LocalDate.of(2025, 12, 31)),
            maksuttomuudenPiirissä = Some(true),
          ),
          YtlMaksuttomuustieto(
            oppijaOid = ValpasMockOppijat.eiKoskessaOppivelvollinenAhvenanmaalainen.oid,
            hetu = ValpasMockOppijat.eiKoskessaOppivelvollinenAhvenanmaalainen.hetu,
            oikeusMaksuttomaanKoulutukseenVoimassaAsti = None,
            maksuttomuudenPiirissä = Some(false),
          ),
          YtlMaksuttomuustieto(
            oppijaOid = ValpasMockOppijat.eiKoskessaAlle18VuotiasMuttaEiOppivelvollinenSyntymäajanPerusteella.oid,
            hetu = ValpasMockOppijat.eiKoskessaAlle18VuotiasMuttaEiOppivelvollinenSyntymäajanPerusteella.hetu,
            oikeusMaksuttomaanKoulutukseenVoimassaAsti = None,
            maksuttomuudenPiirissä = Some(false),
          ),
        )

        doQuery(hetut = Some(hetut)) {
          verifyResponseStatusOk()
          sort(parsedResponse) shouldBe sort(expectedData)
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
      ).sortBy(_.oid)

      doQuery(oidit = Some(oppijat.map(_.oid))) {
        verifyResponseStatusOk()
        AuditLogTester
          .getLogMessages
          .takeRight(oppijat.size)
          .sorted
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
