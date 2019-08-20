package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.{LocalJettyHttpSpecification, OpiskeluoikeusTestMethods}
import fi.oph.koski.henkilo.{MockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.koskiuser.{MockUser, MockUsers}
import fi.oph.koski.log.AuditLogTester
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

import scala.xml.{NodeSeq, XML, Utility}

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

    "palauttaa oppilaan tiedot hetun perusteella - vain osa opiskeluoikeuden kentistä mukana" in {
      postSuomiFiRekisteritiedot(MockUsers.suomiFiKäyttäjä, MockOppijat.ylioppilas.hetu.get) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "KANSALAINEN_SUOMIFI_KATSOMINEN"))
        val oppilaitokset = (soapResponse() \ "Body" \ "suomiFiRekisteritiedotResponse" \ "oppilaitokset").head
        oppilaitokset shouldEqual Utility.trim(
          <oppilaitokset>
            <oppilaitos>
              <nimi>
                <fi>Helsingin medialukio</fi>
              </nimi>
              <opiskeluoikeudet>
                <opiskeluoikeus>
                  <nimi>
                    <fi>Ylioppilastutkinto</fi>
                    <sv>Studentexamen</sv>
                    <en>Matriculation Examination</en>
                  </nimi>
                </opiskeluoikeus>
              </opiskeluoikeudet>
            </oppilaitos>
          </oppilaitokset>
        )
      }
    }

    "palauttaa oppilaan tiedot hetun perusteella - kaikki opiskeluoikeuden kentät mukana" in {
      postSuomiFiRekisteritiedot(MockUsers.suomiFiKäyttäjä, MockOppijat.ammattilainen.hetu.get) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "KANSALAINEN_SUOMIFI_KATSOMINEN"))
        val oppilaitokset = (soapResponse() \ "Body" \ "suomiFiRekisteritiedotResponse" \ "oppilaitokset").head
        oppilaitokset shouldEqual Utility.trim(
          <oppilaitokset>
            <oppilaitos>
              <nimi>
                <fi>Stadin ammattiopisto</fi>
              </nimi>
              <opiskeluoikeudet>
                <opiskeluoikeus>
                  <tila>
                    <fi>Valmistunut</fi>
                    <sv>Utexaminerad</sv>
                    <en>Graduated</en>
                  </tila>
                  <alku>2012-09-01</alku>
                  <loppu>2016-05-31</loppu>
                  <nimi>
                    <fi>Luonto- ja ympäristöalan perustutkinto</fi>
                    <sv>Grundexamen i natur och miljö</sv>
                  </nimi>
                </opiskeluoikeus>
              </opiskeluoikeudet>
            </oppilaitos>
          </oppilaitokset>
        )
      }
    }

    "palauttaa tyhjän lista oppilaitoksia jos oppilasta ei löydy hetun perusteella" in {
      List("261125-1531", "210130-5616", "080278-8433", "061109-011D", "070696-522Y", "010844-509V").foreach { hetu =>
        postSuomiFiRekisteritiedot(MockUsers.suomiFiKäyttäjä, hetu) {
          verifyResponseStatusOk()
          val oppilaitokset = (soapResponse() \ "Body" \ "suomiFiRekisteritiedotResponse" \ "oppilaitokset").head
          oppilaitokset.child shouldBe empty
        }
      }
    }

    "palauttaa SOAP-virheen jos Virta-palvelu ei vastaa" in {
      postSuomiFiRekisteritiedot(MockUsers.suomiFiKäyttäjä, MockOppijat.virtaEiVastaa.hetu.get) {
        verifySOAPError("unavailable.virta", "Korkeakoulutuksen opiskeluoikeuksia ei juuri nyt saada haettua. Yritä myöhemmin uudelleen.")
      }
    }

    "Suorituksen nimi" - {
      "Kun opiskeluoikeudessa on pelkkiä perusopetuksen vuosiluokkia käytetään sanaa 'Perusopetus'" in {
        // kesken olevat perusopetuksen päättötodistukset karsitaan pois -> opiskeluoikeudessa pelkkiä perusopetuksen vuosiluokkia
        ensimmäisenSuorituksenNimiRekisteritiedoissa(MockOppijat.ysiluokkalainen) shouldEqual "Perusopetus"
      }

      "Kun opiskeluoikeudessa on perusopetuksen oppiaineen oppimääriä käytetään nimenä suorituksen tyyppiä" in {
        ensimmäisenSuorituksenNimiRekisteritiedoissa(MockOppijat.montaOppiaineenOppimäärääOpiskeluoikeudessa) shouldEqual "Perusopetuksen oppiaineen oppimäärä"
      }

      "Kun opiskeluoikeudessa on lukion oppiaineen oppimääriä käytetään nimenä suorituksen tyyppiä" in {
        ensimmäisenSuorituksenNimiRekisteritiedoissa(MockOppijat.lukionAineopiskelija) shouldEqual "Lukion oppiaineen oppimäärä"
      }

      "Kun opiskeluoikeudessa on opintojaksojen seassa korkeakoulututkinto käytetään tutkinnon nimeä" in {
        ensimmäisenSuorituksenNimiRekisteritiedoissa(MockOppijat.opintojaksotSekaisin) shouldEqual "Fysioterapeutti (AMK)"
      }

      "Kun opiskeluoikeudessa on pelkkiä korkeakoulun opintojaksoja käytetään '<lkm> opintojaksoa'" in {
        ensimmäisenSuorituksenNimiRekisteritiedoissa(MockOppijat.korkeakoululainen) shouldEqual "69 opintojaksoa"
      }

      "Aikuisten perusopetuksessa käytetään suorituksen tyypin nimeä" in {
        ensimmäisenSuorituksenNimiRekisteritiedoissa(MockOppijat.aikuisOpiskelija) shouldEqual "Aikuisten perusopetuksen oppimäärä"
      }

      "Ammatillisen tutkinnon nimenä käytetään perusteen nimeä" in {
        ensimmäisenSuorituksenNimiRekisteritiedoissa(MockOppijat.ammattilainen) shouldEqual "Luonto- ja ympäristöalan perustutkinto"
      }

      "Osittaisen ammatillisen tutkinnon nimen loppuun tulee sana 'osittainen'" in {
        ensimmäisenSuorituksenNimiRekisteritiedoissa(MockOppijat.osittainenammattitutkinto) shouldEqual "Luonto- ja ympäristöalan perustutkinto, osittainen"
      }

      "Perustapauksessa käytetään suorituksen tunnisteen nimeä" in {
        ensimmäisenSuorituksenNimiRekisteritiedoissa(MockOppijat.lukiolainen) shouldEqual "Lukion oppimäärä"
        ensimmäisenSuorituksenNimiRekisteritiedoissa(MockOppijat.dippainssi) shouldEqual "Dipl.ins., konetekniikka"
        ensimmäisenSuorituksenNimiRekisteritiedoissa(MockOppijat.amkValmistunut) shouldEqual "Fysioterapeutti (AMK)"
        ensimmäisenSuorituksenNimiRekisteritiedoissa(MockOppijat.ylioppilas) shouldEqual "Ylioppilastutkinto"
        ensimmäisenSuorituksenNimiRekisteritiedoissa(MockOppijat.koululainen) shouldEqual "Perusopetus"
      }
    }
  }

  private def ensimmäisenSuorituksenNimiRekisteritiedoissa(oppija: LaajatOppijaHenkilöTiedot): String =
    (haeSuomiFiRekisteritiedot(oppija) \ "oppilaitokset" \ "oppilaitos" \ "opiskeluoikeudet" \ "opiskeluoikeus" \ "nimi" \ "fi").head.text

  private def haeSuomiFiRekisteritiedot(oppija: LaajatOppijaHenkilöTiedot): NodeSeq = postSuomiFiRekisteritiedot(MockUsers.suomiFiKäyttäjä, oppija.hetu.get) {
    verifyResponseStatusOk()
    soapResponse() \ "Body" \ "suomiFiRekisteritiedotResponse"
  }

  private def postSuomiFiRekisteritiedot[A](user: MockUser, hetu: String)(fn: => A): A = {
    post("api/palveluvayla/suomi-fi-rekisteritiedot", body = soapRequest(hetu), headers = authHeaders(user) ++ Map(("Content-type" -> "text/xml")))(fn)
  }

  private def verifySOAPError(faultstring: String, message: String): Unit = {
    response.status shouldBe 500
    val xml = soapResponse() \\ "Fault"
    (xml \ "faultcode").text shouldBe("SOAP-ENV:Server")
    (xml \ "faultstring").text shouldBe(faultstring)
    (xml \ "detail" \ "message").text shouldBe(message)
  }

  private def soapResponse() = Utility.trim(XML.loadString(response.body))

  private def soapRequest(hetu: String) =
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
