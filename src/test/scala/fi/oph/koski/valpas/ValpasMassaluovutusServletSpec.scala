package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.util.Wait
import fi.oph.koski.valpas.log.{ValpasAuditLogMessageField, ValpasOperation}
import fi.oph.koski.valpas.opiskeluoikeusfixture.{FixtureUtil, ValpasMockOppijat}
import fi.oph.koski.valpas.valpasuser.{ValpasMockUser, ValpasMockUsers}
import org.json4s.JsonAST.JObject
import org.json4s.jackson.JsonMethods
import org.scalatest.BeforeAndAfterEach
import org.json4s.DefaultFormats

class ValpasMassaluovutusServletSpec extends ValpasTestBase with BeforeAndAfterEach {
  implicit val formats: org.json4s.Formats = DefaultFormats
  private lazy val t = new LocalizationReader(KoskiApplicationForTests.valpasLocalizationRepository, "fi")

  override protected def beforeAll(): Unit = {
    FixtureUtil.resetMockData(KoskiApplicationForTests, ValpasKuntarouhintaSpec.tarkastelupäivä)
  }

  override protected def beforeEach() {
    AuditLogTester.clearMessages
    Wait.until { !KoskiApplicationForTests.massaluovutusService.hasWork }
    KoskiApplicationForTests.massaluovutusService.truncate()
  }

