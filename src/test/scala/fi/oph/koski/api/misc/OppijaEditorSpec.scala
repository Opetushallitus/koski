package fi.oph.koski.api.misc

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema.AmmatillinenOpiskeluoikeus
import org.json4s.jackson.JsonMethods
import org.json4s.{JBool, JInt, JNothing, JString, JValue}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class OppijaEditorSpec extends AnyFreeSpec with Matchers with KoskiHttpSpec with OpiskeluoikeusTestMethodsAmmatillinen {

  "GET /api/editor/:oid" - {
    "with valid oid" in {
    AuditLogTester.clearMessages()
      get("api/editor/" + KoskiSpecificMockOppijat.eero.oid, headers = authHeaders()) {
        verifyResponseStatusOk()
        AuditLogTester.verifyLastAuditLogMessageForOperation(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN"))
      }
    }
    "with version number" in {
      val opiskeluoikeusOid = lastOpiskeluoikeus(KoskiSpecificMockOppijat.eero.oid).oid.get
    AuditLogTester.clearMessages()
      get("api/editor/" + KoskiSpecificMockOppijat.eero.oid, params = List("opiskeluoikeus" -> opiskeluoikeusOid, "versionumero" -> "1"), headers = authHeaders()) {
        verifyResponseStatusOk()
        AuditLogTester.verifyLastAuditLogMessageForOperation(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN"))
      }
    }
    "with version number returns the other opiskeluoikeudet too, the requested one as a read-only historical version" in resetFixturesAfter {
      val oppija = KoskiSpecificMockOppijat.moniaEriOpiskeluoikeuksia
      val ammatillinen = getOpiskeluoikeudet(oppija.oid).collectFirst { case oo: AmmatillinenOpiskeluoikeus => oo }.get
      val ammatillinenOid = ammatillinen.oid.get
      createOrUpdate(oppija, ammatillinen.copy(arvioituPäättymispäivä = Some(LocalDate.now)))

      val nykyiset = opiskeluoikeusModels(oppija.oid)
      val versiossa = opiskeluoikeusModels(oppija.oid, List("opiskeluoikeus" -> ammatillinenOid, "versionumero" -> "1"))

      versiossa.map(oid) should contain theSameElementsAs nykyiset.map(oid)

      val (versioitu, muut) = versiossa.partition(oid(_) == ammatillinenOid)
      versioitu.map(data(_, "versionumero")) should equal(List(JInt(1)))
      versioitu.map(muokattavuus) should equal(List((ammatillinenOid, JBool(false), JBool(false))))
      muut.map(muokattavuus) should contain theSameElementsAs nykyiset.filterNot(oid(_) == ammatillinenOid).map(muokattavuus)
    }
    "with invalid oid" in {
      get("api/editor/blerg", headers = authHeaders()) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.virheellinenHenkilöOid("Virheellinen oid: blerg. Esimerkki oikeasta muodosta: 1.2.246.562.24.00000000001."))
      }
    }
    "with unknown oid" in {
      get("api/editor/1.2.246.562.24.90000000001", headers = authHeaders()) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa 1.2.246.562.24.90000000001 ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
      }
    }
    "with Virta error" in {
      get("api/editor/" + KoskiSpecificMockOppijat.virtaEiVastaa.oid, headers = authHeaders()) {
        verifyResponseStatusOk()
        body should include("\"unavailable.virta\"")
      }
    }
  }

  "GET /api/omattiedot/editor" - {
    "with virkailija login -> forbidden" in {
    AuditLogTester.clearMessages()
      get("api/omattiedot/editor", headers = authHeaders(user = MockUsers.omattiedot)) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.vainKansalainen())
      }
    }
    "with kansalainen login -> logs KANSALAINEN_OPISKELUOIKEUS_KATSOMINEN" in {
    AuditLogTester.clearMessages()
      get("api/omattiedot/editor", headers = kansalainenLoginHeaders("190751-739W")) {
        verifyResponseStatusOk()
        AuditLogTester.verifyLastAuditLogMessageForOperation(Map("operation" -> "KANSALAINEN_OPISKELUOIKEUS_KATSOMINEN"))
      }
    }
    "with Virta error" in {
      get("api/omattiedot/editor", headers = kansalainenLoginHeaders(KoskiSpecificMockOppijat.virtaEiVastaa.hetu.get)) {
        verifyResponseStatusOk()
        body should include("\"unavailable.virta\"")
      }
    }
  }

  private def opiskeluoikeusModels(oppijaOid: String, params: List[(String, String)] = Nil): List[JValue] =
    get("api/editor/" + oppijaOid, params = params, headers = authHeaders()) {
      verifyResponseStatusOk()
      def items(model: JValue, key: String): List[JValue] =
        (model \ "value" \ "properties").children
          .filter(p => p \ "key" == JString(key))
          .flatMap(p => (p \ "model" \ "value").children)
      // Oppija -> tyypeittäin -> oppilaitoksittain -> opiskeluoikeudet
      items(JsonMethods.parse(body), "opiskeluoikeudet")
        .flatMap(items(_, "opiskeluoikeudet"))
        .flatMap(items(_, "opiskeluoikeudet"))
    }

  private def data(opiskeluoikeus: JValue, key: String): JValue =
    (opiskeluoikeus \ "value" \ "properties").children
      .find(p => p \ "key" == JString(key))
      .map(_ \ "model" \ "value" \ "data")
      .getOrElse(JNothing)

  private def oid(opiskeluoikeus: JValue): String = data(opiskeluoikeus, "oid").asInstanceOf[JString].s

  private def muokattavuus(opiskeluoikeus: JValue) =
    (oid(opiskeluoikeus), opiskeluoikeus \ "editable", opiskeluoikeus \ "invalidatable")
}
