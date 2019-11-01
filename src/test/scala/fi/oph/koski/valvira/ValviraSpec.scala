package fi.oph.koski.valvira

import fi.oph.koski.api.{LocalJettyHttpSpecification, OpiskeluoikeusTestMethodsAmmatillinen}
import fi.oph.koski.documentation.AmmatillinenExampleData
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}
import fi.oph.koski.log.AuditLogTester
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class ValviraSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen with Matchers with BeforeAndAfterAll {

  "ValviraSpec" - {
    "Kutsuminen vaatii VALVIRA-käyttöoikeuden" in {
      getHetu(MockOppijat.amis.hetu.get, user = MockUsers.luovutuspalveluKäyttäjä) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden())
      }
    }
    "Palauttaa 404 jos ei opintoja" in {
      getHetu(MockOppijat.eiKoskessa.hetu.get) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound())
      }
    }
    "Palauttaa 404 jos opiskeluoikeus ei sisällä ammatillisen tutkinnon suorituksia" in {
      getHetu(MockOppijat.osittainenammattitutkinto.hetu.get) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound())
      }
    }
    "Palauttaa opiskeluoikeudesta vain ammatilliset tutkinnot" in {
      getHetu(MockOppijat.erikoisammattitutkinto.hetu.get) {
        parseValviraOppija.opiskeluoikeudet.flatMap(_.suoritukset.map(_.koulutusmoduuli.tunniste.nimi.get.get("fi"))) should equal(
          List("Autoalan työnjohdon erikoisammattitutkinto")
        )
      }
    }
    "Palauttaa linkitettyjen oidien opinnot" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus, MockOppijat.master) {
        putOpiskeluoikeus(AmmatillinenExampleData.puuteollisuusOpiskeluoikeusKesken(), MockOppijat.slave.henkilö) {
         getHetu(MockOppijat.master.hetu.get) {
           parseValviraOppija.opiskeluoikeudet.flatMap(_.suoritukset.map(_.koulutusmoduuli.tunniste.nimi.get.get("fi"))) should equal(
             List("Autoalan perustutkinto", "Puuteollisuuden perustutkinto")
           )
         }
       }
      }
    }
    "Tuottaa oikean auditlogin" in {
      AuditLogTester.clearMessages
      getHetu(MockOppijat.amis.hetu.get) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN", "target" -> Map("oppijaHenkiloOid" -> MockOppijat.amis.oid)))
      }
    }
  }

  def getHetu[A](hetu: String, user: UserWithPassword = MockUsers.valviraKäyttäjä)(f: => A)= {
    authGet(s"api/luovutuspalvelu/valvira/$hetu", user)(f)
  }

  def parseValviraOppija= {
    verifyResponseStatusOk()
    JsonSerializer.parse[ValviraOppija](body)
  }
}
