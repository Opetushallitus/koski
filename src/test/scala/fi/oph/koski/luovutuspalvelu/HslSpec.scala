package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.api.misc.OpiskeluoikeusTestMethods
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat._
import fi.oph.koski.koskiuser.{MockUser, MockUsers}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.json4s.{JNothing, JNull, JObject, JValue}
import org.json4s.jackson.JsonMethods
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.xml.{NodeSeq, Utility, XML}

class HslSpec extends AnyFreeSpec with KoskiHttpSpec with OpiskeluoikeusTestMethods with Matchers with BeforeAndAfterAll {
  val opiskelija = KoskiSpecificMockOppijat.markkanen

  "Hsl" - {
    "vaatii HSL käyttäjän" in {
      KoskiApplicationForTests.mydataRepository.create(opiskelija.oid, "hsl")
      MockUsers.users.diff(List(MockUsers.hslKäyttäjä)).foreach { user =>
        postHsl(user, opiskelija.hetu.get) {
          verifySOAPError("forbidden.vainHSL", "Sallittu vain HSL:lle")
        }
      }
      postHsl(MockUsers.hslKäyttäjä, opiskelija.hetu.get) {
        verifyResponseStatusOk()
      }
    }

    "palauttaa oppilaan tiedot hetun perusteella" in {
      postHsl(MockUsers.hslKäyttäjä, opiskelija.hetu.get) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN"))

        val actualJson = parseOpintoOikeudetJson()
        validateOpintoOikeudetJson(actualJson)
      }
    }

