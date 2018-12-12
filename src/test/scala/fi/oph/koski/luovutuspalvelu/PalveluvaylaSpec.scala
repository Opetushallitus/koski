package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.{LocalJettyHttpSpecification, OpiskeluoikeusTestMethods}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{MockUser, MockUsers}
import fi.oph.koski.schema.Finnish
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

import scala.xml.XML

class PalveluvaylaSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethods with Matchers with BeforeAndAfterAll {
  "Suomi.fi rekisteritiedot" - {
    "käyttää konffattua suomi.fi oidia" in {
      KoskiApplicationForTests.config.getString("suomi-fi-user-oid") shouldEqual(MockUsers.suomiFiKäyttäjä.oid)
    }

    "vaatii suomi.fi käyttäjän" in {
      MockUsers.users
        .diff(List(MockUsers.luovutuspalveluKäyttäjä, MockUsers.suomiFiKäyttäjä))
        .foreach { user =>
          postSuomiFiRekisteritiedot(user, MockOppijat.ylioppilas.hetu.get) {
            verifySOAPError("forbidden.vainViranomainen", "Sallittu vain viranomaisille")
          }
        }
      postSuomiFiRekisteritiedot(MockUsers.luovutuspalveluKäyttäjä, MockOppijat.ylioppilas.hetu.get) {
        verifySOAPError("forbidden.kiellettyKäyttöoikeus", "Ei sallittu näillä käyttöoikeuksilla")
      }
      postSuomiFiRekisteritiedot(MockUsers.suomiFiKäyttäjä, MockOppijat.ylioppilas.hetu.get) {
        response.status shouldBe(200)
      }
    }

    "palauttaa oppilaan tiedot hetun perusteella" in {
      postSuomiFiRekisteritiedot(MockUsers.suomiFiKäyttäjä, MockOppijat.ylioppilas.hetu.get) {
        jsonResponse shouldEqual SuomiFiResponse(
            List(SuomiFiOppilaitos(Finnish("Helsingin medialukio",None,None),
              List(SuomiFiOpiskeluoikeus(None,None,None,Finnish("Ylioppilastutkinto",Some("Studentexamen"),Some("Matriculation Examination")))))))
      }
    }

    "palauttaa tyhjän lista oppilaitoksia jos oppilasta ei löydy hetun perusteella" in {
      List("261125-1531", "210130-5616", "080278-8433", "061109-011D", "070696-522Y", "010844-509V").foreach { hetu =>
        postSuomiFiRekisteritiedot(MockUsers.suomiFiKäyttäjä, hetu) {
          response.status shouldBe(200)
          jsonResponse shouldEqual SuomiFiResponse(List.empty)
        }
      }
    }
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
