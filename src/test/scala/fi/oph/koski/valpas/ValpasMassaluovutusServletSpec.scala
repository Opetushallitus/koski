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

import java.nio.charset.StandardCharsets

class ValpasMassaluovutusServletSpec extends ValpasTestBase with BeforeAndAfterEach {
  override def body: String = new String(response.bodyBytes, StandardCharsets.UTF_8)

  implicit val formats: org.json4s.Formats = DefaultFormats
  private lazy val t = new LocalizationReader(KoskiApplicationForTests.valpasLocalizationRepository, "fi")

  override protected def beforeAll(): Unit = {
    FixtureUtil.resetMockData(KoskiApplicationForTests, ValpasKuntarouhintaSpec.tarkastelupäivä)
  }

  override protected def beforeEach() {
    AuditLogTester.clearMessages
    Wait.until {
      !KoskiApplicationForTests.massaluovutusService.hasWork
    }
    KoskiApplicationForTests.massaluovutusService.truncate()
  }

  "Ei suorita oppivelvollisuutta" - {


    def getQuery(kuntaOid: String) =
      s"""
      {
        "type": "eiSuoritaOppivelvollisuutta",
        "format": "application/json",
        "kuntaOid": "${kuntaOid}"
      }
      """.stripMargin

    def getAktiivisetQuery(kuntaOid: String, vainAktiivisetKuntailmoitukset: Boolean) = s"""
      {
        "type": "eiSuoritaOppivelvollisuutta",
        "format": "application/json",
        "kuntaOid": "${kuntaOid}",
        "vainAktiivisetKuntailmoitukset": ${vainAktiivisetKuntailmoitukset}
      }
      """.stripMargin

    "Kyselyn luonti" - {

      "toimii pääkäyttäjänä" in {
        postQuery(getQuery(MockOrganisaatiot.helsinginKaupunki), ValpasMockUsers.valpasOphPääkäyttäjä) {
          verifyResponseStatusOk()
          val json = JsonMethods.parse(body)
          (json \ "status").extract[String] should equal("pending")
          (json \ "queryId").extract[String] should not be empty
        }
      }

      "toimii kuntakäyttäjänä" in {
        postQuery(getQuery(MockOrganisaatiot.helsinginKaupunki), ValpasMockUsers.valpasHelsinki) {
          verifyResponseStatusOk()
          val json = JsonMethods.parse(body)
          (json \ "status").extract[String] should equal("pending")
        }
      }

      "hylätään, jos kuntakäyttäjällä ei oikeuksia kysyttyyn kuntaan" in {
        postQuery(getQuery(MockOrganisaatiot.helsinginKaupunki), ValpasMockUsers.valpasTornio) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "hylätään pelkillä hakeutumisenvalvonnan oikeuksilla" in {
        postQuery(getQuery(MockOrganisaatiot.helsinginKaupunki), ValpasMockUsers.valpasHelsinkiPeruskoulu) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "hylätään pelkillä suorittamisenvalvonnan oikeuksilla" in {
        postQuery(getQuery(MockOrganisaatiot.helsinginKaupunki), ValpasMockUsers.valpasPelkkäSuorittaminenkäyttäjäAmmattikoulu) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "hylätään pelkillä maksuttomuudenvalvonnan oikeuksilla" in {
        postQuery(getQuery(MockOrganisaatiot.helsinginKaupunki), ValpasMockUsers.valpasPelkkäMaksuttomuusKäyttäjä) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "hylätään Ahvenanmaalaisen kunnan tietojen haku" in {
        postQuery(getQuery(MockOrganisaatiot.maarianhamina), ValpasMockUsers.valpasOphPääkäyttäjä) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "hylkää virheellisesti muodostetun kunnan tietojen haun" in {
        postQuery(getQuery("foo"), ValpasMockUsers.valpasOphPääkäyttäjä) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }
    }

    "Kyselyn tila ja käyttöoikeudet" - {

      "palauttaa kyselyn tilan" in {
        val queryId = postQuery(getQuery(MockOrganisaatiot.helsinginKaupunki), ValpasMockUsers.valpasHelsinki) {
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
        val queryId = postQuery(getQuery(MockOrganisaatiot.helsinginKaupunki), ValpasMockUsers.valpasHelsinki) {
          verifyResponseStatusOk()
          (JsonMethods.parse(body) \ "queryId").extract[String]
        }

        get(s"/valpas/api/massaluovutus/${queryId}", headers = authHeaders(ValpasMockUsers.valpasTornio)) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound())
        }
      }

      "hylkää, kyselyn tunniste on epävalidi" in {
        postQuery(getQuery(MockOrganisaatiot.helsinginKaupunki), ValpasMockUsers.valpasHelsinki) {
          verifyResponseStatusOk()
          (JsonMethods.parse(body) \ "queryId").extract[String]
        }

        get(s"/valpas/api/massaluovutus/foobar", headers = authHeaders(ValpasMockUsers.valpasTornio)) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Epävalidi tunniste"))
        }
      }
    }

    "Kyselyn tulosten noutaminen ja audit lokit" - {

      "suorittaa kyselyn ja palauttaa tulokset" in {
        val queryId = postQuery(getQuery(MockOrganisaatiot.helsinginKaupunki), ValpasMockUsers.valpasHelsinki) {
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
        val queryId = postQuery(getQuery(MockOrganisaatiot.pyhtäänKunta), ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu) {
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

            val vainOppijanumerorekisterissä = (oppija \ "vainOppijanumerorekisterissä").extract[Boolean]
            if (!vainOppijanumerorekisterissä) {
              // Jos on oppivelvollinen Valppaassa, on myös oikeus maksuttomuuteen päätelty
              (oppija \ "oikeusMaksuttomaanKoulutukseenVoimassaAsti").toOption should not be None
              (oppija \ "kotikuntaSuomessaAlkaen").toOption should not be None
            } else {
              (oppija \ "oikeusMaksuttomaanKoulutukseenVoimassaAsti").toOption should be (None)
              (oppija \ "kotikuntaSuomessaAlkaen").toOption should be (None)
            }
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
        val queryId = postQuery(getAktiivisetQuery(MockOrganisaatiot.pyhtäänKunta, vainAktiivisetKuntailmoitukset = true),
          ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu
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
            (oppija \ "oppijanumero").extract[String] shouldBe ValpasMockOppijat
              .oppivelvollisuusKeskeytettyEiOpiskele
              .oid
            (oppija \ "aktiivinenKuntailmoitus" \ "aktiivinen").extract[Boolean] shouldBe true
          }
        }
      }

      "hylkää tiedoston latauksen, jos käyttäjällä ei ole oikeuksia" in {
        val queryId = postQuery(getQuery(MockOrganisaatiot.helsinginKaupunki), ValpasMockUsers.valpasHelsinki) {
          verifyResponseStatusOk()
          (JsonMethods.parse(body) \ "queryId").extract[String]
        }

        waitForCompletion(queryId, ValpasMockUsers.valpasHelsinki)

        val fileUrl = get(
          s"/valpas/api/massaluovutus/${queryId}",
          headers = authHeaders(ValpasMockUsers.valpasHelsinki)
        ) {
          val json = JsonMethods.parse(body)
          val files = (json \ "files").extract[List[String]]
          files.head
        }

        // Yritä ladata tiedosto eri käyttäjänä
        getResult(fileUrl, ValpasMockUsers.valpasTornio) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound())
        }
      }

      "jättää merkinnän kyselyn suorituksesta" in {
        val queryId = postQuery(getQuery(MockOrganisaatiot.pyhtäänKunta), ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu) {
          verifyResponseStatusOk()
          (JsonMethods.parse(body) \ "queryId").extract[String]
        }

        waitForCompletion(queryId, ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu)

        val logMessages = AuditLogTester.getLogMessages
        val massaluovutusLog = logMessages.find(msg =>
          msg.contains(ValpasOperation.VALPAS_MASSALUOVUTUS_KUNTA.toString)
        )

        massaluovutusLog should not be empty
        AuditLogTester.verifyAuditLogString(
          massaluovutusLog.get, Map(
            "operation" -> ValpasOperation.VALPAS_MASSALUOVUTUS_KUNTA.toString,
            "target" -> Map(
              ValpasAuditLogMessageField.hakulause.toString -> ValpasKuntarouhintaSpec.kuntakoodi,
              ValpasAuditLogMessageField.oppijaHenkilöOidList.toString -> ValpasKuntarouhintaSpec
                .eiOppivelvollisuuttaSuorittavatOppijat(t)
                .map(_.oppija.oid)
                .mkString(" "),
              ValpasAuditLogMessageField.sivu.toString -> "1",
              ValpasAuditLogMessageField.sivuLukumäärä.toString -> "1",
            ),
          )
        )
      }
    }
  }

  "Kaikki oppivelvolliset" - {

    def getQuery(kuntaOid: String) =
      s"""
      {
        "type": "oppivelvolliset",
        "format": "application/json",
        "kuntaOid": "${kuntaOid}"
      }
      """.stripMargin

    "Kyselyn luonti" - {

      "toimii pääkäyttäjänä" in {
        postQuery(getQuery(MockOrganisaatiot.helsinginKaupunki), ValpasMockUsers.valpasOphPääkäyttäjä) {
          verifyResponseStatusOk()
          val json = JsonMethods.parse(body)
          (json \ "status").extract[String] should equal("pending")
          (json \ "queryId").extract[String] should not be empty
        }
      }

      "toimii kuntakäyttäjänä" in {
        postQuery(getQuery(MockOrganisaatiot.helsinginKaupunki), ValpasMockUsers.valpasHelsinki) {
          verifyResponseStatusOk()
          val json = JsonMethods.parse(body)
          (json \ "status").extract[String] should equal("pending")
        }
      }

      "hylätään, jos kuntakäyttäjällä ei oikeuksia kysyttyyn kuntaan" in {
        postQuery(getQuery(MockOrganisaatiot.helsinginKaupunki), ValpasMockUsers.valpasTornio) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }
    }

    "Kyselyn tulosten noutaminen ja audit lokit" - {

      "palauttaa kaikki kunnan oppivelvolliset" in {
        val queryId = postQuery(
          getQuery(MockOrganisaatiot.pyhtäänKunta),
          ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu
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
          oppijat should not be empty

          // Tarkista että jokaisella oppijalla on vaaditut kentät
          oppijat.foreach { oppija =>
            (oppija \ "oppijanumero").extract[String] should not be empty
            (oppija \ "etunimet").extract[String] should not be empty
            (oppija \ "sukunimi").extract[String] should not be empty
            val vainOppijanumerorekisterissä = (oppija \ "vainOppijanumerorekisterissä").extract[Boolean]
            if (!vainOppijanumerorekisterissä) {
              // Jos on oppivelvollinen Valppaassa, on myös oikeus maksuttomuuteen päätelty
              (oppija \ "oikeusMaksuttomaanKoulutukseenVoimassaAsti").toOption should not be None
              (oppija \ "kotikuntaSuomessaAlkaen").toOption should not be None
            } else {
              (oppija \ "oikeusMaksuttomaanKoulutukseenVoimassaAsti").toOption should be (None)
              (oppija \ "kotikuntaSuomessaAlkaen").toOption should be (None)
            }
          }

          // Tarkista että myös oppijat ONR:stä on mukana tuloksissa
          oppijat.find(oppija => (oppija \ "vainOppijanumerorekisterissä").extract[Boolean]) should not be empty
        }
      }

      "hylkää tiedoston latauksen, jos käyttäjällä ei ole oikeuksia" in {
        val queryId = postQuery(getQuery(MockOrganisaatiot.helsinginKaupunki), ValpasMockUsers.valpasHelsinki) {
          verifyResponseStatusOk()
          (JsonMethods.parse(body) \ "queryId").extract[String]
        }

        waitForCompletion(queryId, ValpasMockUsers.valpasHelsinki)

        val fileUrl = get(
          s"/valpas/api/massaluovutus/${queryId}",
          headers = authHeaders(ValpasMockUsers.valpasHelsinki)
        ) {
          val json = JsonMethods.parse(body)
          val files = (json \ "files").extract[List[String]]
          files.head
        }

        // Yritä ladata tiedosto eri käyttäjänä
        getResult(fileUrl, ValpasMockUsers.valpasTornio) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound())
        }
      }

      "jättää merkinnän kyselyn suorituksesta" in {
        val queryId = postQuery(getQuery(MockOrganisaatiot.pyhtäänKunta), ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu) {
          verifyResponseStatusOk()
          (JsonMethods.parse(body) \ "queryId").extract[String]
        }

        waitForCompletion(queryId, ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu)

        val logMessages = AuditLogTester.getLogMessages
        val massaluovutusLog = logMessages.find(msg =>
          msg.contains(ValpasOperation.VALPAS_MASSALUOVUTUS_KUNTA.toString)
        )

        massaluovutusLog should not be empty
        val logMessage = massaluovutusLog.get
        logMessage should include(ValpasOperation.VALPAS_MASSALUOVUTUS_KUNTA.toString)
        logMessage should include(ValpasKuntarouhintaSpec.kuntakoodi)
      }
    }
  }

  private def postQuery[T](
    query: String,
    user: fi.oph.koski.valpas.valpasuser.ValpasMockUser
  )(f: => T): T = {
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
    }
    )
    withBaseUrl(location) {
      get(s"${location.getPath}?${location.getQuery}") {
        verifyResponseStatusOk()
        f
      }
    }
  }

  private def waitForCompletion(queryId: String, user: ValpasMockUser): Unit = {
    Wait.until(
      {
        get(s"/valpas/api/massaluovutus/${queryId}", headers = authHeaders(user)) {
          val status = (JsonMethods.parse(body) \ "status").extract[String]
          status == "complete" || status == "failed"
        }
      }, timeoutMs = 30000
    )
  }
}
