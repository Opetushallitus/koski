package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.{LocalJettyHttpSpecification, OpiskeluoikeusTestMethods}
import fi.oph.koski.henkilo.{MockOppijat, OppijaHenkilö}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{MockUser, MockUsers}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema.Finnish
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

import scala.xml.XML

class PalveluvaylaSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethods with Matchers with BeforeAndAfterAll {
  "Suomi.fi rekisteritiedot" - {
    "käyttää konffattua suomi.fi oidia" in {
      KoskiApplicationForTests.config.getString("suomi-fi-user-oid") shouldEqual MockUsers.suomiFiKäyttäjä.oid
    }

    "vaatii suomi.fi käyttäjän" in {
      MockUsers.users
        .diff(List(MockUsers.luovutuspalveluKäyttäjäArkaluontoinen, MockUsers.luovutuspalveluKäyttäjä, MockUsers.suomiFiKäyttäjä))
        .foreach { user =>
          postSuomiFiRekisteritiedot(user, MockOppijat.ylioppilas.hetu.get) {
            verifySOAPError("forbidden.vainViranomainen", "Sallittu vain viranomaisille")
          }
        }
      postSuomiFiRekisteritiedot(MockUsers.luovutuspalveluKäyttäjä, MockOppijat.ylioppilas.hetu.get) {
        verifySOAPError("forbidden.kiellettyKäyttöoikeus", "Ei sallittu näillä käyttöoikeuksilla")
      }
      postSuomiFiRekisteritiedot(MockUsers.suomiFiKäyttäjä, MockOppijat.ylioppilas.hetu.get) {
        verifyResponseStatusOk()
      }
    }

    "palauttaa oppilaan tiedot hetun perusteella" in {
      postSuomiFiRekisteritiedot(MockUsers.suomiFiKäyttäjä, MockOppijat.ylioppilas.hetu.get) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "KANSALAINEN_SUOMIFI_KATSOMINEN"))
        jsonResponse shouldEqual SuomiFiResponse(
            List(SuomiFiOppilaitos(Finnish("Helsingin medialukio",None,None),
              List(SuomiFiOpiskeluoikeus(None,None,None,Finnish("Ylioppilastutkinto",Some("Studentexamen"),Some("Matriculation Examination")))))))
      }
    }

    "palauttaa tyhjän lista oppilaitoksia jos oppilasta ei löydy hetun perusteella" in {
      List("261125-1531", "210130-5616", "080278-8433", "061109-011D", "070696-522Y", "010844-509V").foreach { hetu =>
        postSuomiFiRekisteritiedot(MockUsers.suomiFiKäyttäjä, hetu) {
          verifyResponseStatusOk()
          jsonResponse shouldEqual SuomiFiResponse(List.empty)
        }
      }
    }

    "palauttaa tyhjän listan oppilaitoksia jos virta pavelu ei vastaa" in {
      postSuomiFiRekisteritiedot(MockUsers.suomiFiKäyttäjä, MockOppijat.virtaEiVastaa.hetu.get) {
        verifySOAPError("unavailable.virta", "Korkeakoulutuksen opiskeluoikeuksia ei juuri nyt saada haettua. Yritä myöhemmin uudelleen.")
      }
    }

    "Suorituksen nimi" - {
      "Kun on pelkkiä perusopetuksen vuosiluokkia käytetään sanaa 'Perusopetus'" in {
        // kesken olevat perusopetuksen päättötodistukset karsitaan pois -> opiskeluoikeudessa pelkkiä perusopetuksen vuosiluokkia
        suorituksenNimiRekisteritiedoissa(MockOppijat.ysiluokkalainen) shouldEqual "Perusopetus"
      }

      "Kun on pelkkiä perusopetuksen oppiaineen oppimääriä opiskeluoikeudessa käytetään '<lkm> oppiainetta'" in {
        suorituksenNimiRekisteritiedoissa(MockOppijat.montaOppiaineenOppimäärääOpiskeluoikeudessa) shouldEqual "2 oppiainetta"
      }

      "Kun on pelkkiä korkeakoulun opintojaksoja opiskeluoikeudessa käytetään '<lkm> opintojaksoa'" in {
        suorituksenNimiRekisteritiedoissa(MockOppijat.korkeakoululainen) shouldEqual "69 opintojaksoa"
      }

      "Aikuisten perusopetuksessa käytetään suorituksen tyypin nimeä" in {
        suorituksenNimiRekisteritiedoissa(MockOppijat.aikuisOpiskelija) shouldEqual "Aikuisten perusopetuksen oppimäärä"
      }

      "Ammatillisen tutkinnon nimenä käytetään perusteen nimeä" in {
        suorituksenNimiRekisteritiedoissa(MockOppijat.ammattilainen) shouldEqual "Luonto- ja ympäristöalan perustutkinto"
      }

      "Osittaisen ammatillisen tutkinnon nimen loppuun tulee sana 'osittainen'" in {
        suorituksenNimiRekisteritiedoissa(MockOppijat.osittainenammattitutkinto) shouldEqual "Luonto- ja ympäristöalan perustutkinto, osittainen"
      }

      "Perustapauksessa käytetään suorituksen tunnisteen nimeä" in {
        suorituksenNimiRekisteritiedoissa(MockOppijat.lukiolainen) shouldEqual "Lukion oppimäärä"
        suorituksenNimiRekisteritiedoissa(MockOppijat.dippainssi) shouldEqual "Dipl.ins., konetekniikka"
        suorituksenNimiRekisteritiedoissa(MockOppijat.ylioppilas) shouldEqual "Ylioppilastutkinto"
        suorituksenNimiRekisteritiedoissa(MockOppijat.koululainen) shouldEqual "Perusopetus"
      }
    }
  }

  def suorituksenNimiRekisteritiedoissa(oppija: OppijaHenkilö): String =
    haeSuomiFiRekisteritiedot(oppija).oppilaitokset.head.opiskeluoikeudet.head.nimi.get("fi")

  def haeSuomiFiRekisteritiedot(oppija: OppijaHenkilö): SuomiFiResponse = postSuomiFiRekisteritiedot(MockUsers.suomiFiKäyttäjä, oppija.hetu.get) {
    verifyResponseStatusOk()
    JsonSerializer.parse[SuomiFiResponse]((soapResponse() \ "Body" \ "suomiFiRekisteritiedotResponse").text)
  }

  def postSuomiFiRekisteritiedot[A](user: MockUser, hetu: String)(fn: => A): A = {
    post("api/palveluvayla/suomi-fi-rekisteritiedot", body = soapRequest(hetu), headers = authHeaders(user))(fn)
  }

  def jsonResponse = JsonSerializer.parse[SuomiFiResponse]((soapResponse() \ "Body" \ "suomiFiRekisteritiedotResponse").text)

  def verifySOAPError(faultstring: String, message: String): Unit = {
    response.status shouldBe 500
    val xml = soapResponse() \\ "Fault"
    (xml \ "faultcode").text shouldBe("SOAP-ENV:Server")
    (xml \ "faultstring").text shouldBe(faultstring)
    (xml \ "detail" \ "message").text shouldBe(message)
  }

  def soapResponse() = XML.loadString(response.body)

  def soapRequest(hetu: String) =
    <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xrd="http://x-road.eu/xsd/xroad.xsd" xmlns:id="http://x-road.eu/xsd/identifiers">
      <SOAP-ENV:Header>
        <xrd:client id:objectType="SUBSYSTEM">
          <id:xRoadInstance>FI</id:xRoadInstance>
          <id:memberClass>GOV</id:memberClass>
          <id:memberCode>0245437-2</id:memberCode>
          <id:subsystemCode>ServiceViewClient</id:subsystemCode>
        </xrd:client>
        <xrd:service id:objectType="SERVICE">
          <id:xRoadInstance>FI</id:xRoadInstance>
          <id:memberClass>GOV</id:memberClass>
          <id:memberCode>000000-1</id:memberCode>
          <id:subsystemCode>TestSystem</id:subsystemCode>
          <id:serviceCode>testService</id:serviceCode>
        </xrd:service>
        <xrd:protocolVersion>4.0</xrd:protocolVersion>
        <xrd:id></xrd:id>
        <xrd:userId></xrd:userId>
      </SOAP-ENV:Header>
      <SOAP-ENV:Body>
        <ns1:suomiFiRekisteritiedot xmlns:ns1="http://docs.koski-xroad.fi/producer">
          <ns1:hetu>{hetu}</ns1:hetu>
        </ns1:suomiFiRekisteritiedot>
      </SOAP-ENV:Body>
    </SOAP-ENV:Envelope>.toString()
}
