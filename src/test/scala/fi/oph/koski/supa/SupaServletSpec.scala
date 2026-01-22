package fi.oph.koski.supa

import fi.oph.koski.api.misc.{OpiskeluoikeusTestMethods, OpiskeluoikeusTestMethodsAmmatillinen}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{KoskiMockUser, MockUsers}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema.AmmatillinenOpiskeluoikeus
import fi.oph.koski.KoskiHttpSpec
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, JValue}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class SupaServletSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with OpiskeluoikeusTestMethods
    with OpiskeluoikeusTestMethodsAmmatillinen
    with Matchers
    with BeforeAndAfterEach {

  implicit val formats: DefaultFormats.type = DefaultFormats

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    AuditLogTester.clearMessages()
  }

  "GET /api/supa/:oid/:version" - {
    "Peruskäyttö" - {
      "Palauttaa opiskeluoikeuden tietyn version" in {
        val oppija = KoskiSpecificMockOppijat.amis
        val opiskeluoikeus = lastOpiskeluoikeusByHetu(oppija)
        val oid = opiskeluoikeus.oid.get
        val versio = opiskeluoikeus.versionumero.get

        getSupaVersio(oid, versio, MockUsers.paakayttaja) {
          verifyResponseStatusOk()
          val json = JsonMethods.parse(body)
          (json \ "oppijaOid").extract[String] should equal(oppija.oid)
          (json \ "opiskeluoikeus" \ "oid").extract[String] should equal(oid)
          (json \ "opiskeluoikeus" \ "versionumero").extract[Int] should equal(versio)
          (json \ "kaikkiOidit").extract[List[String]] should contain(oppija.oid)
          (json \ "aikaleima").extract[String] should not be empty
        }
      }

      "Palauttaa myös aiemman version opiskeluoikeudesta" in {
        val oppija = KoskiSpecificMockOppijat.amis
        val opiskeluoikeus = lastOpiskeluoikeusByHetu(oppija).asInstanceOf[AmmatillinenOpiskeluoikeus]
        val oid = opiskeluoikeus.oid.get

        // Luo uusi versio päivittämällä opiskeluoikeutta
        val päivitettyOo = opiskeluoikeus.copy(arvioituPäättymispäivä = Some(java.time.LocalDate.now().plusYears(1)))
        putOpiskeluoikeus(päivitettyOo, oppija) {
          verifyResponseStatusOk()
        }

        // Hae versio 1
        getSupaVersio(oid, 1, MockUsers.paakayttaja) {
          verifyResponseStatusOk()
          val json = JsonMethods.parse(body)
          (json \ "opiskeluoikeus" \ "versionumero").extract[Int] should equal(1)
        }

        // Hae versio 2
        getSupaVersio(oid, 2, MockUsers.paakayttaja) {
          verifyResponseStatusOk()
          val json = JsonMethods.parse(body)
          (json \ "opiskeluoikeus" \ "versionumero").extract[Int] should equal(2)
        }
      }

      "Palauttaa oppijan kaikki oidit (master ja slave)" in {
        val slaveOppija = KoskiSpecificMockOppijat.slave
        val masterOppija = KoskiSpecificMockOppijat.master

        val opiskeluoikeudet = getOpiskeluoikeudet(masterOppija.oid, MockUsers.paakayttaja)
        val opiskeluoikeus = opiskeluoikeudet.headOption.getOrElse(fail("Ei löytynyt opiskeluoikeutta"))
        val oid = opiskeluoikeus.oid.get
        val versio = opiskeluoikeus.versionumero.get

        getSupaVersio(oid, versio, MockUsers.paakayttaja) {
          verifyResponseStatusOk()
          val json = JsonMethods.parse(body)
          val kaikkiOidit = (json \ "kaikkiOidit").extract[List[String]]
          kaikkiOidit should contain(masterOppija.oid)
          kaikkiOidit should contain(slaveOppija.henkilö.oid)
        }
      }
    }

    "Käyttöoikeudet" - {
      "Pääkäyttäjä voi hakea opiskeluoikeuden version" in {
        val opiskeluoikeus = lastOpiskeluoikeusByHetu(KoskiSpecificMockOppijat.amis)
        getSupaVersio(opiskeluoikeus.oid.get, 1, MockUsers.paakayttaja) {
          verifyResponseStatusOk()
        }
      }

      "OPH-katselija voi hakea opiskeluoikeuden version" in {
        val opiskeluoikeus = lastOpiskeluoikeusByHetu(KoskiSpecificMockOppijat.amis)
        getSupaVersio(opiskeluoikeus.oid.get, 1, MockUsers.ophkatselija) {
          verifyResponseStatusOk()
        }
      }

      "Tavallinen virkailija ei voi hakea opiskeluoikeuden versiota" in {
        val opiskeluoikeus = lastOpiskeluoikeusByHetu(KoskiSpecificMockOppijat.amis)
        getSupaVersio(opiskeluoikeus.oid.get, 1, MockUsers.kalle) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "Organisaation palvelukäyttäjä ei voi hakea oman organisaationsa opiskeluoikeutta" in {
        val oppija = KoskiSpecificMockOppijat.amis
        val opiskeluoikeus = lastOpiskeluoikeusByHetu(oppija)
        getSupaVersio(opiskeluoikeus.oid.get, 1, MockUsers.stadinAmmattiopistoPalvelukäyttäjä) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "Organisaation palvelukäyttäjä ei voi hakea toisen organisaation opiskeluoikeutta" in {
        val oppija = KoskiSpecificMockOppijat.amis
        val opiskeluoikeus = lastOpiskeluoikeusByHetu(oppija)
        getSupaVersio(opiskeluoikeus.oid.get, 1, MockUsers.omniaPalvelukäyttäjä) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      "Kirjautumaton käyttäjä ei pääse rajapintaan" in {
        val opiskeluoikeus = lastOpiskeluoikeusByHetu(KoskiSpecificMockOppijat.amis)
        get(s"api/supa/${opiskeluoikeus.oid.get}/1") {
          verifyResponseStatus(401, KoskiErrorCategory.unauthorized.notAuthenticated())
        }
      }
    }

    "Virhetilanteet" - {
      "Virheellinen opiskeluoikeuden OID palauttaa 400" in {
        getSupaVersio("virheellinen-oid", 1, MockUsers.paakayttaja) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.queryParam.virheellinenOpiskeluoikeusOid("Virheellinen oid: virheellinen-oid. Esimerkki oikeasta muodosta: 1.2.246.562.15.00000000001."))
        }
      }

      "Olematon opiskeluoikeuden OID palauttaa 404" in {
        getSupaVersio("1.2.246.562.15.99999999999", 1, MockUsers.paakayttaja) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta 1.2.246.562.15.99999999999 ei löydy tai käyttäjällä ei ole oikeutta sen katseluun"))
        }
      }

      "Olematon versio palauttaa 404" in {
        val opiskeluoikeus = lastOpiskeluoikeusByHetu(KoskiSpecificMockOppijat.amis)
        getSupaVersio(opiskeluoikeus.oid.get, 999, MockUsers.paakayttaja) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound.versiotaEiLöydy(s"Versiota 999 ei löydy opiskeluoikeuden ${opiskeluoikeus.oid.get} historiasta."))
        }
      }

      "Virheellinen versio (ei numero) palauttaa 400" in {
        val opiskeluoikeus = lastOpiskeluoikeusByHetu(KoskiSpecificMockOppijat.amis)
        get(s"api/supa/${opiskeluoikeus.oid.get}/abc", headers = authHeaders(MockUsers.paakayttaja)) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.format.number("Invalid version : abc"))
        }
      }

      "Negatiivinen versio palauttaa 400" in {
        val opiskeluoikeus = lastOpiskeluoikeusByHetu(KoskiSpecificMockOppijat.amis)
        getSupaVersio(opiskeluoikeus.oid.get, -1, MockUsers.paakayttaja) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.format.number("Invalid version : -1"))
        }
      }

      "SUPAn tukematon opiskeluoikeustyyppi palauttaa 404" in {
        // Esikoulun opiskeluoikeus ei ole SUPAn tukema tyyppi
        val opiskeluoikeus = lastOpiskeluoikeusByHetu(KoskiSpecificMockOppijat.esikoululainen2025)
        getSupaVersio(opiskeluoikeus.oid.get, 1, MockUsers.paakayttaja) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
        }
      }
    }

    "Audit-lokitus" - {
      "Onnistunut haku kirjoittaa audit-lokiin" in {
        val oppija = KoskiSpecificMockOppijat.amis
        val opiskeluoikeus = lastOpiskeluoikeusByHetu(oppija)
        val oid = opiskeluoikeus.oid.get
        val versio = opiskeluoikeus.versionumero.get

        AuditLogTester.clearMessages()

        getSupaVersio(oid, versio, MockUsers.paakayttaja) {
          verifyResponseStatusOk()
        }

        AuditLogTester.verifyLastAuditLogMessage(Map(
          "operation" -> "SUORITUSPALVELU_OPISKELUOIKEUS_HAKU",
          "target" -> Map(
            "oppijaHenkiloOid" -> oppija.oid,
            "opiskeluoikeusOid" -> oid,
            "opiskeluoikeusVersio" -> versio.toString
          )
        ))
      }

      "Epäonnistunut haku (ei oikeuksia) ei kirjoita audit-lokiin" in {
        val oppija = KoskiSpecificMockOppijat.amis
        val opiskeluoikeus = lastOpiskeluoikeusByHetu(oppija)

        AuditLogTester.clearMessages()

        getSupaVersio(opiskeluoikeus.oid.get, 1, MockUsers.stadinAmmattiopistoPalvelukäyttäjä) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }

        AuditLogTester.verifyNoAuditLogMessages()
      }

      "Epäonnistunut haku (olematon OID) ei kirjoita audit-lokiin" in {
        AuditLogTester.clearMessages()

        getSupaVersio("1.2.246.562.15.99999999999", 1, MockUsers.paakayttaja) {
          verifyResponseStatus(404)
        }

        AuditLogTester.verifyNoAuditLogMessages()
      }
    }
  }

  private def getSupaVersio[T](oid: String, versio: Int, user: KoskiMockUser)(f: => T): T = {
    get(s"api/supa/$oid/$versio", headers = authHeaders(user))(f)
  }
}
