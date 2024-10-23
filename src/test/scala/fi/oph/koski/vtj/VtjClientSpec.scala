package fi.oph.koski.vtj

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.huoltaja.HuollettavatVtjRepository
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.xml.Elem

class VtjClientSpec extends AnyFreeSpec with KoskiHttpSpec with Matchers {
  val mockResponse: Elem = {
    <VTJHenkiloVastaussanoma xsi:schemaLocation="http://xml.vrk.fi/schema/vtjkysely PERUSLT1.xsd" tietojenPoimintaaika="20151117112133" sanomatunnus="PERUSLT1" versio="1.0">
      <Asiakasinfo>
        <InfoS>17.11.2015 11:21</InfoS>
        <InfoR>17.11.2015 11:21</InfoR>
        <InfoE>17.11.2015 11:21</InfoE>
      </Asiakasinfo>
      <Paluukoodi koodi="0000">Haku onnistui</Paluukoodi>
      <Hakuperusteet>
        <Henkilotunnus hakuperustePaluukoodi="1" hakuperusteTekstiS="Löytyi" hakuperusteTekstiR="Hittades" hakuperusteTekstiE="Found">310118-998M</Henkilotunnus>
      </Hakuperusteet>
      <Henkilo>
        <Henkilotunnus voimassaolokoodi="1">310118-998M</Henkilotunnus>
        <NykyinenSukunimi>
          <Sukunimi>Haapakoski</Sukunimi>
        </NykyinenSukunimi>
        <NykyisetEtunimet>
          <Etunimet>Alina Kukka-Maaria</Etunimet>
        </NykyisetEtunimet>
        <Kotikunta>
          <Kuntanumero>915</Kuntanumero>
          <KuntaS>Varkaus</KuntaS>
          <KuntaR>Varkaus</KuntaR>
          <KuntasuhdeAlkupvm>19701125</KuntasuhdeAlkupvm>
        </Kotikunta>
        <VakinainenKotimainenLahiosoite>
          <LahiosoiteS>Iso Haukka Linnun Kuja 1 I 35</LahiosoiteS>
          <LahiosoiteR/>
          <Postinumero>78850</Postinumero>
          <PostitoimipaikkaS>VARKAUS</PostitoimipaikkaS>
          <PostitoimipaikkaR>VARKAUS</PostitoimipaikkaR>
          <AsuminenAlkupvm>19960429</AsuminenAlkupvm>
          <AsuminenLoppupvm/>
        </VakinainenKotimainenLahiosoite>
        <Aidinkieli>
          <AidinkieliTietokoodi>1</AidinkieliTietokoodi>
        </Aidinkieli>
        <Huollettava>
          <Henkilotunnus>010203A123B</Henkilotunnus>
          <Sukunimi>Virtanen</Sukunimi>
          <Etunimet>Mikko Juhani</Etunimet>
          <Suhde>Syntymävuosi</Suhde>
          <SuhdeTeksti>2003</SuhdeTeksti>
        </Huollettava>
        <Huollettava>
          <Henkilotunnus>010105A9413</Henkilotunnus>
          <Sukunimi>Virtanen</Sukunimi>
          <Etunimet>Jaakko Mikael</Etunimet>
          <Suhde>Syntymävuosi</Suhde>
          <SuhdeTeksti>2005</SuhdeTeksti>
        </Huollettava>
        <Kuolintiedot>
          <Kuolinpvm/>
        </Kuolintiedot>
      </Henkilo>
    </VTJHenkiloVastaussanoma>
  }

  "Response parsitaan oikein" in {
    val huollettavat = HuollettavatVtjRepository.parseHuollettavatFromVtjResponse(mockResponse)
    huollettavat should have size (2)

    huollettavat.head.hetu should equal("010203A123B")
    huollettavat.head.etunimet should equal("Mikko Juhani")
    huollettavat.head.sukunimi should equal("Virtanen")

    huollettavat.last.hetu should equal("010105A9413")
    huollettavat.last.etunimet should equal("Jaakko Mikael")
    huollettavat.last.sukunimi should equal("Virtanen")
  }
}
