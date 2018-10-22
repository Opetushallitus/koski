package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.api.{LocalJettyHttpSpecification, OpiskeluoikeusTestMethods}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.AuditLogTester
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class LuovutuspalveluSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethods with Matchers with BeforeAndAfterAll {

  "Luovutuspalvelu hetu-API" - {
    "Palauttaa oikean näköisen vastauksen" in {
      val hetu = MockOppijat.eero.hetu.get
      postHetu(hetu, List("ammatillinenkoulutus")) {
        verifyResponseStatusOk()
        val resp = JsonSerializer.parse[HetuResponseV1](body)
        resp.henkilö.hetu should equal(Some(hetu))
        resp.opiskeluoikeudet.map(_.tyyppi.koodiarvo).distinct should equal(List("ammatillinenkoulutus"))
      }
    }
    "Palauttaa 404 jos henkilöä ei löydy" in {
      postHetu("150505-085R", List("ammatillinenkoulutus")) {
        verifyResponseStatus(404, ErrorMatcher.regex(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia, ".*".r))
      }
    }
    "Palauttaa 404 jos henkilölle ei löydy opiskeluoikeuksia (annetuilla rajauksilla)" in {
      postHetu(MockOppijat.eero.hetu.get, List("ibtutkinto")) {
        verifyResponseStatus(404, ErrorMatcher.regex(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia, ".*".r))
      }
    }
    "Palauttaa 404 jos henkilölle ei löydy opiskeluoikeuksia (ollenkaan)" in {
      postHetu(MockOppijat.eiKoskessa.hetu.get, List("ibtutkinto")) {
        verifyResponseStatus(404, ErrorMatcher.regex(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia, ".*".r))
      }
    }
    "Palauttaa 503 jos Virta ei vastaa" in {
      postHetu(MockOppijat.virtaEiVastaa.hetu.get, List("korkeakoulutus")) {
        verifyResponseStatus(503, KoskiErrorCategory.unavailable.virta())
      }
    }
    "Tuottaa oikean audit log viestin" in {
      AuditLogTester.clearMessages
      postHetu(MockOppijat.eero.hetu.get, List("ammatillinenkoulutus")) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN"))
      }
    }
    "Palauttaa 400 jos v-kentässä tuntematon arvo" in {
      post(
        "api/luovutuspalvelu/hetu",
        JsonSerializer.writeWithRoot(HetuRequestV1(666, MockOppijat.eero.hetu.get, List("ammatillinenkoulutus"), None)),
        headers = authHeaders() ++ jsonContent
      ) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Tuntematon versio"))
      }
    }
    "Palauttaa 400 jos opiskeluoikeudenTyypit-listassa tuntematon arvo" in {
      postHetu(MockOppijat.eero.hetu.get, List("foobar")) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Tuntematon opiskeluoikeudenTyyppi"))
      }
    }
  }

  private def postHetu[A](hetu: String, opiskeluoikeudenTyypit: List[String])(f: => A): A = {
    post(
      "api/luovutuspalvelu/hetu",
      JsonSerializer.writeWithRoot(HetuRequestV1(1, hetu, opiskeluoikeudenTyypit, None)),
      headers = authHeaders() ++ jsonContent
    )(f)
  }
}
