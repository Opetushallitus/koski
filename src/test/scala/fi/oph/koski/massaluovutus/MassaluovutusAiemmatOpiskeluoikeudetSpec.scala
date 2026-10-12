package fi.oph.koski.massaluovutus

import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.documentation.PerusopetusExampleData
import fi.oph.koski.fixture.AmmatillinenOpiskeluoikeusTestData
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.massaluovutus.aiemmatopiskeluoikeudet.MassaluovutusQueryAiemmatOpiskeluoikeudet
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.util.Wait
import fi.oph.scalaschema.Serializer.format
import org.json4s.JNothing
import org.json4s.jackson.JsonMethods
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import java.nio.charset.StandardCharsets
import java.time.LocalDate

class MassaluovutusAiemmatOpiskeluoikeudetSpec extends AnyFreeSpec with MassaluovutusTestMethods with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with OpiskeluoikeusTestMethodsAmmatillinen {
  override def body: String = new String(response.bodyBytes, StandardCharsets.UTF_8)

  private val oppija = KoskiSpecificMockOppijat.lukiolainen
  private val query = MassaluovutusQueryAiemmatOpiskeluoikeudet(organisaatioOid = MockOrganisaatiot.stadinAmmattiopisto)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    resetFixtures()
    createOrUpdate(oppija, makeOpiskeluoikeus(alkamispäivä = LocalDate.of(2020, 1, 1)))
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    Wait.until { !app.massaluovutusService.hasWork }
    app.massaluovutusService.truncate()
  }

  private def oppijanTiedosto(files: List[String], oid: String = oppija.oid) = files.find(_.contains(oid))

  private def koulutusmuodot(json: org.json4s.JValue): List[String] =
    (json \ "opiskeluoikeudet").children.map(oo => (oo \ "tyyppi" \ "koodiarvo").extract[String])

  private def oppilaitokset(json: org.json4s.JValue): Set[String] =
    (json \ "opiskeluoikeudet").children.map(oo => (oo \ "oppilaitos" \ "oid").extract[String]).toSet

  private def haeTulostiedostot(user: UserWithPassword = MassaluovutusAiemmatOpiskeluoikeudetSpec.oletuskäyttäjä): List[String] = {
    val queryId = addQuerySuccessfully(query, user)(_.queryId)
    waitForCompletion(queryId, user).files
  }

  private def lukionSisäoppilaitosmainenMajoitus(json: org.json4s.JValue): org.json4s.JValue =
    (json \ "opiskeluoikeudet").children
      .find(oo => (oo \ "tyyppi" \ "koodiarvo").extract[String] == "lukiokoulutus")
      .map(oo => oo \ "lisätiedot" \ "sisäoppilaitosmainenMajoitus")
      .getOrElse(fail("lukion opiskeluoikeutta ei löytynyt"))

  "Aiemmat opiskeluoikeudet" - {
    "Ei onnistu ilman AIEMMAT_OPISKELUOIKEUDET-roolia" in {
      addQuery(query, MockUsers.helsinkiKatselija) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden())
      }
    }

    "Ei onnistu, jos rooli on vain toisessa organisaatiossa" in {
      addQuery(query, MockUsers.tornioAiemmatOpiskeluoikeudetHelsinkiKatselija) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden())
      }
    }

    "Ei onnistu koulutustoimijan oidilla" in {
      addQuery(query.copy(organisaatioOid = MockOrganisaatiot.helsinginKaupunki), MockUsers.helsinkiAiemmatOpiskeluoikeudet) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.massaluovutus.eiOppilaitos())
      }
    }

    "Ei onnistu koulutustoimijan oidilla globaaleilla lukuoikeuksilla" in {
      addQuery(query.copy(organisaatioOid = MockOrganisaatiot.helsinginKaupunki), MockUsers.paakayttaja) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden())
      }
    }

    "Ei onnistu tuntemattomalla koulutusmuodolla" in {
      addQuery(query.copy(koulutusmuoto = Some(List("perusopetus", "korkeakoulutus"))), MockUsers.helsinkiAiemmatOpiskeluoikeudet) {
        verifyResponseStatus(400)
        body should include("Tuntematon koulutusmuoto")
      }
    }

    "Palauttaa nykyisen opiskelijan muiden organisaatioiden aiemmat opiskeluoikeudet" in {
      AuditLogTester.clearMessages()
      val user = MockUsers.helsinkiAiemmatOpiskeluoikeudet
      val queryId = addQuerySuccessfully(query, user)(_.queryId)
      val complete = waitForCompletion(queryId, user)
      val file = oppijanTiedosto(complete.files)
      file shouldBe defined
      verifyResultAndContent(file.get, user) {
        val json = JsonMethods.parse(body)
        (json \ "henkilö" \ "oid").extract[String] shouldBe oppija.oid
        koulutusmuodot(json).sorted shouldBe List("lukiokoulutus", "perusopetus")
        (json \ "opiskeluoikeudet").children.map(oo => (oo \ "oppilaitos" \ "oid").extract[String]).toSet shouldBe Set(MockOrganisaatiot.jyväskylänNormaalikoulu)
      }
      AuditLogTester.verifyLastAuditLogMessageForOperation(Map(
        "operation" -> "AIEMMAT_OPISKELUOIKEUDET_KATSOMINEN",
        "target" -> Map(
          "oppijaHenkiloOid" -> oppija.oid,
          "hakuEhto" -> s"koulutusmuoto=esiopetus&koulutusmuoto=perusopetus&koulutusmuoto=ammatillinenkoulutus&koulutusmuoto=lukiokoulutus&organisaatio=${MockOrganisaatiot.stadinAmmattiopisto}",
        ),
      ))
    }

    "Rajaa koulutusmuodon mukaan" in {
      val user = MockUsers.helsinkiAiemmatOpiskeluoikeudet
      val queryId = addQuerySuccessfully(query.copy(koulutusmuoto = Some(List("perusopetus"))), user)(_.queryId)
      val complete = waitForCompletion(queryId, user)
      verifyResultAndContent(oppijanTiedosto(complete.files).get, user) {
        koulutusmuodot(JsonMethods.parse(body)) shouldBe List("perusopetus")
      }
    }

    "Suodattaa sensitiiviset kentät ilman LUOTTAMUKSELLINEN_KAIKKI_TIEDOT-roolia" in {
      val luottamuksellinen = MockUsers.helsinkiAiemmatOpiskeluoikeudet
      val queryId1 = addQuerySuccessfully(query, luottamuksellinen)(_.queryId)
      verifyResultAndContent(oppijanTiedosto(waitForCompletion(queryId1, luottamuksellinen).files).get, luottamuksellinen) {
        lukionSisäoppilaitosmainenMajoitus(JsonMethods.parse(body)) should not be JNothing
      }

      val eiLuottamuksellinen = MockUsers.helsinkiAiemmatOpiskeluoikeudetEiLuottamuksellinen
      val queryId2 = addQuerySuccessfully(query, eiLuottamuksellinen)(_.queryId)
      verifyResultAndContent(oppijanTiedosto(waitForCompletion(queryId2, eiLuottamuksellinen).files).get, eiLuottamuksellinen) {
        val json = JsonMethods.parse(body)
        koulutusmuodot(json).sorted shouldBe List("lukiokoulutus", "perusopetus")
        lukionSisäoppilaitosmainenMajoitus(json) shouldBe JNothing
      }
    }

    "Ei palauta tiedostoa oppijalle, jolla ei ole aiempia opiskeluoikeuksia muualla" in {
      val user = MockUsers.helsinkiAiemmatOpiskeluoikeudet
      val queryId = addQuerySuccessfully(query, user)(_.queryId)
      val complete = waitForCompletion(queryId, user)
      oppijanTiedosto(complete.files, KoskiSpecificMockOppijat.eero.oid) shouldBe None
    }

    "Master/slave -yhdistys: slave-oidin aiemmat opiskeluoikeudet ryhmitellään masterin alle" in {
      val user = MockUsers.helsinkiAiemmatOpiskeluoikeudet
      val slaveOid = KoskiSpecificMockOppijat.slave.henkilö.oid
      val masterOid = KoskiSpecificMockOppijat.master.oid
      createOrUpdate(KoskiSpecificMockOppijat.slave.henkilö, makeOpiskeluoikeus(alkamispäivä = LocalDate.of(2020, 1, 1)))

      val queryId = addQuerySuccessfully(query, user)(_.queryId)
      val complete = waitForCompletion(queryId, user)

      oppijanTiedosto(complete.files, slaveOid) shouldBe None
      val masterFile = oppijanTiedosto(complete.files, masterOid)
      masterFile shouldBe defined
      verifyResultAndContent(masterFile.get, user) {
        val json = JsonMethods.parse(body)
        (json \ "henkilö" \ "oid").extract[String] shouldBe masterOid
        (json \ "linkitetytOidit").extract[List[String]] should contain(slaveOid)
        koulutusmuodot(json) should contain("lukiokoulutus")
      }
    }

    "Ei palauta muiden organisaatioiden opiskeluoikeuksia, jotka ovat alkaneet omien opintojen alkamisen jälkeen" in {
      val oppija = KoskiSpecificMockOppijat.ylioppilasLukiolainen
      createOrUpdate(oppija, makeOpiskeluoikeus(alkamispäivä = LocalDate.of(2020, 1, 1)))
      createOrUpdate(oppija, AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.omnia, alkamispäivä = LocalDate.of(2021, 1, 1)))

      val files = haeTulostiedostot()
      verifyResultAndContent(oppijanTiedosto(files, oppija.oid).get, MockUsers.helsinkiAiemmatOpiskeluoikeudet) {
        val json = JsonMethods.parse(body)
        koulutusmuodot(json) shouldBe List("lukiokoulutus")
        oppilaitokset(json) shouldBe Set(MockOrganisaatiot.jyväskylänNormaalikoulu)
      }
    }

    "Ei palauta oman oppilaitoksen päättyneitä opiskeluoikeuksia" in {
      val oppija = KoskiSpecificMockOppijat.ammattilainen
      createOrUpdate(oppija, makeOpiskeluoikeus(alkamispäivä = LocalDate.of(2020, 1, 1)))
      createOrUpdate(oppija, PerusopetusExampleData.päättötodistusOpiskeluoikeus())

      val files = haeTulostiedostot()
      verifyResultAndContent(oppijanTiedosto(files, oppija.oid).get, MockUsers.helsinkiAiemmatOpiskeluoikeudet) {
        val json = JsonMethods.parse(body)
        koulutusmuodot(json) shouldBe List("perusopetus")
        oppilaitokset(json) shouldBe Set(MockOrganisaatiot.jyväskylänNormaalikoulu)
      }
    }

    "Ei palauta tiedostoa oppijalle, jonka opiskeluoikeus omassa oppilaitoksessa on päättynyt" in {
      val oppija = KoskiSpecificMockOppijat.tero
      createOrUpdate(oppija, päättymispäivällä(makeOpiskeluoikeus(alkamispäivä = LocalDate.of(2015, 1, 1)), LocalDate.of(2018, 1, 1)))
      createOrUpdate(oppija, PerusopetusExampleData.päättötodistusOpiskeluoikeus())

      oppijanTiedosto(haeTulostiedostot(), oppija.oid) shouldBe None
    }

    "Toisen käyttäjän kyselyä ei näe" in {
      val queryId = addQuerySuccessfully(query, MockUsers.helsinkiAiemmatOpiskeluoikeudet)(_.queryId)
      getQuery(queryId, MockUsers.jyväskylänKatselijaEsiopetus) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound())
      }
    }
  }
}

object MassaluovutusAiemmatOpiskeluoikeudetSpec {
  val oletuskäyttäjä: UserWithPassword = MockUsers.helsinkiAiemmatOpiskeluoikeudet
}
