package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.api.{LocalJettyHttpSpecification, OpiskeluoikeusTestMethods}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.log.AuditLogTester
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class LuovutuspalveluSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethods with Matchers with BeforeAndAfterAll {

  "Luovutuspalvelu hetu-API" - {
    "Palauttaa oikean näköisen vastauksen" in {
      val hetu = KoskiSpecificMockOppijat.eero.hetu.get
      postHetu(hetu, List("ammatillinenkoulutus")) {
        verifyResponseStatusOk()
        val resp = JsonSerializer.parse[LuovutuspalveluResponseV1](body)
        resp.henkilö.hetu should equal(Some(hetu))
        resp.opiskeluoikeudet.map(_.tyyppi.koodiarvo).distinct should equal(List("ammatillinenkoulutus"))
      }
    }
    "Palauttaa 404 jos henkilöä ei löydy" in {
      postHetu("150505-085R", List("ammatillinenkoulutus")) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa (hetu) ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
      }
    }
    "Ei vuoda oidia paluuviestissa jos henkilö löytyy oppijanumerorekisteristä muttei Koskesta" in {
      postHetu("270366-697B", List("ammatillinenkoulutus")) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa (hetu) ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
      }
    }
    "Palauttaa 404 jos henkilölle ei löydy opiskeluoikeuksia (annetuilla rajauksilla)" in {
      postHetu(KoskiSpecificMockOppijat.eero.hetu.get, List("ibtutkinto")) {
        verifyResponseStatus(404, ErrorMatcher.regex(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia, ".*".r))
      }
    }
    "Palauttaa 404 jos henkilölle ei löydy opiskeluoikeuksia (ollenkaan)" in {
      postHetu(KoskiSpecificMockOppijat.eiKoskessa.hetu.get, List("ibtutkinto")) {
        verifyResponseStatus(404, ErrorMatcher.regex(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia, ".*".r))
      }
    }
    "Palauttaa 503 jos Virta ei vastaa" in {
      postHetu(KoskiSpecificMockOppijat.virtaEiVastaa.hetu.get, List("korkeakoulutus")) {
        verifyResponseStatus(503, KoskiErrorCategory.unavailable.virta())
      }
    }
    "Tuottaa oikean audit log viestin" in {
      AuditLogTester.clearMessages
      postHetu(KoskiSpecificMockOppijat.eero.hetu.get, List("ammatillinenkoulutus")) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN"))
      }
    }
    "Palauttaa 400 jos v-kentässä tuntematon arvo" in {
      post(
        "api/luovutuspalvelu/hetu",
        JsonSerializer.writeWithRoot(HetuRequestV1(666, KoskiSpecificMockOppijat.eero.hetu.get, List("ammatillinenkoulutus"))),
        headers = authHeaders(MockUsers.luovutuspalveluKäyttäjä) ++ jsonContent
      ) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Tuntematon versio"))
      }
    }
    "Palauttaa 400 jos opiskeluoikeudenTyypit-listassa tuntematon arvo" in {
      postHetu(KoskiSpecificMockOppijat.eero.hetu.get, List("foobar")) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Tuntematon opiskeluoikeudentyyppi"))
      }
    }
    "Vaatii vähintään yhden opiskeluoikeudenTyypin" in {
      postHetu(KoskiSpecificMockOppijat.eero.hetu.get, List()) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Opiskeluoikeuden tyypit puuttuvat"))
      }
    }
  }

  "Oid rajapinta" - {
    "Oikealla oidilla" in {
      val oid = KoskiSpecificMockOppijat.eero.oid
      postOid(oid, List("ammatillinenkoulutus")) {
        verifyResponseStatusOk()
        val resp = JsonSerializer.parse[LuovutuspalveluResponseV1](body)
        resp.henkilö.oid should equal(oid)
        resp.opiskeluoikeudet.map(_.tyyppi.koodiarvo).distinct should equal(List("ammatillinenkoulutus"))
      }
    }
    "Palauttaa 404 jos henkilöä ei löydy" in {
      postOid("1.2.246.562.24.45141690981", List("ammatillinenkoulutus")) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa 1.2.246.562.24.45141690981 ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
      }
    }
    "Palauttaa 404 jos henkilölle ei löydy opiskeluoikeuksia (annetuilla rajauksilla)" in {
      postOid(KoskiSpecificMockOppijat.eero.oid, List("ibtutkinto")) {
        verifyResponseStatus(404, ErrorMatcher.regex(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia, ".*".r))
      }
    }
    "Palauttaa 404 jos henkilölle ei löydy opiskeluoikeuksia (ollenkaan)" in {
      postOid(KoskiSpecificMockOppijat.eiKoskessa.oid, List("ibtutkinto")) {
        verifyResponseStatus(404, ErrorMatcher.regex(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia, ".*".r))
      }
    }
    "Palauttaa 503 jos Virta ei vastaa" in {
      postOid(KoskiSpecificMockOppijat.virtaEiVastaa.oid, List("korkeakoulutus")) {
        verifyResponseStatus(503, KoskiErrorCategory.unavailable.virta())
      }
    }
    "Tuottaa oikean audit log viestin" in {
      AuditLogTester.clearMessages
      postOid(KoskiSpecificMockOppijat.eero.oid, List("ammatillinenkoulutus")) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN"))
      }
    }
    "Palauttaa 400 jos v-kentässä tuntematon arvo" in {
      post(
        "api/luovutuspalvelu/oid",
        JsonSerializer.writeWithRoot(OidRequestV1(666, KoskiSpecificMockOppijat.eero.oid, List("ammatillinenkoulutus"))),
        headers = authHeaders(MockUsers.luovutuspalveluKäyttäjä) ++ jsonContent
      ) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Tuntematon versio"))
      }
    }
    "Palauttaa 400 jos opiskeluoikeudenTyypit-listassa tuntematon arvo" in {
      postOid(KoskiSpecificMockOppijat.eero.oid, List("foobar")) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Tuntematon opiskeluoikeudentyyppi"))
      }
    }
    "Vaatii vähintään yhden opiskeluoikeudenTyypin" in {
      postOid(KoskiSpecificMockOppijat.eero.oid, List()) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Opiskeluoikeuden tyypit puuttuvat"))
      }
    }

    "Hakee linkitetyt tiedot" - {
      "Masterin oidilla" in {
        postOid(KoskiSpecificMockOppijat.master.oid, List("perusopetus", "lukiokoulutus")) {
          verifyResponseStatusOk()
          val resp = JsonSerializer.parse[LuovutuspalveluResponseV1](body)
          resp.henkilö.oid should equal(KoskiSpecificMockOppijat.master.oid)
          resp.opiskeluoikeudet.map(_.tyyppi.koodiarvo) should equal(List("perusopetus", "lukiokoulutus"))
        }
      }

      "Slaven oidilla" in {
        postOid(KoskiSpecificMockOppijat.slave.henkilö.oid, List("perusopetus", "lukiokoulutus")) {
          verifyResponseStatusOk()
          val resp = JsonSerializer.parse[LuovutuspalveluResponseV1](body)
          resp.henkilö.oid should equal(KoskiSpecificMockOppijat.master.oid)
          resp.opiskeluoikeudet.map(_.tyyppi.koodiarvo) should equal(List("perusopetus", "lukiokoulutus"))
        }
      }
    }
  }

 "Luovutuspalvelu hetu massahaku API" - {
   "Palauttaa oikean näköisen vastauksen" in {
     val henkilot = Set(KoskiSpecificMockOppijat.amis, KoskiSpecificMockOppijat.eerola)
     postHetut(henkilot.map(_.hetu.get).toList, List("ammatillinenkoulutus")) {
       verifyResponseStatusOk()
       val resp = JsonSerializer.parse[Seq[LuovutuspalveluResponseV1]](body)
       resp.map(_.henkilö.oid).toSet should equal (henkilot.map(_.oid))
     }
   }

   "Hakee myös linkitetyt tiedot" in {
     postHetut(List(KoskiSpecificMockOppijat.master.hetu.get), List("perusopetus", "lukiokoulutus")) {
       verifyResponseStatusOk()
       val resp = JsonSerializer.parse[Seq[LuovutuspalveluResponseV1]](body)
       resp.flatMap(_.opiskeluoikeudet.map(_.tyyppi.koodiarvo)) should equal(List("perusopetus", "lukiokoulutus"))
     }
   }

   "Palauttaa valitut opiskeluoikeudenTyypit" in {
     val henkilot = Set(KoskiSpecificMockOppijat.amis, KoskiSpecificMockOppijat.lukiolainen, KoskiSpecificMockOppijat.ysiluokkalainen)
     val opiskeluoikeudenTyypit = Set("ammatillinenkoulutus", "lukiokoulutus", "perusopetus")
     postHetut(henkilot.map(_.hetu.get).toList, opiskeluoikeudenTyypit.toList) {
       verifyResponseStatusOk()
       val resp = JsonSerializer.parse[Seq[LuovutuspalveluResponseV1]](body)
       val actualOpiskeluoikeudenTyypit = resp.flatMap(_.opiskeluoikeudet.map(_.tyyppi.koodiarvo)).toSet
       actualOpiskeluoikeudenTyypit should equal (opiskeluoikeudenTyypit)
     }
   }

   "Palauttaa valitun opiskeluoikeudenTyypin" in {
     val opiskeluoikeudenTyyppi = Set("ammatillinenkoulutus")
     postHetut(List(KoskiSpecificMockOppijat.amis.hetu.get), opiskeluoikeudenTyyppi.toList) {
       verifyResponseStatusOk()
       val resp = JsonSerializer.parse[Seq[LuovutuspalveluResponseV1]](body)
       resp.flatMap(_.opiskeluoikeudet.map(_.tyyppi.koodiarvo)).toSet should equal (opiskeluoikeudenTyyppi)
     }
   }

   "Palauttaa 400 jos liian monta hetua" in {
     val hetut = List.range(0, 1001).map(_.toString)
     postHetut(hetut, List("ammatillinenkoulutus")) {
       verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Liian monta hetua, enintään 1000 sallittu"))
     }
   }

   "Palauttaa 400 jos rajapinnan versionumero ei ole 1" in {
     val hetut = List(KoskiSpecificMockOppijat.eerola.hetu.get)
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
     val hetut = List(KoskiSpecificMockOppijat.amis.hetu.get, KoskiSpecificMockOppijat.eerola.hetu.get)
     val ooTyypit = List("ammatillinenkoulutus", "epävalidityyppi")
     postHetut(hetut, ooTyypit) {
       verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Tuntematon opiskeluoikeudentyyppi"))
     }
   }

   "Palauttaa 400 jos tutkintotyyppiä ei voi hakea massahaulla" in {
     val hetut = List(KoskiSpecificMockOppijat.amis.hetu.get)
     val ooTyypit = List("ammatillinenkoulutus", "ylioppilastutkinto")
     postHetut(hetut, ooTyypit) {
       verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Korkeakoulutus tai ylioppilastutkinto ei sallittu"))
     }
   }

   "Vaatii vähintään yhden opiskeluoikeudenTyypin" in {
     val hetut = List(KoskiSpecificMockOppijat.amis.hetu.get)
     val ooTyypit = List()
     postHetut(hetut, ooTyypit) {
       verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam("Opiskeluoikeuden tyypit puuttuvat"))
     }
   }

   "Tuottaa oikean audit log viestin" in {
     AuditLogTester.clearMessages
     val henkilo = KoskiSpecificMockOppijat.amis
     postHetut(List(henkilo.hetu.get), List("ammatillinenkoulutus")) {
       verifyResponseStatusOk()
       AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN", "target" -> Map("oppijaHenkiloOid" -> henkilo.oid.toString)))
     }
   }
 }

  private def postHetu[A](hetu: String, opiskeluoikeudenTyypit: List[String])(f: => A): A = {
    post(
      "api/luovutuspalvelu/hetu",
      JsonSerializer.writeWithRoot(HetuRequestV1(1, hetu, opiskeluoikeudenTyypit)),
      headers = authHeaders(MockUsers.luovutuspalveluKäyttäjä) ++ jsonContent
    )(f)
  }

  private def postOid[A](oid: String, opiskeluoikeudenTyypit: List[String])(f: => A): A = {
    post(
      "api/luovutuspalvelu/oid",
      JsonSerializer.writeWithRoot(OidRequestV1(1, oid, opiskeluoikeudenTyypit)),
      headers = authHeaders(MockUsers.luovutuspalveluKäyttäjä) ++ jsonContent
    )(f)
  }

  private def postHetut[A](hetut: List[String], opiskeluoikeudenTyypit: List[String], v: Int = 1)(f: => A): A = {
    post(
      "api/luovutuspalvelu/hetut",
      JsonSerializer.writeWithRoot(BulkHetuRequestV1(v, hetut, opiskeluoikeudenTyypit)),
      headers = authHeaders(MockUsers.luovutuspalveluKäyttäjä) ++ jsonContent
    )(f)
  }
}
