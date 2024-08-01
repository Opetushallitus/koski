package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.api.misc.OpiskeluoikeusTestMethods
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat._
import fi.oph.koski.koskiuser.{MockUser, MockUsers}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.scalaschema.Serializer.format
import org.json4s.jackson.JsonMethods
import org.json4s.{JNothing, JNull, JObject, JValue}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.xml.{Utility, XML}

class HslSpec extends AnyFreeSpec with KoskiHttpSpec with OpiskeluoikeusTestMethods with Matchers with BeforeAndAfterAll {
  val opiskelija = KoskiSpecificMockOppijat.markkanen

  "Hsl API" - {
    "vaatii HSL käyttäjän" in {
      KoskiApplicationForTests.mydataRepository.create(opiskelija.oid, "hsl")
      MockUsers.users
        .diff(List(MockUsers.hslKäyttäjä, MockUsers.suomiFiKäyttäjä))
        .foreach { user =>
          postHsl(user, opiskelija.hetu.get) {
            verifySOAPError("forbidden.vainPalveluvayla", "Sallittu vain palveluväylän kautta")
          }
        }
      postHsl(MockUsers.suomiFiKäyttäjä, opiskelija.hetu.get) {
        verifySOAPError("forbidden.kiellettyKäyttöoikeus", "Ei sallittu näillä käyttöoikeuksilla")
      }
      postHsl(MockUsers.hslKäyttäjä, ammattilainen.hetu.get) {
        verifySOAPError("forbidden.vainSallittuKumppani", "X-ROAD-MEMBER:llä ei ole lupaa hakea opiskelijan tietoja")
      }
      postHsl(MockUsers.hslKäyttäjä, "150966-5900") {
        verifySOAPError("notFound.oppijaaEiLöydyTaiEiOikeuksia", "Oppijaa ei löydy annetulla oidilla tai käyttäjällä ei ole oikeuksia tietojen katseluun.")
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
        val actualJson = parseOpintoOikeudetJson()
        (actualJson \ "henkilö") should equal(JsonMethods.parse("""{"oid": "1.2.246.562.24.00000000003", "syntymäaika": "1954-01-08"}"""))
      }
    }

    "opiskeluoikeuden kentät" in {
      postHsl(MockUsers.hslKäyttäjä, opiskelija.hetu.get) {
        verifyResponseStatusOk()
        val actualJson = parseOpintoOikeudetJson()
        val opiskeluoikeudet = (actualJson \ "opiskeluoikeudet").children

        opiskeluoikeudet should have size 1

        val expectedKeys = Set("tyyppi", "oid", "tila", "oppilaitos", "suoritukset", "lisätiedot")
        val actualKeys = opiskeluoikeudet.head.asInstanceOf[JObject].obj.map(_._1).toSet
        actualKeys should contain allElementsOf expectedKeys
      }
    }

    "tarkista opiskeluoikeus tila" in {
      postHsl(MockUsers.hslKäyttäjä, opiskelija.hetu.get) {
        verifyResponseStatusOk()
        val actualJson = parseOpintoOikeudetJson()
        val opiskeluoikeudet = (actualJson \ "opiskeluoikeudet").children

        opiskeluoikeudet should not be empty

        val tila = opiskeluoikeudet.head \ "tila" \ "opiskeluoikeusJaksot"

        (tila(0) \ "tila" \ "koodiarvo").extract[String] should equal("lasna")
        (tila(0) \ "alku").extract[String] should equal("2019-05-30")
      }
    }

    "tarkista opiskeluoikeus tila - ei tietoa opintojenRahoitus" in {
      KoskiApplicationForTests.mydataRepository.create(aikuisOpiskelijaMuuRahoitus.oid, "hsl")
      postHsl(MockUsers.hslKäyttäjä, opiskelija.hetu.get) {
        verifyResponseStatusOk()
        val opintoOikeudetXml = soapResponse() \ "Body" \ "opintoOikeudetServiceResponse" \ "opintoOikeudet"
        val opintoOikeudetJsonString = (opintoOikeudetXml.text)
        val actualJson = JsonMethods.parse(opintoOikeudetJsonString)
        val opiskeluoikeudet = (actualJson \ "opiskeluoikeudet").children

        val rahoitukset = opiskeluoikeudet.flatMap { oikeus =>
          (oikeus \ "tila" \ "opiskeluoikeusjaksot").children.flatMap { jakso =>
            (jakso \ "opintojenRahoitus").children
          }
        }

        rahoitukset should have size 0
      }
    }
  }

  "tarkista opiskeluoikeus oppilaitos" in {
    postHsl(MockUsers.hslKäyttäjä, opiskelija.hetu.get) {
      verifyResponseStatusOk()
      val actualJson = parseOpintoOikeudetJson()
      val opiskeluoikeudet = (actualJson \ "opiskeluoikeudet").children

      opiskeluoikeudet should not be empty

      val oppilaitos = opiskeluoikeudet.head \ "oppilaitos"
      (oppilaitos \ "nimi" \ "fi").extract[String] should equal("Omnia")
      (oppilaitos \ "oppilaitosnumero" \ "koodiarvo").extract[String] should equal("10054")
    }
  }