  "POST /valpas/api/massaluovutus - Kyselyn luonti" - {

    "toimii pääkäyttäjänä" in {
      postQuery(ValpasMockUsers.valpasOphPääkäyttäjä) {
        verifyResponseStatusOk()
        val json = JsonMethods.parse(body)
        (json \ "status").extract[String] should equal("pending")
        (json \ "queryId").extract[String] should not be empty
      }
    }

    "toimii kuntakäyttäjänä" in {
      postQuery(ValpasMockUsers.valpasHelsinki) {
        verifyResponseStatusOk()
        val json = JsonMethods.parse(body)
        (json \ "status").extract[String] should equal("pending")
      }
    }

    "hylkää, jos kuntakäyttäjällä ei oikeuksia kysyttyyn kuntaan" in {
      postQuery(ValpasMockUsers.valpasTornio, MockOrganisaatiot.helsinginKaupunki) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden())
      }
    }

    "hylkää pelkillä hakeutumisenvalvonnan oikeuksilla" in {
      postQuery(ValpasMockUsers.valpasHelsinkiPeruskoulu) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden())
      }
    }

    "hylkää pelkillä suorittamisenvalvonnan oikeuksilla" in {
      postQuery(ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjäAmmattikoulu) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden())
      }
    }

    "hylkää pelkillä maksuttomuudenvalvonnan oikeuksilla" in {
      postQuery(ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden())
      }
    }

    "hylkää Ahvenanmaalaisen kunnan" in {
      postQuery(ValpasMockUsers.valpasOphPääkäyttäjä, MockOrganisaatiot.maarianhamina) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden())
      }
    }

    "hylkää virheellisesti muodostetun kunnan" in {
      postQuery(ValpasMockUsers.valpasOphPääkäyttäjä, "foo") {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden())
      }
    }
  }

  "GET /valpas/api/massaluovutus/:id - Kyselyn tilan kysely" - {

    "palauttaa kyselyn tilan" in {
      val queryId = postQuery(ValpasMockUsers.valpasHelsinki) {
        verifyResponseStatusOk()
        (JsonMethods.parse(body) \ "queryId").extract[String]
      }

      get(s"/valpas/api/massaluovutus/${queryId}", headers = authHeaders(ValpasMockUsers.valpasHelsinki)) {
        verifyResponseStatusOk()
        val json = JsonMethods.parse(body)
        (json \ "queryId").extract[String] should equal(queryId)
        val status = (json \ "status").extract[String]
        status should (equal("pending") or equal("running") or equal("complete"))
      }
    }

    "hylkää, jos ei oikeutta kyselyn katseluun" in {
      val queryId = postQuery(ValpasMockUsers.valpasHelsinki) {
        verifyResponseStatusOk()
        (JsonMethods.parse(body) \ "queryId").extract[String]
      }

      get(s"/valpas/api/massaluovutus/${queryId}", headers = authHeaders(ValpasMockUsers.valpasTornio)) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound())
      }
    }

    "hylkää, kyselyn tunniste on epävalidi" in {
      postQuery(ValpasMockUsers.valpasHelsinki) {
        verifyResponseStatusOk()
        (JsonMethods.parse(body) \ "queryId").extract[String]
      }

      get(s"/valpas/api/massaluovutus/foobar", headers = authHeaders(ValpasMockUsers.valpasTornio)) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Epävalidi tunniste"))
      }
    }
  }

  "GET /valpas/api/massaluovutus/:id/:fileName - Kyselyn tulosten noutaminen" - {

    "suorittaa kyselyn ja palauttaa tulokset" in {
      val queryId = postQuery(ValpasMockUsers.valpasHelsinki) {
        verifyResponseStatusOk()
        (JsonMethods.parse(body) \ "queryId").extract[String]
      }

      waitForCompletion(queryId, ValpasMockUsers.valpasHelsinki)

      get(s"/valpas/api/massaluovutus/${queryId}", headers = authHeaders(ValpasMockUsers.valpasHelsinki)) {
        verifyResponseStatusOk()
        val json = JsonMethods.parse(body)
        val status = (json \ "status").extract[String]
        status should equal("complete")

        val files = (json \ "files").extract[List[String]]
        files should not be empty
      }
    }

    "palauttaa oikean sisällön tulostiedostoissa" in {
      val queryId = postQuery(ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu, MockOrganisaatiot.pyhtäänKunta) {
        verifyResponseStatusOk()
        (JsonMethods.parse(body) \ "queryId").extract[String]
      }

      waitForCompletion(queryId, ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu)

      val fileUrl: String = verifyAndGetFileUrl(queryId)

      // Lataa ja tarkista tiedoston sisältö
      verifyResultAndContent(fileUrl, ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu) {
        val json = JsonMethods.parse(body)

        // Tarkista että vastaus sisältää oppijat-listan
        val oppijat = (json \ "oppijat").extract[List[JObject]]
        oppijat should not be empty

        // Tarkista että jokaisella oppijalla on vaaditut kentät
        oppijat.foreach { oppija =>
          (oppija \ "oppijanumero").extract[String] should not be empty
          (oppija \ "hetu").extractOpt[String] should not be None
          (oppija \ "etunimet").extract[String] should not be empty
          (oppija \ "sukunimi").extract[String] should not be empty
        }

        // Tarkista että palautetut oppijat vastaavat kuntarouhinta-haun tuloksia
        val expectedOppijat = ValpasKuntarouhintaSpec.eiOppivelvollisuuttaSuorittavatOppijat(t)
        oppijat.length should equal(expectedOppijat.length)

        val palautetutOppijanumerot = oppijat.map(o => (o \ "oppijanumero").extract[String]).toSet
        val odotetutOppijanumerot = expectedOppijat.map(_.oppija.oid).toSet
        palautetutOppijanumerot should equal(odotetutOppijanumerot)
      }
    }

    "palauttaa vain aktiivisten kuntailmoitusten oppijat kun vainAktiivisetKuntailmoitukset=true" in {
      val queryId = postQueryWithParams(
        ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu,
        MockOrganisaatiot.pyhtäänKunta,
        vainAktiivisetKuntailmoitukset = true
      ) {
        verifyResponseStatusOk()
        (JsonMethods.parse(body) \ "queryId").extract[String]
      }

      waitForCompletion(queryId, ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu)

      val fileUrl: String = verifyAndGetFileUrl(queryId)

      // Lataa ja tarkista tiedoston sisältö
      verifyResultAndContent(fileUrl, ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu) {
        val json = JsonMethods.parse(body)

        // Tarkista että vastaus sisältää oppijat-listan
        val oppijat = (json \ "oppijat").extract[List[JObject]]
        oppijat.size shouldBe 1

        // Tarkista että jokaisella oppijalla on aktiivinenKuntailmoitus
        oppijat.foreach { oppija =>
          (oppija \ "oppijanumero").extract[String] shouldBe ValpasMockOppijat.oppivelvollisuusKeskeytettyEiOpiskele.oid
          (oppija \ "aktiivinenKuntailmoitus" \ "aktiivinen").extract[Boolean] shouldBe true
        }
      }
    }

    "hylkää tiedoston latauksen, jos käyttäjällä ei ole oikeuksia" in {
      val queryId = postQuery(ValpasMockUsers.valpasHelsinki) {
        verifyResponseStatusOk()
        (JsonMethods.parse(body) \ "queryId").extract[String]
      }

      waitForCompletion(queryId, ValpasMockUsers.valpasHelsinki)

      val fileUrl = get(s"/valpas/api/massaluovutus/${queryId}", headers = authHeaders(ValpasMockUsers.valpasHelsinki)) {
        val json = JsonMethods.parse(body)
        val files = (json \ "files").extract[List[String]]
        files.head
      }

      // Yritä ladata tiedosto eri käyttäjänä
      getResult(fileUrl, ValpasMockUsers.valpasTornio) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound())
      }
    }
  }

  "Auditloki" - {

    "jättää merkinnän kyselyn suorituksesta" in {
      val queryId = postQuery(ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu, MockOrganisaatiot.pyhtäänKunta) {
        verifyResponseStatusOk()
        (JsonMethods.parse(body) \ "queryId").extract[String]
      }

      waitForCompletion(queryId, ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu)

      val logMessages = AuditLogTester.getLogMessages
      val massaluovutusLog = logMessages.find(msg =>
        msg.contains(ValpasOperation.VALPAS_MASSALUOVUTUS_KUNTA.toString)
      )

      massaluovutusLog should not be empty
      AuditLogTester.verifyAuditLogString(massaluovutusLog.get, Map(
        "operation" -> ValpasOperation.VALPAS_MASSALUOVUTUS_KUNTA.toString,
        "target" -> Map(
          ValpasAuditLogMessageField.hakulause.toString -> ValpasKuntarouhintaSpec.kuntakoodi,
          ValpasAuditLogMessageField.oppijaHenkilöOidList.toString -> ValpasKuntarouhintaSpec.eiOppivelvollisuuttaSuorittavatOppijat(t).map(_.oppija.oid).mkString(" "),
          ValpasAuditLogMessageField.sivu.toString -> "1",
          ValpasAuditLogMessageField.sivuLukumäärä.toString -> "1",
        ),
      ))
    }
  }

  private def postQuery[T](user: fi.oph.koski.valpas.valpasuser.ValpasMockUser, kuntaOid: String = MockOrganisaatiot.helsinginKaupunki)(f: => T): T = {
    val query =
      s"""
      {
        "type": "eiSuoritaOppivelvollisuutta",
        "format": "application/json",
        "kuntaOid": "${kuntaOid}"
      }
      """.stripMargin

    post("/valpas/api/massaluovutus", body = query, headers = authHeaders(user) ++ jsonContent) {
      f
    }
  }

  private def postQueryWithParams[T](
    user: fi.oph.koski.valpas.valpasuser.ValpasMockUser,
    kuntaOid: String = MockOrganisaatiot.helsinginKaupunki,
    vainAktiivisetKuntailmoitukset: Boolean = false
  )(f: => T): T = {
    val query =
      s"""
      {
        "type": "eiSuoritaOppivelvollisuutta",
        "format": "application/json",
        "kuntaOid": "${kuntaOid}",
        "vainAktiivisetKuntailmoitukset": ${vainAktiivisetKuntailmoitukset}
      }
      """.stripMargin

    post("/valpas/api/massaluovutus", body = query, headers = authHeaders(user) ++ jsonContent) {
      f
    }
  }

  private def getResult[T](url: String, user: ValpasMockUser)(f: => T): T = {
    val rootUrl = KoskiApplicationForTests.config.getString("koski.root.url")
    get(url.replace(rootUrl, ""), headers = authHeaders(user))(f)
  }

  private def verifyAndGetFileUrl(queryId: String): String = {
    val files = get(
      s"/valpas/api/massaluovutus/${queryId}",
      headers = authHeaders(ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu)
    ) {
      verifyResponseStatusOk()
      val json = JsonMethods.parse(body)
      val status = (json \ "status").extract[String]
      status should equal("complete")

      (json \ "files").extract[List[String]]
    }

    files should not be empty
    files.head
  }

  private def verifyResultAndContent[T](url: String, user: ValpasMockUser)(f: => T): T = {
    val location = new java.net.URL(getResult(url, user) {
      verifyResponseStatus(302) // 302: Found (redirect)
      response.header("Location")
    })
    withBaseUrl(location) {
      get(s"${location.getPath}?${location.getQuery}") {
        verifyResponseStatusOk()
        f
      }
    }
  }

  private def waitForCompletion(queryId: String, user: ValpasMockUser): Unit = {
    Wait.until({
      get(s"/valpas/api/massaluovutus/${queryId}", headers = authHeaders(user)) {
        val status = (JsonMethods.parse(body) \ "status").extract[String]
        status == "complete" || status == "failed"
      }
    }, timeoutMs = 30000)
  }
}
