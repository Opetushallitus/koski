package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.AuditLogTester
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class SuomiFiServletSpec extends AnyFreeSpec with KoskiHttpSpec with Matchers with BeforeAndAfterAll {

  private val suomiFiCertificateHeaders: Map[String, String] = Map(
    "x-amzn-mtls-clientcert-subject" -> "CN=dvv-suomifi",
    "x-amzn-mtls-clientcert-serial-number" -> "123",
    "x-amzn-mtls-clientcert-issuer" -> "CN=mock-issuer",
    "X-Forwarded-For" -> "0.0.0.0"
  )

  private val valviraCertificateHeaders: Map[String, String] = Map(
    "x-amzn-mtls-clientcert-subject" -> "CN=valvira",
    "x-amzn-mtls-clientcert-serial-number" -> "123",
    "x-amzn-mtls-clientcert-issuer" -> "CN=mock-issuer",
    "X-Forwarded-For" -> "0.0.0.0"
  )

  "SuomiFiServlet" - {
    "vaatii mTLS-varmenteen" in {
      postRekisteritiedot(KoskiSpecificMockOppijat.ylioppilas.hetu.get, headers = jsonHeaders) {
        verifyResponseStatus(401)
      }
    }

    "hylkää pyynnöt väärällä varmenteella" in {
      postRekisteritiedot(KoskiSpecificMockOppijat.ylioppilas.hetu.get, headers = valviraCertificateHeaders ++ jsonHeaders) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden())
      }
    }

    "palauttaa opiskeluoikeudet hetun perusteella" in {
      postRekisteritiedot(KoskiSpecificMockOppijat.ammattilainen.hetu.get) {
        verifyResponseStatusOk()
        AuditLogTester.verifyLastAuditLogMessageForOperation(Map("operation" -> "KANSALAINEN_SUOMIFI_KATSOMINEN"))
        val response = parseSuomiFiResponse
        response.oppilaitokset should not be empty
        val oppilaitos = response.oppilaitokset.head
        oppilaitos.nimi.get("fi") should equal("Stadin ammatti- ja aikuisopisto")
        val oo = oppilaitos.opiskeluoikeudet.head
        oo.nimi.get("fi") should equal("Luonto- ja ympäristöalan perustutkinto")
        oo.tila.map(_.get("fi")) should equal(Some("Valmistunut"))
        oo.alku should equal(Some(java.time.LocalDate.of(2012, 9, 1)))
        oo.loppu should equal(Some(java.time.LocalDate.of(2016, 5, 31)))
      }
    }

    "palauttaa tyhjän listan jos opiskeluoikeuksia ei löydy" in {
      postRekisteritiedot("261125-1531") {
        verifyResponseStatusOk()
        val response = parseSuomiFiResponse
        response.oppilaitokset shouldBe empty
      }
    }

    "palauttaa virheen jos oppija puuttuu" in {
      postRekisteritiedot("070118A9033") {
        verifyResponseStatus(400)
      }
    }

    "palauttaa virheen jos hetu puuttuu" in {
      post(
        "api/luovutuspalvelu/suomifi/rekisteritiedot",
        body = """{}""",
        headers = suomiFiCertificateHeaders ++ jsonHeaders
      ) {
        verifyResponseStatus(400)
      }
    }

    "palauttaa virheen jos JSON on virheellistä" in {
      post(
        "api/luovutuspalvelu/suomifi/rekisteritiedot",
        body = "not json",
        headers = suomiFiCertificateHeaders ++ jsonHeaders
      ) {
        verifyResponseStatus(400)
      }
    }

    "Suorituksen nimi" - {
      "Kun opiskeluoikeudessa on pelkkiä perusopetuksen vuosiluokkia käytetään sanaa 'Perusopetus'" in {
        ensimmäisenSuorituksenNimiRekisteritiedoissa(KoskiSpecificMockOppijat.ysiluokkalainen) shouldEqual "Perusopetus"
      }

      "Kun opiskeluoikeudessa on perusopetuksen oppiaineen oppimääriä käytetään nimenä suorituksen tyyppiä" in {
        ensimmäisenSuorituksenNimiRekisteritiedoissa(KoskiSpecificMockOppijat.montaOppiaineenOppimäärääOpiskeluoikeudessa) shouldEqual "Perusopetuksen oppiaineen oppimäärä"
      }

      "Kun opiskeluoikeudessa on lukion oppiaineen oppimääriä käytetään nimenä suorituksen tyyppiä" in {
        ensimmäisenSuorituksenNimiRekisteritiedoissa(KoskiSpecificMockOppijat.lukionAineopiskelija) shouldEqual "Lukion oppiaineen oppimäärä"
      }

      "Kun opiskeluoikeudessa on opintojaksojen seassa korkeakoulututkinto käytetään tutkinnon nimeä" in {
        ensimmäisenSuorituksenNimiRekisteritiedoissa(KoskiSpecificMockOppijat.opintojaksotSekaisin) shouldEqual "Fysioterapeutti (AMK)"
      }

      "Kun opiskeluoikeudessa on pelkkiä korkeakoulun opintojaksoja käytetään '<lkm> opintojaksoa'" in {
        ensimmäisenSuorituksenNimiRekisteritiedoissa(KoskiSpecificMockOppijat.korkeakoululainen) shouldEqual "69 opintojaksoa"
      }

      "Aikuisten perusopetuksessa käytetään suorituksen tyypin nimeä" in {
        ensimmäisenSuorituksenNimiRekisteritiedoissa(KoskiSpecificMockOppijat.aikuisOpiskelija) shouldEqual "Aikuisten perusopetuksen oppimäärä"
      }

      "Ammatillisen tutkinnon nimenä käytetään perusteen nimeä" in {
        ensimmäisenSuorituksenNimiRekisteritiedoissa(KoskiSpecificMockOppijat.ammattilainen) shouldEqual "Luonto- ja ympäristöalan perustutkinto"
      }

      "Osittaisen ammatillisen tutkinnon nimen loppuun tulee sana 'osittainen'" in {
        ensimmäisenSuorituksenNimiRekisteritiedoissa(KoskiSpecificMockOppijat.osittainenammattitutkinto) shouldEqual "Luonto- ja ympäristöalan perustutkinto, osittainen"
      }

      "Perustapauksessa käytetään suorituksen tunnisteen nimeä" in {
        ensimmäisenSuorituksenNimiRekisteritiedoissa(KoskiSpecificMockOppijat.lukiolainen) shouldEqual "Lukion oppimäärä"
        ensimmäisenSuorituksenNimiRekisteritiedoissa(KoskiSpecificMockOppijat.dippainssi) shouldEqual "Dipl.ins., konetekniikka"
        ensimmäisenSuorituksenNimiRekisteritiedoissa(KoskiSpecificMockOppijat.amkValmistunut) shouldEqual "Fysioterapeutti (AMK)"
        ensimmäisenSuorituksenNimiRekisteritiedoissa(KoskiSpecificMockOppijat.ylioppilas) shouldEqual "Ylioppilastutkinto"
        ensimmäisenSuorituksenNimiRekisteritiedoissa(KoskiSpecificMockOppijat.koululainen) shouldEqual "Perusopetukseen valmistava opetus"
      }
    }
  }

  private val jsonHeaders: Map[String, String] = Map(
    "Content-Type" -> "application/json;charset=utf-8"
  )

  private def postRekisteritiedot[A](hetu: String, headers: Map[String, String] = suomiFiCertificateHeaders ++ jsonHeaders)(fn: => A): A = {
    post(
      "api/luovutuspalvelu/suomifi/rekisteritiedot",
      body = s"""{"hetu": "$hetu"}""",
      headers = headers
    )(fn)
  }

  private def parseSuomiFiResponse: SuomiFiResponse = {
    JsonSerializer.parse[SuomiFiResponse](body)
  }

  private def ensimmäisenSuorituksenNimiRekisteritiedoissa(oppija: LaajatOppijaHenkilöTiedot): String =
    postRekisteritiedot(oppija.hetu.get) {
      verifyResponseStatusOk()
      parseSuomiFiResponse.oppilaitokset.head.opiskeluoikeudet.head.nimi.get("fi")
    }
}
