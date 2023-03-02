package fi.oph.koski.sure

import fi.oph.koski.api.{OpiskeluoikeudenMitätöintiJaPoistoTestMethods, OpiskeluoikeusTestMethodsAmmatillinen}
import fi.oph.koski.db.KoskiTables.KoskiOpiskeluOikeudet
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.documentation.AmmatillinenExampleData
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.MockUsers.{stadinAmmattiopistoKatselija, stadinVastuukäyttäjä}
import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema._
import fi.oph.koski.{DatabaseTestMethods, KoskiHttpSpec}
import fi.oph.scalaschema.{ExtractionContext, SchemaValidatingExtractor}
import org.json4s.JsonAST.{JArray, JBool}
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, JString, JValue}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class SureSpec extends AnyFreeSpec with KoskiHttpSpec with OpiskeluoikeusTestMethodsAmmatillinen with DatabaseTestMethods with OpiskeluoikeudenMitätöintiJaPoistoTestMethods with Matchers {
  private implicit val context: ExtractionContext = strictDeserialization
  private implicit val formats = DefaultFormats

  "Sure-rajapinnat" - {
    "/api/sure/oids" - {
      "Palauttaa pyydetyt OIDit" in {
        val henkilöOids = Set(KoskiSpecificMockOppijat.amis.oid, KoskiSpecificMockOppijat.lukiolainen.oid)
        postOids(henkilöOids) {
          verifyResponseStatusOk()
          val oppijat = SchemaValidatingExtractor.extract[List[Oppija]](body).right.get
          oppijat.map(_.henkilö).toSet should equal(henkilöOids.map(OidHenkilö))
        }
      }
      "Palauttaa oppijan muut opiskeluoikeudet, jos oppijalla on sekä VST-vapaatavoitteinen että muita opiskeluoikeuksia" in {
        resetFixtures()

        val oppija = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus

        val uusiOo = createOpiskeluoikeus(
          oppija = oppija,
          opiskeluoikeus = AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmisVahvistettuKoulutustoimijalla(),
          user = MockUsers.paakayttaja)

        val henkilöOids = Set(oppija).map(_.oid)

        postOids(henkilöOids) {
          verifyResponseStatusOk()

          val oppijat = SchemaValidatingExtractor.extract[List[Oppija]](body).right.get

          oppijat(0).opiskeluoikeudet.length should be(1)

          oppijat(0).opiskeluoikeudet(0).oid should be(uusiOo.oid)
        }
      }
      "Palauttaa tyhjän listan, jos oppijalla on vain VST-vapaatavoitteinen opiskeluoikeus" in {
        resetFixtures()

        val henkilöOids = Set(KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.oid)

        postOids(henkilöOids) {
          verifyResponseStatusOk()
          JsonMethods.parse(body) should equal(JArray(List.empty))
        }
      }
      "Palauttaa tyhjän listan, jos oppijalla on vain poistettu VST-vapaatavoitteinen opiskeluoikeus" in {
        val henkilöOids = Set(KoskiSpecificMockOppijat.poistettuOpiskeluoikeus.oid)

        postOids(henkilöOids) {
          verifyResponseStatusOk()
          JsonMethods.parse(body) should equal(JArray(List.empty))
        }
      }
      "Palauttaa tyhjän listan jos OIDia ei löydy" in {
        postOids(Seq("1.2.246.562.24.90000000001")) {
          verifyResponseStatusOk()
          JsonMethods.parse(body) should equal(JArray(List.empty))
        }
      }
      "Palauttaa virheen jos OID on virheellinen" in {
        postOids(Seq("foobar")) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.virheellinenHenkilöOid("Virheellinen oid: foobar. Esimerkki oikeasta muodosta: 1.2.246.562.24.00000000001."))
        }
      }
      "Palauttaa virheen jos liian monta OIDia" in {
        val oids = List.fill(2000)("1.2.246.562.24.90000000001")
        postOids(oids) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Liian monta oidia, enintään 1000 sallittu."))
        }
      }
      "Tuottaa oikean audit log viestin" in {
        val oids = Seq("1.2.246.562.24.90000000001", "1.2.246.562.24.90000000002", "1.2.246.562.24.90000000003", "1.2.246.562.24.90000000004")
        postOids(oids) {
          verifyResponseStatusOk()
          AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_HAKU", "target" -> Map("hakuEhto" -> "oids=1.2.246.562.24.90000000001,1.2.246.562.24.90000000002,...(4)")))
        }
      }
      "Luottamuksellinen data" - {
        "Näytetään käyttäjälle jolla on LUOTTAMUKSELLINEN_KAIKKI_TIEDOT-rooli" in {
          resetFixtures
          postOids(Seq(KoskiSpecificMockOppijat.eero.oid), user = stadinAmmattiopistoKatselija) {
            verifyResponseStatusOk()
            body should include("vankilaopetuksessa")
          }
        }
        "Piilotetaan käyttäjältä jolta puuttuu LUOTTAMUKSELLINEN_KAIKKI_TIEDOT-rooli" in {
          postOids(Seq(KoskiSpecificMockOppijat.eero.oid), user = stadinVastuukäyttäjä) {
            verifyResponseStatusOk()
            body should not include("vankilaopetuksessa")
          }
        }
      }
      "Lasketut kentät palautetaan" in {
        postOids(Seq(KoskiSpecificMockOppijat.valma.oid), user = stadinAmmattiopistoKatselija) {
          verifyResponseStatusOk()
          val parsedJson = JsonMethods.parse(body)
          val ensimmäisenOsasuorituksetArviointi = ((((parsedJson \ "opiskeluoikeudet") (0) \ "suoritukset") (0) \ "osasuoritukset") (0) \ "arviointi") (0)
          (ensimmäisenOsasuorituksetArviointi \ "hyväksytty") should equal(JArray(List(JBool(true))))
        }
      }

      "Hakee myös slave-oidille tallennetut opiskeluoikeudet" in {
        val oppijat = postOids(List(KoskiSpecificMockOppijat.master.oid)) {
          verifyResponseStatusOk()
          JsonSerializer.parse[List[JValue]](body).map(JsonSerializer.extract[Oppija](_))
        }
        oppijat.flatMap(_.opiskeluoikeudet).length should equal(2)
      }
    }

    "/api/sure/muuttuneet-oppijat" - {

      "Kysely aikaleimalla palauttaa kursorin (mutta ei rivejä)" in {
        muuttuneetAikaleimalla("2019-01-01T00:00:00+02:00") {
          verifyResponseStatusOk()
          val parsedJson = JsonMethods.parse(body)
          (parsedJson \ "result") should equal(JArray(Nil))
          (parsedJson \ "mayHaveMore") should equal(JBool(true))
          (parsedJson \ "nextCursor") shouldBe a[JString]
        }
      }

      "Kysely kursorilla palauttaa oikeat oidit" in {
        val cursor = muuttuneetAikaleimalla("2015-01-01T00:00:00+02:00")(extractCursor)
        val res = muuttuneetKursorilla(cursor)
        val expectedOids = runDbSync(KoskiOpiskeluOikeudet.sortBy(_.aikaleima).map(_.oppijaOid).take(5).result)
        res.result should contain theSameElementsAs(expectedOids)
        res.mayHaveMore should equal(true)
      }

      "Kursorilla iterointi palauttaa kaikki oidit" in {
        val cursor = muuttuneetAikaleimalla("2015-01-01T00:00:00+02:00")(extractCursor)
        val res = muuttuneetKursorillaIteroi(cursor)
        val pageCount = res.size
        // huom, kummassakaan näissä ei ole ".distinct"
        val oids = res.flatMap(_.result).sorted
        val expectedOids = runDbSync(KoskiOpiskeluOikeudet.map(_.oppijaOid).result).sorted
        pageCount should be > 5
        oids should contain theSameElementsAs(expectedOids)
      }

      "Kursorilla haku palauttaa muuttuneet" in {
        val cursor = muuttuneetAikaleimalla("2015-01-01T00:00:00+02:00")(extractCursor)
        val res1 = muuttuneetKursorilla(cursor, pageSize = 1000)

        val muuttuneetOidit = res1.result.distinct.take(3)
        runDbSync(KoskiOpiskeluOikeudet.filter(_.oppijaOid inSetBind muuttuneetOidit).map(_.luokka).update(Some("X")))
        val res2 = muuttuneetKursorilla(res1.nextCursor, pageSize = 1000)
        //res2.result.distinct should contain allElementsOf(muuttuneetOidit)
        res2.result.distinct should contain theSameElementsAs(muuttuneetOidit)
      }

      "Kursorilla haku ei palauta muutoksia jos muuttuneita ei ole" in {
        val cursor = muuttuneetAikaleimalla("2015-01-01T00:00:00+02:00")(extractCursor)
        val res1 = muuttuneetKursorilla(cursor, pageSize = 1000)
        val res2 = muuttuneetKursorilla(res1.nextCursor, pageSize = 1000)
        res2.result shouldBe empty
        res2.nextCursor should equal(res1.nextCursor)
      }

      "Hiljattain muuttuneet palautetaan useaan kertaan" in {
        val cursor = muuttuneetAikaleimalla("2015-01-01T00:00:00+02:00")(extractCursor)
        val kaikkiOidit = muuttuneetKursorilla(cursor, pageSize = 1000).result.distinct

        // Varmistetaan että kannassa on tasan 3 hiljattain muuttunutta riviä
        Thread.sleep(2000)
        val muuttuneetOidit1 = kaikkiOidit.take(3)
        val muuttuneetOidit2 = kaikkiOidit.drop(3).take(1)
        runDbSync(KoskiOpiskeluOikeudet.filter(_.oppijaOid inSetBind muuttuneetOidit1).map(_.luokka).update(Some("X")))

        // Ensimmäinen haku palauttaa kaikki oidit
        val res1 = muuttuneetKursorilla(cursor, pageSize = 1000, recentPageOverlapTestsOnly = 2)
        res1.result.distinct should contain theSameElementsAs(kaikkiOidit)

        // Toinen haku palauttaa nuo kahden sekunnin sisällä muuttuneet kolme riviä
        val res2 = muuttuneetKursorilla(res1.nextCursor, pageSize = 1000, recentPageOverlapTestsOnly = 2)
        res2.result.distinct should contain theSameElementsAs(muuttuneetOidit1)

        // ...ja jos siellä on vielä uusia muuttuneita, niin myös ne:
        runDbSync(KoskiOpiskeluOikeudet.filter(_.oppijaOid inSetBind muuttuneetOidit2).map(_.luokka).update(Some("X")))
        // huom, tässä tarkoituksella "res1.nextCursor" eikä res2
        val res3 = muuttuneetKursorilla(res1.nextCursor, pageSize = 1000, recentPageOverlapTestsOnly = 2)
        res3.result.distinct should contain theSameElementsAs(muuttuneetOidit1 ++ muuttuneetOidit2)
      }

      "Kursorilla iterointi palauttaa kaikki oidit myös jos niillä on sama aikaleima" in {
        val cursor = muuttuneetAikaleimalla("2015-01-01T00:00:00+02:00")(extractCursor)
        runDbSync(KoskiOpiskeluOikeudet.map(_.luokka).update(Some("X")))
        val res = muuttuneetKursorillaIteroi(cursor)
        val pageCount = res.size
        // huom, kummassakaan näissä ei ole ".distinct"
        val oids = res.flatMap(_.result).sorted
        val expectedOids = runDbSync(KoskiOpiskeluOikeudet.map(_.oppijaOid).result).sorted
        pageCount should be > 5
        oids should contain theSameElementsAs(expectedOids)
      }

      "Myös mitätöidyt lasketaan muuttuneiksi" in {
        val cursor = muuttuneetAikaleimalla("2015-01-01T00:00:00+02:00")(extractCursor)
        val res1 = muuttuneetKursorilla(cursor, pageSize = 1000)

        val oppijaOid = KoskiSpecificMockOppijat.ibFinal.oid
        val opiskeluoikeusOid = runDbSync(KoskiOpiskeluOikeudet.filter(_.oppijaOid === oppijaOid).map(_.oid).result).head
        delete(s"api/opiskeluoikeus/$opiskeluoikeusOid", headers = authHeaders(MockUsers.paakayttaja)) {
          verifyResponseStatusOk()
        }

        val res2 = muuttuneetKursorilla(res1.nextCursor)
        res2.result should equal(Seq(oppijaOid))
      }

      "Myös poistetut lasketaan muuttuneiksi" in {
        resetFixtures()

        val cursor = muuttuneetAikaleimalla("2015-01-01T00:00:00+02:00")(extractCursor)
        val res1 = muuttuneetKursorilla(cursor, pageSize = 1000)

        val oppijaOid = KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.oid
        val opiskeluoikeusOid = runDbSync(KoskiOpiskeluOikeudet.filter(_.oppijaOid === oppijaOid).map(_.oid).result).head

        poistaOpiskeluoikeus(oppijaOid, opiskeluoikeusOid)

        val res2 = muuttuneetKursorilla(res1.nextCursor)
        res2.result should equal(Seq(oppijaOid))
      }

      "Palauttaa virheen jos annetaan sekä aikaleima että kursori" in {
        val cursor = muuttuneetAikaleimalla("2019-01-01T00:00:00+02:00")(extractCursor)
        muuttuneetAikaleimalla("2019-01-01T00:00:00+02:00", cursor = cursor) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Pitää antaa joko timestamp tai cursor"))
        }
      }
      "Palauttaa virheen jos sekä aikaleima että kursori puuttuu" in {
        muuttuneetAikaleimalla("") {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Pitää antaa joko timestamp tai cursor"))
        }
      }
      "Palauttaa virheen jos annetaan virheellinen aikaleima" in {
        muuttuneetAikaleimalla(timestamp = "2019-01-01T00:00:00") {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.format.pvm())
        }
      }
      "Palauttaa virheen jos annetaan virheellinen kursori" in {
        muuttuneetAikaleimalla(timestamp = "", cursor = "v9,2019-01-01T00:00:00Z") {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Virheellinen cursor"))
        }
      }
      "Palauttaa virheen jos annetaan virheellinen pageSize" in {
        get("api/sure/muuttuneet-oppijat", Map("timestamp" -> "2015-01-01T00:00:00+02:00", "pageSize" -> "0"), authHeaders(MockUsers.paakayttaja)) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("pageSize pitää olla välillä 2-5000"))
        }
      }
      "Palauttaa virheen jos ei ole globaaleja lukuoikeuksia" in {
        get("api/sure/muuttuneet-oppijat", Map("timestamp" -> "2015-01-01T00:00:00+02:00"), authHeaders(MockUsers.kalle)) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden("Käyttäjällä ei ole oikeuksia annetun organisaation tietoihin."))
        }
      }
    }
  }

  private def postOids[A](oids: Iterable[String], user: UserWithPassword = defaultUser)(f: => A) =
    post("api/sure/oids", "[" + oids.map("\"" + _ + "\"").mkString(",") + "]", authHeaders(user) ++ jsonContent)(f)

  private def muuttuneetAikaleimalla[A](timestamp: String, cursor: String = "")(f: => A) = {
    val params = List("timestamp" -> timestamp, "cursor" -> cursor).filter(_._2 != "").toMap
    get("api/sure/muuttuneet-oppijat", params, authHeaders(MockUsers.paakayttaja))(f)
  }

  private def muuttuneetKursorilla(cursor: String, pageSize: Int = 5, recentPageOverlapTestsOnly: Int = 0): MuuttuneetOppijatResponse = {
    get("api/sure/muuttuneet-oppijat", Map("cursor" -> cursor, "pageSize" -> pageSize.toString, "recentPageOverlapTestsOnly" -> recentPageOverlapTestsOnly.toString), authHeaders(MockUsers.paakayttaja)) {
      verifyResponseStatusOk()
      SchemaValidatingExtractor.extract[MuuttuneetOppijatResponse](body).right.get
    }
  }

  private def muuttuneetKursorillaIteroi(firstCursor: String): Seq[MuuttuneetOppijatResponse] = {
    val responses = collection.mutable.ArrayBuffer[MuuttuneetOppijatResponse]()
    var cursor = firstCursor
    while (true) {
      val res = muuttuneetKursorilla(cursor)
      responses += res
      if (res.mayHaveMore) {
        cursor = res.nextCursor
      } else {
        return responses
      }
    }
    responses
  }

  private def extractCursor = {
    verifyResponseStatusOk()
    (JsonMethods.parse(body) \ "nextCursor").extract[String]
  }
}