    "henkilötiedoista vain oid ja syntymäaika" in {
      postHsl(MockUsers.hslKäyttäjä, opiskelija.hetu.get) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN"))

        val actualJson = parseOpintoOikeudetJson()
        (actualJson \ "henkilö") should equal(JsonMethods.parse("""{"oid": "1.2.246.562.24.00000000003", "syntymäaika": "1954-01-08"}"""))
      }
    }

    "opiskeluoikeuden kentät" in {
      postHsl(MockUsers.hslKäyttäjä, opiskelija.hetu.get) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN"))

        val actualJson = parseOpintoOikeudetJson()
        val opiskeluoikeudet = (actualJson \ "opiskeluoikeudet").children

        opiskeluoikeudet should have size 1
        validateOpiskeluoikeudenKeys(opiskeluoikeudet.head)
      }
    }

    "ammatillisen opiskeluoikeus sisältää tiedon järjestämismuodoista jos olemassa" in {
      KoskiApplicationForTests.mydataRepository.create(valviraaKiinnostavaTutkinto.oid, "hsl")
      postHsl(MockUsers.hslKäyttäjä, valviraaKiinnostavaTutkinto.hetu.get) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN"))

        val actualJson = parseOpintoOikeudetJson()
        val opiskeluoikeudet = (actualJson \ "opiskeluoikeudet").children

        opiskeluoikeudet should have size 1
        validateJärjestämismuodot(opiskeluoikeudet.head)
      }
    }

    "ammatillisen opiskeluoikeus sisältää tiedon osaamisenhankkimistavoista ja koulutussopimuksista jos olemassa" in {
      KoskiApplicationForTests.mydataRepository.create(reformitutkinto.oid, "hsl")
      postHsl(MockUsers.hslKäyttäjä, reformitutkinto.hetu.get) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN"))

        val actualJson = parseOpintoOikeudetJson()
        val opiskeluoikeudet = (actualJson \ "opiskeluoikeudet").children

        opiskeluoikeudet should have size 1
        validateOsaamisenHankkimistavatJaKoulutussopimukset(opiskeluoikeudet.head)
      }
    }

    "korkeakoulun opiskeluoikeuden lisätiedot" in {
      KoskiApplicationForTests.mydataRepository.create(dippainssi.oid, "hsl")
      postHsl(MockUsers.hslKäyttäjä, dippainssi.hetu.get) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN"))

        val actualJson = parseOpintoOikeudetJson()
        val opiskeluoikeudet = (actualJson \ "opiskeluoikeudet").children

        opiskeluoikeudet should have size 2
        validateLisätiedot(opiskeluoikeudet)
      }
    }
  }

  private def postHsl[A](user: MockUser, hetu: String)(fn: => A): A = {
    post("api/hsl", body = soapRequest(hetu), headers = authHeaders(user) ++ Map(("Content-type" -> "text/xml")))(fn)
  }

  private def verifySOAPError(faultstring: String, message: String): Unit = {
    response.status shouldBe 500
    val xml = soapResponse() \\ "Fault"
    (xml \ "faultcode").text shouldBe "SOAP-ENV:Server"
    (xml \ "faultstring").text shouldBe faultstring
    (xml \ "detail" \ "message").text shouldBe message
  }

  private def soapResponse() = Utility.trim(XML.loadString(response.body))

  private def soapRequest(hetu: String) =
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xro="http://x-road.eu/xsd/xroad.xsd" xmlns:id="http://x-road.eu/xsd/identifiers" xmlns:prod="http://docs.koski-xroad.fi/producer">
      <soapenv:Header>
        <xro:protocolVersion>4.0</xro:protocolVersion>
        <xro:issue>issue #123</xro:issue>
        <xro:id>ID123456</xro:id>
        <xro:userId>123456789</xro:userId>
        <xro:service id:objectType="SERVICE">
          <id:xRoadInstance>FI-TEST</id:xRoadInstance>
          <id:memberClass>GOV</id:memberClass>
          <id:memberCode>2769790-1</id:memberCode>
          <id:subsystemCode>koski</id:subsystemCode>
          <id:serviceCode>opintoOikeudetService</id:serviceCode>
          <id:serviceVersion>v1</id:serviceVersion>
        </xro:service>
        <xro:client id:objectType="SUBSYSTEM">
          <id:xRoadInstance>FI-TEST</id:xRoadInstance>
          <id:memberClass>GOV</id:memberClass>
          <id:memberCode>000000-1</id:memberCode>
          <id:subsystemCode>TestSystem</id:subsystemCode>
          <id:subsystemCode>testService</id:subsystemCode>
        </xro:client>
      </soapenv:Header>
      <soapenv:Body>
        <kns1:opintoOikeudetService>
          <kns1:hetu>
            {hetu}
          </kns1:hetu>
        </kns1:opintoOikeudetService>
      </soapenv:Body>
    </soapenv:Envelope>.toString()

  private def parseOpintoOikeudetJson() = {
    val opintoOikeudetXml = soapResponse() \ "Body" \ "opintoOikeudetServiceResponse" \ "opintoOikeudet"
    val opintoOikeudetJsonString = (opintoOikeudetXml.text)
    JsonMethods.parse(opintoOikeudetJsonString)
  }

  private def validateOpintoOikeudetJson(json: JValue) = {
    json \ "foo" should be(JNothing)
    json \ "henkilö" should not be JNothing
    json \ "opiskeluoikeudet" should not be JNothing
    json \ "suostumuksenPaattymispaiva" should not be JNothing
  }

  private def validateOpiskeluoikeudenKeys(opiskeluoikeus: JValue) = {
    val expectedKeys = Set("tyyppi", "oid", "tila", "oppilaitos", "suoritukset", "lisätiedot")
    val actualKeys = opiskeluoikeus.asInstanceOf[JObject].obj.map(_._1).toSet
    actualKeys should contain allElementsOf expectedKeys
  }

  private def validateJärjestämismuodot(opiskeluoikeus: JValue) = {
    val suoritukset = (opiskeluoikeus \ "suoritukset").children
    suoritukset should not be empty

    val järjestämismuodot = (suoritukset.head \ "järjestämismuodot").children
    järjestämismuodot should not be empty
  }

  private def validateOsaamisenHankkimistavatJaKoulutussopimukset(opiskeluoikeus: JValue) = {
    val suoritukset = (opiskeluoikeus \ "suoritukset").children
    suoritukset should not be empty

    val osaamisenHankkimistavat = (suoritukset.head \ "osaamisenHankkimistavat").children
    osaamisenHankkimistavat should not be empty

    val koulutussopimukset = (suoritukset.head \ "koulutussopimukset").children
    koulutussopimukset should not be empty
  }

  private def validateLisätiedot(opiskeluoikeudet: List[JValue]) = {
    val lisätiedollinen = opiskeluoikeudet.find { oo =>
      val lisätiedot = (oo \ "lisätiedot")
      lisätiedot != JNothing && lisätiedot != JNull && lisätiedot.children.nonEmpty
    }

    lisätiedollinen should not be empty

    val lisätiedot = lisätiedollinen.get \ "lisätiedot"

    val virtaOpiskeluoikeudenTyyppi = lisätiedot \ "virtaOpiskeluoikeudenTyyppi"
    (virtaOpiskeluoikeudenTyyppi \ "koodiarvo") should not be JNothing
    (virtaOpiskeluoikeudenTyyppi \ "koodiarvo") should not be JNull

    val lukukausiIlmoittautuminen = (lisätiedot \ "lukukausiIlmoittautuminen").children
    lukukausiIlmoittautuminen should not be empty
  }
}
