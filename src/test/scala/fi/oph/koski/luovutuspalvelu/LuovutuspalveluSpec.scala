package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.api.{LocalJettyHttpSpecification, OpiskeluoikeusTestMethods}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.MockUsers
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
        headers = authHeaders(MockUsers.luovutuspalveluKäyttäjä) ++ jsonContent
      ) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Tuntematon versio"))
      }
    }
    "Palauttaa 400 jos opiskeluoikeudenTyypit-listassa tuntematon arvo" in {
      postHetu(MockOppijat.eero.hetu.get, List("foobar")) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Tuntematon opiskeluoikeudentyyppi"))
      }
    }
    "Vaatii vähintään yhden opiskeluoikeudenTyypin" in {
      postHetu(MockOppijat.eero.hetu.get, List()) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Opiskeluoikeuden tyypit puuttuvat"))
      }
    }
  }

 "Luovutuspalvelu hetu massahaku API" - {
   "Palauttaa oikean näköisen vastauksen" in {
     val henkilot = Set(MockOppijat.amis, MockOppijat.eerola)
     postHetut(henkilot.map(_.hetu.get).toList, List("ammatillinenkoulutus")) {
       verifyResponseStatusOk()
       val resp = JsonSerializer.parse[Seq[HetuResponseV1]](body)
       resp.map(_.henkilö.oid).toSet should equal (henkilot.map(_.oid))
     }
   }

   "Palauttaa 400 jos liian monta hetua" in {
     val hetut = List.range(0, 1001).map(_.toString)
     hetut.length should be > 1000
     postHetut(hetut, List("ammatillinenkoulutus")) {
       verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Liian monta hetua, enintään 1000 sallittu"))
     }
   }

   "Palauttaa 400 jos rajapinnan versionumero ei ole 1" in {
     val hetut = List(MockOppijat.eerola.hetu.get)
     postHetut(hetut, List("ammatillinenkoulutus"), 2) {
       verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Tuntematon versio"))
     }
   }

   "Palauttaa 400 jos hetu on epävalidi" in {
     val hetut = List("1235-123",  "456-456")
     postHetut(hetut, List("ammatillinenkoulutus")) {
       verifyResponseStatus(400, Nil)
       response.body should include ("Virheellinen muoto hetulla: ")
     }
   }

   "Palauttaa 400 jos tutkintotyyppi ei ole validi" in {
     val hetut = List(MockOppijat.amis.hetu.get, MockOppijat.eerola.hetu.get)
     val ooTyypit = List("ammatillinenkoulutus", "epävalidityyppi")
     postHetut(hetut, ooTyypit) {
       verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Tuntematon opiskeluoikeudentyyppi"))
     }
   }

   "Palauttaa 400 jos tutkintotyyppiä ei voi hakea massahaulla" in {
     val hetut = List(MockOppijat.amis.hetu.get)
     val ooTyypit = List("ammatillinenkoulutus", "ylioppilastutkinto")
     postHetut(hetut, ooTyypit) {
       verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Korkeakoulutus tai ylioppilastutkinto ei sallittu"))
     }
   }

   "Vaatii vähintään yhden opiskeluoikeudenTyypin" in {
     val hetut = List(MockOppijat.amis.hetu.get)
     val ooTyypit = List()
     postHetut(hetut, ooTyypit) {
       verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Opiskeluoikeuden tyypit puuttuvat"))
     }
   }

   "Tuottaa oikean audit log viestin" in {
     AuditLogTester.clearMessages
     val henkilo = MockOppijat.amis
     postHetut(List(henkilo.hetu.get), List("ammatillinenkoulutus")) {
       verifyResponseStatusOk()
       AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN", "target" -> Map("oppijaHenkiloOid" -> henkilo.oid.toString)))
     }
   }
 }

  private def postHetu[A](hetu: String, opiskeluoikeudenTyypit: List[String])(f: => A): A = {
    post(
      "api/luovutuspalvelu/hetu",
      JsonSerializer.writeWithRoot(HetuRequestV1(1, hetu, opiskeluoikeudenTyypit, None)),
      headers = authHeaders(MockUsers.luovutuspalveluKäyttäjä) ++ jsonContent
    )(f)
  }

  private def postHetut[A](hetut: List[String], opiskeluoikeudenTyypit: List[String], v: Int = 1)(f: => A): A = {
    post(
      "api/luovutuspalvelu/hetut",
      JsonSerializer.writeWithRoot(BulkHetuRequestV1(v, hetut, opiskeluoikeudenTyypit, None)),
      headers = authHeaders(MockUsers.luovutuspalveluKäyttäjä) ++ jsonContent
    )(f)
  }
}