  "tarkista opiskeluoikeus arvioituPäättymispäivä" in {
    postHsl(MockUsers.hslKäyttäjä, reformitutkinto.hetu.get) {
      verifyResponseStatusOk()
      val actualJson = parseOpintoOikeudetJson()
      val opiskeluoikeudet = (actualJson \ "opiskeluoikeudet").children

      opiskeluoikeudet should not be empty

      val firstOpiskeluoikeus = opiskeluoikeudet.head

      (firstOpiskeluoikeus \ "arvioituPäättymispäivä").extract[String] should equal("2020-05-31")
    }
  }

  "tarkista lisätiedot osaAikaisuusjaksot" in {
    KoskiApplicationForTests.mydataRepository.create(amis.oid, "hsl")
    postHsl(MockUsers.hslKäyttäjä, amis.hetu.get) {
      verifyResponseStatusOk()
      val actualJson = parseOpintoOikeudetJson()
      val opiskeluoikeudet = (actualJson \ "opiskeluoikeudet").children

      opiskeluoikeudet should not be empty

      val firstOpiskeluoikeus = opiskeluoikeudet.head

      val lisatiedot = firstOpiskeluoikeus \ "lisätiedot"

      val osaAikaisuusjaksot = (lisatiedot \ "osaAikaisuusjaksot").children

      val jakso1 = osaAikaisuusjaksot.find { jakso =>
        (jakso \ "alku").extract[String] == "2012-09-01"
      }

      val jakso2 = osaAikaisuusjaksot.find { jakso =>
        (jakso \ "alku").extract[String] == "2019-05-08"
      }

      jakso1 should not be empty
      jakso2 should not be empty

      (jakso1.get \ "osaAikaisuus").extract[Int] shouldEqual 80
      (jakso2.get \ "osaAikaisuus").extract[Int] shouldEqual 60
    }
  }

  "ammatillinen opiskeluoikeus" - {
    "sisältää tiedon oppisopimuksesta jos olemassa" in {
      KoskiApplicationForTests.mydataRepository.create(reformitutkinto.oid, "hsl")
      postHsl(MockUsers.hslKäyttäjä, reformitutkinto.hetu.get) {
        verifyResponseStatusOk()
        val opintoOikeudetXml = soapResponse() \ "Body" \ "opintoOikeudetServiceResponse" \ "opintoOikeudet"
        val opintoOikeudetJsonString = (opintoOikeudetXml.text)
        val actualJson = JsonMethods.parse(opintoOikeudetJsonString)
        val opiskeluoikeudet = (actualJson \ "opiskeluoikeudet").children

        opiskeluoikeudet should have size 1

        val suoritukset = (opiskeluoikeudet.head \ "suoritukset").children
        suoritukset should not be empty

        val osaamisenHankkimistavat = (suoritukset.head \ "osaamisenHankkimistavat").children
        osaamisenHankkimistavat should not be empty

        val oppisopimuksellinen = osaamisenHankkimistavat.find { oh =>
          (oh \ "osaamisenHankkimistapa" \ "tunniste" \ "koodiarvo").extract[String] == "oppisopimus"
        }

        oppisopimuksellinen should not be empty
      }
    }

    "sisältää tiedon järjestämismuodoista (ja oppisopimuksesta) jos olemassa" in {
      KoskiApplicationForTests.mydataRepository.create(valviraaKiinnostavaTutkinto.oid, "hsl")
      postHsl(MockUsers.hslKäyttäjä, valviraaKiinnostavaTutkinto.hetu.get) {
        verifyResponseStatusOk()

        val actualJson = parseOpintoOikeudetJson()
        val opiskeluoikeudet = (actualJson \ "opiskeluoikeudet").children

        opiskeluoikeudet should have size 1

        val suoritukset = (opiskeluoikeudet.head \ "suoritukset").children
        suoritukset should not be empty

        val järjestämismuodot = (suoritukset.head \ "järjestämismuodot").children
        järjestämismuodot should not be empty

        val oppisopimusOld = järjestämismuodot.find { j =>
          (j \ "järjestämismuoto" \ "tunniste" \ "koodiarvo").extract[String] == "20"
        }
        oppisopimusOld should not be empty
      }
    }

    "sisältää tiedon koulutussopimuksista jos olemassa" - {
      KoskiApplicationForTests.mydataRepository.create(reformitutkinto.oid, "hsl")
      postHsl(MockUsers.hslKäyttäjä, reformitutkinto.hetu.get) {
        verifyResponseStatusOk()
        val actualJson = parseOpintoOikeudetJson()
        val opiskeluoikeudet = (actualJson \ "opiskeluoikeudet").children

        opiskeluoikeudet should have size 1
        validateKoulutussopimukset(opiskeluoikeudet.head)
      }
    }
  }

