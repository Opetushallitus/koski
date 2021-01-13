package fi.oph.koski.valvira

import java.time.LocalDate

import fi.oph.koski.api.{LocalJettyHttpSpecification, OpiskeluoikeusTestMethodsAmmatillinen}
import fi.oph.koski.documentation.AmmatillinenExampleData
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.common.koskiuser.{MockUsers, UserWithPassword}
import fi.oph.common.log.{AccessLogTester, AuditLogTester}
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class ValviraSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen with Matchers with BeforeAndAfterAll {

  "ValviraSpec" - {
    "Yhdistää datat taulun sarakkeista jsoniin" - {
      "päättymispäivä" in {
       getHetu(MockOppijat.valviraaKiinnostavaTutkinto.hetu.get) {
          parseValviraOppija.opiskeluoikeudet.head.päättymispäivä should equal(Some(LocalDate.of(2016, 5, 31)))
       }
      }
      "serialisoituu kun päättymispäivää ei ole" in {
        getHetu(MockOppijat.valviraaKiinnostavaTutkintoKesken.hetu.get) {
          parseValviraOppija.opiskeluoikeudet.head.päättymispäivä should equal(None)
        }
      }
    }
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
    "Palauttaa 404 jos opiskeluoikeus sisältää vain ammatillisen tutkinnon suorituksia muilta kuin Valviran tutkintokoodeilta" in {
      getHetu(MockOppijat.erikoisammattitutkinto.hetu.get) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound())
      }
    }
    "Palauttaa opiskeluoikeudesta vain ammatilliset Valviraa kiinnostavat tutkinnot" in {
      getHetu(MockOppijat.valviraaKiinnostavaTutkinto.hetu.get) {
        parseValviraOppija.opiskeluoikeudet.flatMap(_.suoritukset.map(_.koulutusmoduuli.tunniste.nimi.get.get("fi"))) should equal(
          List("Sosiaali- ja terveysalan perustutkinto")
        )
      }
    }
    "Palauttaa linkitettyjen oidien opinnot" in {
      putOpiskeluoikeus(AmmatillinenExampleData.sosiaaliJaTerveysalaOpiskeluoikeus(), MockOppijat.master) {
        putOpiskeluoikeus(AmmatillinenExampleData.sosiaaliJaTerveysalaOpiskeluoikeusKesken(), MockOppijat.slave.henkilö) {
         getHetu(MockOppijat.master.hetu.get) {
           parseValviraOppija.opiskeluoikeudet.flatMap(_.suoritukset.map(_.koulutusmoduuli.tunniste.nimi.get.get("fi"))) should equal(
             List("Sosiaali- ja terveysalan perustutkinto", "Sosiaali- ja terveysalan perustutkinto")
           )
         }
       }
      }
    }
    "Tuottaa oikean auditlogin" in {
      AuditLogTester.clearMessages
      getHetu(MockOppijat.valviraaKiinnostavaTutkinto.hetu.get) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN", "target" -> Map("oppijaHenkiloOid" -> MockOppijat.valviraaKiinnostavaTutkinto.oid)))
      }
    }
    "Hetu ei päädy lokiin" in {
      AccessLogTester.clearMessages
      val maskedHetu = "******-****"
      getHetu(MockOppijat.valviraaKiinnostavaTutkinto.hetu.get) {
        verifyResponseStatusOk()
        AccessLogTester.getLatestMatchingAccessLog("/koski/api/luovutuspalvelu/valvira/") should include(maskedHetu)
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
