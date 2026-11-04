package fi.oph.koski.massaluovutus

import fi.oph.koski.api.misc.{OpiskeluoikeudenMitätöintiJaPoistoTestMethods, OpiskeluoikeusTestMethodsAmmatillinen}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.MassaluovutusQuerySisaltyvatOpiskeluoikeudet
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeus, OidOrganisaatio, SisältäväOpiskeluoikeus}
import fi.oph.koski.util.Wait
import fi.oph.scalaschema.Serializer.format
import org.json4s.JArray
import org.json4s.jackson.JsonMethods
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import java.nio.charset.StandardCharsets
import java.time.LocalDate

class MassaluovutusSisaltyvatOpiskeluoikeudetSpec
  extends AnyFreeSpec
    with MassaluovutusTestMethods
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with OpiskeluoikeusTestMethodsAmmatillinen
    with OpiskeluoikeudenMitätöintiJaPoistoTestMethods {

  override def body: String = new String(response.bodyBytes, StandardCharsets.UTF_8)

  private val sisältävänAlkamispäivä = LocalDate.of(2020, 1, 1)
  private val sisältyvänAlkamispäivä = LocalDate.of(2021, 1, 1)

  // Stadin ammattiopiston (Helsingin kaupunki) opiskeluoikeus, johon Omnian opiskeluoikeus on merkitty sisältyväksi
  private var sisältävä: AmmatillinenOpiskeluoikeus = _
  private var sisältyvä: AmmatillinenOpiskeluoikeus = _
  // Toinen pari, jonka sisältävä opiskeluoikeus mitätöidään
  private var mitätöitySisältävä: AmmatillinenOpiskeluoikeus = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    resetFixtures()

    sisältävä = setupOppijaWithAndGetOpiskeluoikeus(makeOpiskeluoikeus(alkamispäivä = sisältävänAlkamispäivä), KoskiSpecificMockOppijat.eero, MockUsers.stadinAmmattiopistoJaOppisopimuskeskusTallentaja)
    sisältyvä = createOpiskeluoikeus(KoskiSpecificMockOppijat.eero, sisältyväOmniassa(sisältävä), user = MockUsers.omniaTallentaja)

    mitätöitySisältävä = setupOppijaWithAndGetOpiskeluoikeus(makeOpiskeluoikeus(alkamispäivä = sisältävänAlkamispäivä), KoskiSpecificMockOppijat.eerola, MockUsers.stadinAmmattiopistoJaOppisopimuskeskusTallentaja)
    createOpiskeluoikeus(KoskiSpecificMockOppijat.eerola, sisältyväOmniassa(mitätöitySisältävä), user = MockUsers.omniaTallentaja)
    mitätöiOpiskeluoikeus(mitätöitySisältävä.oid.get, MockUsers.stadinAmmattiopistoJaOppisopimuskeskusTallentaja)
  }

  override protected def afterAll(): Unit = {
    resetFixtures()
    super.afterAll()
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    Wait.until { !app.massaluovutusService.hasWork }
    app.massaluovutusService.truncate()
  }

  private def sisältyväOmniassa(sisältävä: AmmatillinenOpiskeluoikeus): AmmatillinenOpiskeluoikeus =
    makeOpiskeluoikeus(alkamispäivä = sisältyvänAlkamispäivä, toimpiste = OidOrganisaatio(MockOrganisaatiot.omnia), oppilaitos = MockOrganisaatiot.omnia).copy(
      sisältyyOpiskeluoikeuteen = Some(SisältäväOpiskeluoikeus(sisältävä.oppilaitos.get, sisältävä.oid.get)),
    )

  private val query = MassaluovutusQuerySisaltyvatOpiskeluoikeudet(
    alkanutAikaisintaan = LocalDate.of(2020, 1, 1),
  )

  private def opiskeluoikeusOidit(url: String, user: UserWithPassword): List[String] =
    verifyResultAndContent(url, user) {
      (JsonMethods.parse(body) \ "opiskeluoikeudet").asInstanceOf[JArray].arr.map(oo => (oo \ "oid").extract[String])
    }

  "Sisältyvät opiskeluoikeudet" - {
    "Ei onnistu väärän organisaation tietoihin" in {
      addQuery(query.withOrganisaatioOid(MockOrganisaatiot.jyväskylänNormaalikoulu), MockUsers.helsinkiKatselija) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden())
      }
    }

    "Ei onnistu ilman oikeuksia sensitiivisen datan lukemiseen" in {
      addQuery(query.withOrganisaatioOid(MockOrganisaatiot.omnia), MockUsers.tallentajaEiLuottamuksellinen) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden())
      }
    }

    "Sisältävän opiskeluoikeuden organisaatio saa toisen oppilaitoksen sisältyvän opiskeluoikeuden, muttei omaa sisältävää opiskeluoikeuttaan" in {
      AuditLogTester.clearMessages()
      val user = MockUsers.helsinkiKatselija
      val queryId = addQuerySuccessfully(query, user) { response =>
        response.query.asInstanceOf[MassaluovutusQuerySisaltyvatOpiskeluoikeudet].organisaatioOid should contain(MockOrganisaatiot.helsinginKaupunki)
        response.queryId
      }
      val complete = waitForCompletion(queryId, user)

      complete.files should have length 1
      complete.files.head should include(KoskiSpecificMockOppijat.eero.oid)
      verifyResultAndContent(complete.files.head, user) {
        val json = JsonMethods.parse(body)
        (json \ "henkilö" \ "oid").extract[String] should equal(KoskiSpecificMockOppijat.eero.oid)
        val opiskeluoikeudet = (json \ "opiskeluoikeudet").asInstanceOf[JArray].arr
        opiskeluoikeudet.map(oo => (oo \ "oid").extract[String]) should equal(List(sisältyvä.oid.get))
        (opiskeluoikeudet.head \ "oppilaitos" \ "oid").extract[String] should equal(MockOrganisaatiot.omnia)
        (opiskeluoikeudet.head \ "sisältyyOpiskeluoikeuteen" \ "oid").extract[String] should equal(sisältävä.oid.get)
      }

      AuditLogTester.verifyLastAuditLogMessageForOperation(Map(
        "operation" -> "SISALTYVAT_OPISKELUOIKEUDET_HAKU",
        "target" -> Map(
          "hakuEhto" -> s"alkanutAikaisintaan=2020-01-01&organisaatio=${MockOrganisaatiot.helsinginKaupunki}",
        ),
      ))
    }

    "Kysely toimii myös sisältävän opiskeluoikeuden oppilaitokselle, ei vain koulutustoimijalle" in {
      val user = MockUsers.stadinAmmattiopistoKatselija
      val queryId = addQuerySuccessfully(query.withOrganisaatioOid(MockOrganisaatiot.stadinAmmattiopisto), user)(_.queryId)
      val complete = waitForCompletion(queryId, user)

      complete.files should have length 1
      opiskeluoikeusOidit(complete.files.head, user) should equal(List(sisältyvä.oid.get))
    }

    "Sisältyvän opiskeluoikeuden oma oppilaitos ei saa kyselyllä mitään" in {
      val user = MockUsers.omniaKatselija
      val queryId = addQuerySuccessfully(query, user)(_.queryId)
      val complete = waitForCompletion(queryId, user)

      complete.files shouldBe empty
    }

    "Hakuehdot kohdistuvat sisältyvään opiskeluoikeuteen, eivät sisältävään" in {
      val user = MockUsers.helsinkiKatselija

      // Sisältävä on alkanut ennen rajapäivää, sisältyvä sen jälkeen
      val rajapäiväSisältävänJälkeen = addQuerySuccessfully(query.copy(alkanutAikaisintaan = sisältävänAlkamispäivä.plusMonths(6)), user)(_.queryId)
      waitForCompletion(rajapäiväSisältävänJälkeen, user).files should have length 1

      val rajapäiväSisältyvänJälkeen = addQuerySuccessfully(query.copy(alkanutAikaisintaan = sisältyvänAlkamispäivä.plusDays(1)), user)(_.queryId)
      waitForCompletion(rajapäiväSisältyvänJälkeen, user).files shouldBe empty

      val väärääKoulutusmuotoa = addQuerySuccessfully(query.copy(koulutusmuoto = Some("lukiokoulutus")), user)(_.queryId)
      waitForCompletion(väärääKoulutusmuotoa, user).files shouldBe empty
    }

    "Mitätöityyn opiskeluoikeuteen sisältyviä opiskeluoikeuksia ei palauteta" in {
      val user = MockUsers.helsinkiKatselija
      val queryId = addQuerySuccessfully(query, user)(_.queryId)
      val complete = waitForCompletion(queryId, user)

      complete.files.find(_.contains(KoskiSpecificMockOppijat.eerola.oid)) shouldBe None
    }

    "Toisen käyttäjän kyselyn tietoja ei voi hakea" in {
      val queryId = addQuerySuccessfully(query, MockUsers.helsinkiKatselija)(_.queryId)
      getQuery(queryId, MockUsers.omniaKatselija) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound())
      }
    }
  }
}