  "korkeakoulun opiskeluoikeuden lisätiedot" in {
    KoskiApplicationForTests.mydataRepository.create(dippainssi.oid, "hsl")
    postHsl(MockUsers.hslKäyttäjä, dippainssi.hetu.get) {
      verifyResponseStatusOk()
      val actualJson = parseOpintoOikeudetJson()
      val opiskeluoikeudet = (actualJson \ "opiskeluoikeudet").children

      opiskeluoikeudet should have size 2
      validateVirtaLisätiedot(opiskeluoikeudet)
    }
  }

  "tarkista lukion oppimäärä - suorituksen tyyppi json" in {
    KoskiApplicationForTests.mydataRepository.create(uusiLukio.oid, "hsl")
    postHsl(MockUsers.hslKäyttäjä, uusiLukio.hetu.get) {
      val opintoOikeudetXml = soapResponse() \ "Body" \ "opintoOikeudetServiceResponse" \ "opintoOikeudet"
      val opintoOikeudetJsonString = (opintoOikeudetXml.text)
      val actualJson = JsonMethods.parse(opintoOikeudetJsonString)
      val opiskeluoikeudet = (actualJson \ "opiskeluoikeudet").children

      val oppimääränSuoritus: Option[JValue] = opiskeluoikeudet.flatMap { oo =>
        (oo \ "suoritukset").children
      }.map { suoritus =>
        suoritus \ "tyyppi"
      }.find { tyyppi =>
        (tyyppi \ "koodiarvo").extractOpt[String].contains("lukionoppimaara")
      }

      oppimääränSuoritus shouldBe defined

      val expectedOppimääräJson = """{"koodiarvo": "lukionoppimaara", "nimi": {"fi": "Lukion oppimäärä", "sv": "Gymnasiets lärokurs", "en": "General upper secondary education syllabus"}, "koodistoUri": "suorituksentyyppi", "koodistoVersio": 1}"""
      val expectedObject = JsonMethods.parse(expectedOppimääräJson)

      oppimääränSuoritus.get should equal(expectedObject)
    }
  }


  private def postHsl[A](user: MockUser, hetu: String)(fn: => A): A = {
    post("api/palveluvayla/hsl", body = soapRequest(hetu), headers = authHeaders(user) ++ Map(("Content-type" -> "text/xml")))(fn)
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

  private def validateOsaamisenHankkimistavat(opiskeluoikeus: JValue) = {
    val suoritukset = (opiskeluoikeus \ "suoritukset").children
    suoritukset should not be empty

    val osaamisenHankkimistavat = (suoritukset.head \ "osaamisenHankkimistavat").children
    osaamisenHankkimistavat should not be empty
  }

  private def validateKoulutussopimukset(opiskeluoikeus: JValue) = {
    val suoritukset = (opiskeluoikeus \ "suoritukset").children
    suoritukset should not be empty

    val koulutussopimukset = (suoritukset.head \ "koulutussopimukset").children
    koulutussopimukset should not be empty
    koulutussopimukset.head shouldEqual JsonMethods.parse("""{"alku":"2018-08-01","paikkakunta":{"koodiarvo":"179","nimi":{"fi":"Jyväskylä","sv":"Jyväskylä"},"koodistoUri":"kunta","koodistoVersio":2},"maa":{"koodiarvo":"246","nimi":{"fi":"Suomi","sv":"Finland","en":"Finland"},"lyhytNimi":{"fi":"FI","sv":"FI","en":"FI"},"koodistoUri":"maatjavaltiot2","koodistoVersio":2}}""")
  }

  private def validateVirtaLisätiedot(opiskeluoikeudet: List[JValue]) = {
    val lisätiedollinen = opiskeluoikeudet.find { oo =>
      val lisätiedot = (oo \ "lisätiedot")
      lisätiedot != JNothing && lisätiedot != JNull && lisätiedot.children.nonEmpty
    }

    lisätiedollinen should not be empty

    val lisätiedot = lisätiedollinen.get \ "lisätiedot"

    val virtaOpiskeluoikeudenTyyppi = lisätiedot \ "virtaOpiskeluoikeudenTyyppi"
    (virtaOpiskeluoikeudenTyyppi \ "koodiarvo").extract[String] should equal("4")
    (virtaOpiskeluoikeudenTyyppi \ "koodistoUri").extract[String] should equal("virtaopiskeluoikeudentyyppi")

    val ilmoittautumisjaksot = (lisätiedot \ "lukukausiIlmoittautuminen" \ "ilmoittautumisjaksot").children
    ilmoittautumisjaksot should not be empty
    ilmoittautumisjaksot.head shouldEqual JsonMethods.parse("""{"alku":"2013-08-01","loppu":"2013-12-31","tila":{"koodiarvo":"1","nimi":{"fi":"Läsnä","sv":"Närvarande"},"koodistoUri":"virtalukukausiilmtila","koodistoVersio":1},"ylioppilaskunnanJäsen":true}""")
  }
}
