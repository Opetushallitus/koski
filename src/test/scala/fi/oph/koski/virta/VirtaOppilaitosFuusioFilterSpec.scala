package fi.oph.koski.virta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.util.Files
import org.scalatest.{FreeSpec, Matchers}

import scala.xml.Utility.trim

class VirtaOppilaitosFuusioFilterSpec extends FreeSpec with Matchers {
  private lazy val mockDataDir = KoskiApplication.defaultConfig.getString("virta.mockDataDir")
  private val tampereenYliopistoVanha = "01905"
  private val tampereenTeknillinenYliopisto = "01915"
  private val tampereenYliopisto = "10122"
  private val fuusioFilter = new VirtaOppilaitosFuusioFilter(List(tampereenYliopistoVanha, tampereenTeknillinenYliopisto), List(tampereenYliopisto))

  "Karsii pois fuusioduplikaattisuoritukset määriteltyjen oppilaitosten datoista" in {
    trim(fuusioFilter.poistaDuplikaattisuoritukset(Nil)(virtaFuusioXml(tampereenYliopistoVanha, tampereenYliopisto))) should equal(expected(tampereenYliopistoVanha, tampereenYliopisto))
    trim(fuusioFilter.poistaDuplikaattisuoritukset(Nil)(virtaFuusioXml(tampereenTeknillinenYliopisto, tampereenYliopisto))) should equal(expected(tampereenTeknillinenYliopisto, tampereenYliopisto))
  }

  "Ei karsi mitään muiden oppilaitosten datoista" in {
    val aaltoMyöntäjänäXml = virtaFuusioXml(myöntäjä = "10076", hyväksilukija = tampereenYliopisto)
    fuusioFilter.poistaDuplikaattisuoritukset(Nil)(aaltoMyöntäjänäXml) should equal(aaltoMyöntäjänäXml)
    val aaltoHyväksilukijanaXml = virtaFuusioXml(myöntäjä = tampereenYliopistoVanha, hyväksilukija = "10076")
    fuusioFilter.poistaDuplikaattisuoritukset(Nil)(aaltoHyväksilukijanaXml) should equal(aaltoHyväksilukijanaXml)
  }

  "Palauttaa duplikaatittomat xml:ät sellaisenaan" in {
    List(MockOppijat.dippainssi, MockOppijat.korkeakoululainen, MockOppijat.amkValmistunut, MockOppijat.opintojaksotSekaisin,
      MockOppijat.amkKesken, MockOppijat.amkKeskeytynyt, MockOppijat.monimutkainenKorkeakoululainen, MockOppijat.montaJaksoaKorkeakoululainen
    ).map(_.hetu.get)
     .map(loadVirtaXml).foreach { x =>
      fuusioFilter.poistaDuplikaattisuoritukset(Nil)(x) should equal(x)
    }
  }

  private def virtaFuusioXml(myöntäjä: String, hyväksilukija: String) =
    <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
      <SOAP-ENV:Header/>
      <SOAP-ENV:Body>
        <virtaluku:OpiskelijanKaikkiTiedotResponse xmlns:virtaluku="http://tietovaranto.csc.fi/luku">
          <virta:Virta xmlns:virta="urn:mace:funet.fi:virta/2015/09/01">
            <virta:Opiskelija avain="opiskelija-avain-123">
              <virta:Opiskeluoikeudet>
                <virta:Opiskeluoikeus avain="oo-avain-123" opiskelijaAvain="opiskelija-avain-123">
                  <virta:Myontaja>{hyväksilukija}</virta:Myontaja>
                  <virta:Organisaatio>
                    <virta:Rooli>3</virta:Rooli>
                    <virta:Koodi>41</virta:Koodi>
                  </virta:Organisaatio>
                  <virta:Organisaatio>
                    <virta:Rooli>5</virta:Rooli>
                    <virta:Koodi>{myöntäjä}</virta:Koodi>
                  </virta:Organisaatio>
                </virta:Opiskeluoikeus>
              </virta:Opiskeluoikeudet>
              <virta:Opintosuoritukset>
                <virta:Opintosuoritus avain="s-123" opiskelijaAvain="opiskelija-avain-123">
                  <virta:Myontaja>{hyväksilukija}</virta:Myontaja>
                  <virta:Organisaatio>
                    <virta:Rooli>5</virta:Rooli>
                    <virta:Koodi>{myöntäjä}</virta:Koodi>
                  </virta:Organisaatio>
                </virta:Opintosuoritus>
                <virta:Opintosuoritus avain="s-456" opiskelijaAvain="opiskelija-avain-123">
                  <virta:Myontaja>{hyväksilukija}</virta:Myontaja>
                  <virta:Organisaatio>
                    <virta:Rooli>5</virta:Rooli>
                    <virta:Koodi>{myöntäjä}</virta:Koodi>
                  </virta:Organisaatio>
                </virta:Opintosuoritus>
              </virta:Opintosuoritukset>
            </virta:Opiskelija>
            <virta:Opiskelija avain="opiskelija-avain-123">
              <virta:Opiskeluoikeudet>
                <virta:Opiskeluoikeus avain="oo-avain-123" opiskelijaAvain="opiskelija-avain-123">
                  <virta:Myontaja>{myöntäjä}</virta:Myontaja>
                </virta:Opiskeluoikeus>
              </virta:Opiskeluoikeudet>
              <virta:Opintosuoritukset>
                <virta:Opintosuoritus avain="s-123" opiskelijaAvain="opiskelija-avain-123">
                  <virta:Myontaja>{myöntäjä}</virta:Myontaja>
                </virta:Opintosuoritus>
              </virta:Opintosuoritukset>
            </virta:Opiskelija>
          </virta:Virta>
        </virtaluku:OpiskelijanKaikkiTiedotResponse>
      </SOAP-ENV:Body>
    </SOAP-ENV:Envelope>

