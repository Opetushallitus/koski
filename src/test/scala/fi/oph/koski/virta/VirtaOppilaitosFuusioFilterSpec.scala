package fi.oph.koski.virta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.util.Files
import org.scalatest.{FreeSpec, Matchers}

import scala.xml.Utility.trim

class VirtaOppilaitosFuusioFilterSpec extends FreeSpec with Matchers {
  private lazy val mockDataDir = KoskiApplication.defaultConfig.getString("virta.mockDataDir")

  "Karsii pois fuusioduplikaatit" in {
    val xmlWithDuplicates =
      <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
        <SOAP-ENV:Header/>
        <SOAP-ENV:Body>
          <virtaluku:OpiskelijanKaikkiTiedotResponse xmlns:virtaluku="http://tietovaranto.csc.fi/luku">
            <virta:Virta xmlns:virta="urn:mace:funet.fi:virta/2015/09/01">
              <virta:Opiskelija avain="opiskelija-avain-123">
                <virta:Opiskeluoikeudet>
                  <virta:Opiskeluoikeus avain="oo-avain-123" opiskelijaAvain="opiskelija-avain-123">
                    <virta:Myontaja>10122</virta:Myontaja>
                    <virta:Organisaatio>
                      <virta:Rooli>5</virta:Rooli>
                      <virta:Koodi>01905</virta:Koodi>
                    </virta:Organisaatio>
                  </virta:Opiskeluoikeus>
                </virta:Opiskeluoikeudet>
                <virta:Opintosuoritukset>
                  <virta:Opintosuoritus opiskelijaAvain="opiskelija-avain-123">
                    <virta:Myontaja>10122</virta:Myontaja>
                    <virta:Organisaatio>
                      <virta:Rooli>5</virta:Rooli>
                      <virta:Koodi>01905</virta:Koodi>
                    </virta:Organisaatio>
                  </virta:Opintosuoritus>
                </virta:Opintosuoritukset>
              </virta:Opiskelija>
              <virta:Opiskelija avain="opiskelija-avain-123">
                <virta:Opiskeluoikeudet>
                  <virta:Opiskeluoikeus avain="oo-avain-123" opiskelijaAvain="opiskelija-avain-123">
                    <virta:Myontaja>01905</virta:Myontaja>
                  </virta:Opiskeluoikeus>
                </virta:Opiskeluoikeudet>
                <virta:Opintosuoritukset>
                  <virta:Opintosuoritus opiskelijaAvain="opiskelija-avain-123">
                    <virta:Myontaja>01905</virta:Myontaja>
                  </virta:Opintosuoritus>
                </virta:Opintosuoritukset>
              </virta:Opiskelija>
            </virta:Virta>
          </virtaluku:OpiskelijanKaikkiTiedotResponse>
        </SOAP-ENV:Body>
      </SOAP-ENV:Envelope>

    val xmlWithoutDuplicates = <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
      <SOAP-ENV:Header/>
      <SOAP-ENV:Body>
        <virtaluku:OpiskelijanKaikkiTiedotResponse xmlns:virtaluku="http://tietovaranto.csc.fi/luku">
          <virta:Virta xmlns:virta="urn:mace:funet.fi:virta/2015/09/01">
            <virta:Opiskelija avain="opiskelija-avain-123">
              <virta:Opiskeluoikeudet></virta:Opiskeluoikeudet>
              <virta:Opintosuoritukset></virta:Opintosuoritukset>
            </virta:Opiskelija>
            <virta:Opiskelija avain="opiskelija-avain-123">
              <virta:Opiskeluoikeudet>
                <virta:Opiskeluoikeus avain="oo-avain-123" opiskelijaAvain="opiskelija-avain-123">
                  <virta:Myontaja>01905</virta:Myontaja>
                </virta:Opiskeluoikeus>
              </virta:Opiskeluoikeudet>
              <virta:Opintosuoritukset>
                <virta:Opintosuoritus opiskelijaAvain="opiskelija-avain-123">
                  <virta:Myontaja>01905</virta:Myontaja>
                </virta:Opintosuoritus>
              </virta:Opintosuoritukset>
            </virta:Opiskelija>
          </virta:Virta>
        </virtaluku:OpiskelijanKaikkiTiedotResponse>
      </SOAP-ENV:Body>
    </SOAP-ENV:Envelope>

    trim(VirtaOppilaitosFuusioFilter.discardDuplicates(xmlWithDuplicates)) should equal(trim(xmlWithoutDuplicates))
  }

  "Palauttaa duplikaatittomat xml:ät sellaisenaan" in {
    List(MockOppijat.dippainssi, MockOppijat.korkeakoululainen, MockOppijat.amkValmistunut, MockOppijat.opintojaksotSekaisin,
      MockOppijat.amkKesken, MockOppijat.amkKeskeytynyt, MockOppijat.monimutkainenKorkeakoululainen, MockOppijat.montaJaksoaKorkeakoululainen
    ).map(_.hetu.get)
     .map(loadVirtaXml).foreach { xml =>
      VirtaOppilaitosFuusioFilter.discardDuplicates(xml) should equal(xml)
    }
  }

  private def loadVirtaXml(hetu: String) = {
    val filename = s"$mockDataDir/opintotiedot/$hetu.xml"
    Files.asString(filename).map(scala.xml.XML.loadString).get
  }
}
