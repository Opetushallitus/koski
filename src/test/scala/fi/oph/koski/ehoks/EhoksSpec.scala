package fi.oph.koski.ehoks

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.{OpiskeluoikeudenMitätöintiJaPoistoTestMethods, OpiskeluoikeusTestMethodsAmmatillinen}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.log.AuditLogTester
import org.json4s.jackson.JsonMethods
import org.json4s.{JArray, JObject, JString}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class EhoksSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with Matchers
    with BeforeAndAfterAll
    with OpiskeluoikeusTestMethodsAmmatillinen
    with OpiskeluoikeudenMitätöintiJaPoistoTestMethods {

  private val ehoksKäyttäjä = MockUsers.ophkatselija

  override def afterAll(): Unit = {
    resetFixtures()
    super.afterAll()
  }

  "GET /api/ehoks/oppija/:oid" - {
    "palauttaa ammatillisen opiskeluoikeuden vain eHOKSin tarvitsemin kentin" in {
      getEhoksOppija(KoskiSpecificMockOppijat.ammattilainen.oid) {
        verifyResponseStatusOk()
        val oppija = JsonSerializer.parse[EhoksOppija](body, ignoreExtras = true)
        oppija.henkilö.oid should equal(KoskiSpecificMockOppijat.ammattilainen.oid)
        oppija.opiskeluoikeudet.map(_.tyyppi.koodiarvo) should equal(List("ammatillinenkoulutus"))

        val oo = oppija.opiskeluoikeudet.head
        oo.tila.opiskeluoikeusjaksot should not be empty
        oo.koulutustoimija.flatMap(_.nimi) should not be empty
        val suoritus = oo.suoritukset.head
        suoritus.tyyppi.koodiarvo should equal("ammatillinentutkinto")
        suoritus.koulutusmoduuli.tunniste.nimi should not be empty
        suoritus.vahvistus should not be empty

        val json = JsonMethods.parse(body)
        val ooJson = (json \ "opiskeluoikeudet")(0).asInstanceOf[JObject]
        ooJson.values.keySet should equal(Set(
          "oid", "tyyppi", "oppilaitos", "koulutustoimija", "arvioituPäättymispäivä", "tila", "suoritukset", "alkamispäivä", "päättymispäivä"
        ))
        (ooJson \ "tyyppi").asInstanceOf[JObject].values.keySet should equal(Set("koodiarvo"))
        (ooJson \ "oppilaitos").asInstanceOf[JObject].values.keySet should equal(Set("oid"))
        (ooJson \ "suoritukset")(0).asInstanceOf[JObject].values.keySet should equal(Set(
          "tyyppi", "koulutusmoduuli", "osaamisala", "tutkintonimike", "suorituskieli", "toimipiste", "vahvistus"
        ))
      }
    }

    "palauttaa TUVA-opiskeluoikeuden" in {
      getEhoksOppija(KoskiSpecificMockOppijat.tuva.oid) {
        verifyResponseStatusOk()
        JsonSerializer.parse[EhoksOppija](body, ignoreExtras = true).opiskeluoikeudet.map(_.tyyppi.koodiarvo) should contain("tuva")
      }
    }

    "ei palauta muita opiskeluoikeuksia" in {
      getEhoksOppija(KoskiSpecificMockOppijat.lukiolainen.oid) {
        verifyResponseStatusOk()
        JsonSerializer.parse[EhoksOppija](body, ignoreExtras = true).opiskeluoikeudet should be(empty)
      }
    }

    "kirjoittaa audit-lokin" in {
      AuditLogTester.clearMessages()
      getEhoksOppija(KoskiSpecificMockOppijat.ammattilainen.oid) {
        verifyResponseStatusOk()
        AuditLogTester.verifyOnlyAuditLogMessageForOperation(Map(
          "operation" -> "OPISKELUOIKEUS_KATSOMINEN",
          "target" -> Map("oppijaHenkiloOid" -> KoskiSpecificMockOppijat.ammattilainen.oid)
        ))
      }
    }

    "virheellinen oid" in {
      getEhoksOppija("123") {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.virheellinenHenkilöOid("Virheellinen oid: 123. Esimerkki oikeasta muodosta: 1.2.246.562.24.00000000001."))
      }
    }
  }

  "GET /api/ehoks/opiskeluoikeus/:oid" - {
    "palauttaa ammatillisen opiskeluoikeuden" in {
      val ooOid = lastOpiskeluoikeus(KoskiSpecificMockOppijat.ammattilainen.oid, MockUsers.paakayttaja).oid.get
      AuditLogTester.clearMessages()
      getEhoksOpiskeluoikeus(ooOid) {
        verifyResponseStatusOk()
        JsonSerializer.parse[EhoksOpiskeluoikeus](body, ignoreExtras = true).oid should equal(ooOid)
        AuditLogTester.verifyOnlyAuditLogMessageForOperation(Map(
          "operation" -> "OPISKELUOIKEUS_KATSOMINEN",
          "target" -> Map("oppijaHenkiloOid" -> KoskiSpecificMockOppijat.ammattilainen.oid, "opiskeluoikeusOid" -> ooOid)
        ))
      }
    }

    "muun tyyppinen opiskeluoikeus palauttaa 404" in {
      val ooOid = lastOpiskeluoikeus(KoskiSpecificMockOppijat.lukiolainen.oid, MockUsers.paakayttaja).oid.get
      getEhoksOpiskeluoikeus(ooOid) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
      }
    }
  }

  "GET /api/ehoks/mitatoidyt" - {
    "palauttaa aikavälillä mitätöidyt ammatilliset opiskeluoikeudet" in {
      val tänään = LocalDate.now()
      val oo = createOpiskeluoikeus(KoskiSpecificMockOppijat.tyhjä, defaultOpiskeluoikeus, resetFixtures = true)
      val muuttumaton = lastOpiskeluoikeus(KoskiSpecificMockOppijat.ammattilainen.oid, MockUsers.paakayttaja)
      mitätöiOpiskeluoikeus(oo.oid.get)

      getMitätöidyt(Map("alku" -> tänään.toString)) {
        verifyResponseStatusOk()
        val oidit = mitätöidytOidit
        oidit should contain(oo.oid.get)
        oidit should not contain muuttumaton.oid.get
      }

      getMitätöidyt(Map("alku" -> tänään.minusYears(1).toString, "loppu" -> tänään.minusDays(1).toString)) {
        verifyResponseStatusOk()
        mitätöidytOidit should not contain oo.oid.get
      }
    }

    "alku puuttuu" in {
      getMitätöidyt(Map.empty) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.missing("Missing alku"))
      }
    }

    "alku loppua myöhemmin" in {
      getMitätöidyt(Map("alku" -> "2026-02-01", "loppu" -> "2026-01-01")) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("alku ei voi olla loppua myöhemmin"))
      }
    }

    "virheellinen päivämäärä" in {
      getMitätöidyt(Map("alku" -> "2026-01-01T00:00:00+02:00")) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.format.pvm())
      }
    }
  }

  "Käyttöoikeudet" - {
    "vain globaalit lukuoikeudet sallitaan" in {
      get(s"api/ehoks/oppija/${KoskiSpecificMockOppijat.ammattilainen.oid}", headers = authHeaders(MockUsers.kalle)) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden())
      }
      get("api/ehoks/mitatoidyt", Map("alku" -> "2026-01-01"), authHeaders(MockUsers.kalle)) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden())
      }
    }
  }

  private def getEhoksOppija[A](oid: String)(f: => A): A =
    get(s"api/ehoks/oppija/$oid", headers = authHeaders(ehoksKäyttäjä))(f)

  private def getEhoksOpiskeluoikeus[A](oid: String)(f: => A): A =
    get(s"api/ehoks/opiskeluoikeus/$oid", headers = authHeaders(ehoksKäyttäjä))(f)

  private def getMitätöidyt[A](params: Map[String, String])(f: => A): A =
    get("api/ehoks/mitatoidyt", params, authHeaders(ehoksKäyttäjä))(f)

  private def mitätöidytOidit: List[String] =
    JsonMethods.parse(body).asInstanceOf[JArray].arr.collect { case JString(s) => s }
}
