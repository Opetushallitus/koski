package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.{LocalJettyHttpSpecification, OpiskeluoikeusTestMethods}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.koskiuser.{MockUser, MockUsers}
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
          postSuomiFiRekisteritiedot(user) {
            verifySOAPError("vainViranomainen", "Sallittu vain viranomaisille")
          }
        }
      postSuomiFiRekisteritiedot(MockUsers.luovutuspalveluKäyttäjä) {
        verifySOAPError("kiellettyKäyttöoikeus", "Ei sallittu näillä käyttöoikeuksilla")
      }
      postSuomiFiRekisteritiedot(MockUsers.suomiFiKäyttäjä) {
        response.status shouldBe(200)
      }
    }
  }

  def postSuomiFiRekisteritiedot[A](user: MockUser)(fn: => A): A = {
    post("api/palveluvayla/suomi-fi-rekisteritiedot", body = soapMsg(), headers = authHeaders(user))(fn)
  }

  def verifySOAPError(faultstring: String, message: String): Unit = {
    response.status shouldBe 500
    val xml = XML.loadString(response.body) \\ "Fault"
    (xml \ "faultcode").text shouldBe("SOAP-ENV:Server")
    (xml \ "faultstring").text shouldBe(s"forbidden.${faultstring}")
    (xml \ "detail" \ "message").text shouldBe(message)
  }

  def soapRequest() =
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
          <ns1:hetu>{MockOppijat.ylioppilas.hetu.get}</ns1:hetu>
        </ns1:suomiFiRekisteritiedot>
      </SOAP-ENV:Body>
    </SOAP-ENV:Envelope>.toString()
}