  private def expected(myöntäjä: String, hyväksilukija: String) = trim(<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
    <SOAP-ENV:Header/>
    <SOAP-ENV:Body>
      <virtaluku:OpiskelijanKaikkiTiedotResponse xmlns:virtaluku="http://tietovaranto.csc.fi/luku">
        <virta:Virta xmlns:virta="urn:mace:funet.fi:virta/2015/09/01">
          <virta:Opiskelija avain="opiskelija-avain-123">
            <virta:Opiskeluoikeudet>
              <virta:Opiskeluoikeus avain="oo-avain-123" opiskelijaAvain="opiskelija-avain-123">
                <virta:Myontaja>{hyväksilukija}</virta:Myontaja>
                <virta:Organisaatio>
                  <virta:Rooli>3</virta:Rooli>
                  <virta:Koodi>41</virta:Koodi>
                </virta:Organisaatio>
                <virta:Organisaatio>
                  <virta:Rooli>5</virta:Rooli>
                  <virta:Koodi>{myöntäjä}</virta:Koodi>
                </virta:Organisaatio>
              </virta:Opiskeluoikeus>
            </virta:Opiskeluoikeudet>
            <virta:Opintosuoritukset>
              <virta:Opintosuoritus avain="s-456" opiskelijaAvain="opiskelija-avain-123">
                <virta:Myontaja>{hyväksilukija}</virta:Myontaja>
                <virta:Organisaatio>
                  <virta:Rooli>5</virta:Rooli>
                  <virta:Koodi>{myöntäjä}</virta:Koodi>
                </virta:Organisaatio>
              </virta:Opintosuoritus>
            </virta:Opintosuoritukset>
          </virta:Opiskelija>
          <virta:Opiskelija avain="opiskelija-avain-123">
            <virta:Opiskeluoikeudet>
              <virta:Opiskeluoikeus avain="oo-avain-123" opiskelijaAvain="opiskelija-avain-123">
                <virta:Myontaja>{myöntäjä}</virta:Myontaja>
              </virta:Opiskeluoikeus>
            </virta:Opiskeluoikeudet>
            <virta:Opintosuoritukset>
              <virta:Opintosuoritus avain="s-123" opiskelijaAvain="opiskelija-avain-123">
                <virta:Myontaja>{myöntäjä}</virta:Myontaja>
              </virta:Opintosuoritus>
            </virta:Opintosuoritukset>
          </virta:Opiskelija>
        </virta:Virta>
      </virtaluku:OpiskelijanKaikkiTiedotResponse>
    </SOAP-ENV:Body>
  </SOAP-ENV:Envelope>)

  private def loadVirtaXml(hetu: String) = {
    val filename = s"$mockDataDir/opintotiedot/$hetu.xml"
    Files.asString(filename).map(scala.xml.XML.loadString).get
  }
}
