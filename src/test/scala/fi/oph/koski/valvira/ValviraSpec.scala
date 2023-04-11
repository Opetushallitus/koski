package fi.oph.koski.valvira

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.documentation.AmmatillinenExampleData
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}
import fi.oph.koski.log.{AccessLogTester, AuditLogTester}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class ValviraSpec extends AnyFreeSpec with KoskiHttpSpec with OpiskeluoikeusTestMethodsAmmatillinen with Matchers with BeforeAndAfterAll {

  "ValviraSpec" - {
    "Yhdistää datat taulun sarakkeista jsoniin" - {
      "päättymispäivä" in {
       getHetu(KoskiSpecificMockOppijat.valviraaKiinnostavaTutkinto.hetu.get) {
         parseValviraOppija.opiskeluoikeudet.head.päättymispäivä should equal(Some(LocalDate.of(2016, 8, 1)))
       }
      }
      "serialisoituu kun päättymispäivää ei ole" in {
        getHetu(KoskiSpecificMockOppijat.valviraaKiinnostavaTutkintoKesken.hetu.get) {
          parseValviraOppija.opiskeluoikeudet.head.päättymispäivä should equal(None)
        }
      }
    }
    "Kutsuminen vaatii VALVIRA-käyttöoikeuden" in {
      getHetu(KoskiSpecificMockOppijat.amis.hetu.get, user = MockUsers.luovutuspalveluKäyttäjä) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden())
      }
    }
    "Palauttaa 404 jos ei opintoja" in {
      getHetu(KoskiSpecificMockOppijat.eiKoskessa.hetu.get) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound())
      }
    }
    "Palauttaa 404 jos opiskeluoikeus ei sisällä ammatillisen tutkinnon suorituksia" in {
      getHetu(KoskiSpecificMockOppijat.osittainenammattitutkinto.hetu.get) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound())
      }
    }
    "Palauttaa 404 jos opiskeluoikeus sisältää vain ammatillisen tutkinnon suorituksia muilta kuin Valviran tutkintokoodeilta" in {
      getHetu(KoskiSpecificMockOppijat.erikoisammattitutkinto.hetu.get) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound())
      }
    }
    "Palauttaa opiskeluoikeudesta vain ammatilliset Valviraa kiinnostavat tutkinnot" in {
      getHetu(KoskiSpecificMockOppijat.valviraaKiinnostavaTutkinto.hetu.get) {
        parseValviraOppija.opiskeluoikeudet.flatMap(_.suoritukset.map(_.koulutusmoduuli.tunniste.nimi.get.get("fi"))) should equal(
          List("Sosiaali- ja terveysalan perustutkinto")
        )
      }
    }
    "ei palauta ammatillisen koulutusvientikoulutuksen opiskeluoikeutta Valviralle" in {
      getHetu(KoskiSpecificMockOppijat.amisKoulutusvienti.hetu.get) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound())
      }
    }
    "Palauttaa linkitettyjen oidien opinnot" in {
      putOpiskeluoikeus(AmmatillinenExampleData.sosiaaliJaTerveysalaOpiskeluoikeus(), KoskiSpecificMockOppijat.master) {
        putOpiskeluoikeus(AmmatillinenExampleData.sosiaaliJaTerveysalaOpiskeluoikeusKesken(), KoskiSpecificMockOppijat.slave.henkilö) {
         getHetu(KoskiSpecificMockOppijat.master.hetu.get) {
           parseValviraOppija.opiskeluoikeudet.flatMap(_.suoritukset.map(_.koulutusmoduuli.tunniste.nimi.get.get("fi"))) should equal(
             List("Sosiaali- ja terveysalan perustutkinto", "Sosiaali- ja terveysalan perustutkinto")
           )
         }
       }
      }
    }
    "Tuottaa oikean auditlogin" in {
      AuditLogTester.clearMessages
      getHetu(KoskiSpecificMockOppijat.valviraaKiinnostavaTutkinto.hetu.get) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN", "target" -> Map("oppijaHenkiloOid" -> KoskiSpecificMockOppijat.valviraaKiinnostavaTutkinto.oid)))
      }
    }
    "Hetu ei päädy lokiin" in {
      AccessLogTester.clearMessages
      val maskedHetu = "******-****"
      getHetu(KoskiSpecificMockOppijat.valviraaKiinnostavaTutkinto.hetu.get) {
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
