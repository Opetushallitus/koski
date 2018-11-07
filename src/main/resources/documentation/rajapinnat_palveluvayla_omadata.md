## Palveluväylä- ja omadata-rajapinnat

Tällä sivulla kuvataan rajapinnat, joilla kolmannet osapuolet (kumppanit) voivat pyytää
käyttölupaa kansalaisen tietoihin ja hakea kansalaisen tietoja Suomi.fi-palveluväylän kautta.

Kansalaisen tietoja voi hakea vain, mikäli kansalainen on antanut hakijalle nimenomaisen luvan tietojen hakemiseen.
Tämän lisäksi kumppaneiden tulee olla Opetushallituksen hyväksymiä ja heidän tunnisteensa tulee olla 
lisättyinä Kosken sovellukseen.

Dokumentaatiossa on käytetty kuvitteellista yhtiötä Acme Corporation, jolle on Koskessa annettu tunniste `acme`. 

### Luvan pyytäminen kansalaiselta

Käyttäjän selain ohjataan osoitteeseen **/koski/omadata/valtuutus/acme**
, missä käyttäjä voi halutessaan antaa luvan kumppanille.

Mikäli halutaan, että käyttäjän selain palaa luvan antamisen jälkeen tietylle sivulle, voidaan tämä tieto lisätä `callback`-parametriin:
**/koski/omadata/valtuutus/acme?callback=https%3A%2F%2Fwww.acme.org%2Fuser%2F**

### Kansalaisen opintotietojen hakeminen

Kun kansalainen on myöntänyt luvan tietojen hakemiseen, voidaan tietoja hakea Suomi.fi-palveluväylän kautta.
Tiedot haetaan kumppanin omalta liityntäpalvelimelta SOAP-requestilla, joka on kuvattu [täällä](https://sp.omadata.opintopolku.fi/?wsdl=true).

Esimerkkipyyntö:

    POST / HTTP/1.1
    Host: localhost:8080
    Content-Type: text/xml
    
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xro="http://x-road.eu/xsd/xroad.xsd" xmlns:id="http://x-road.eu/xsd/identifiers" xmlns:prod="http://docs.koski-xroad.fi/producer">
       <soapenv:Header>
          <xro:protocolVersion>4.0</xro:protocolVersion>
          <xro:issue>issue #123</xro:issue>
          <xro:id>ID123456</xro:id>
          <xro:userId>123456789</xro:userId>
          <xro:service id:objectType="SERVICE">
             <id:xRoadInstance>FI-DEV</id:xRoadInstance>
             <id:memberClass>GOV</id:memberClass>
             <id:memberCode>2769790-1</id:memberCode>
             <id:subsystemCode>koski</id:subsystemCode>
             <id:serviceCode>opintoOikeudetService</id:serviceCode>
             <id:serviceVersion>v1</id:serviceVersion>
          </xro:service>
          <xro:client id:objectType="SUBSYSTEM">
             <id:xRoadInstance>FI-DEV</id:xRoadInstance>
             <id:memberClass>COM</id:memberClass>
             <id:memberCode>1234567-1</id:memberCode>
             <id:subsystemCode>acme</id:subsystemCode>
          </xro:client>
       </soapenv:Header>
       <soapenv:Body>
          <prod:opintoOikeudetService>
             <prod:hetu>051198-997J</prod:hetu>
          </prod:opintoOikeudetService>
       </soapenv:Body>
    </soapenv:Envelope>

Palveluväylän vaatimien kenttien (Header) kuvaukset löytyvät [Palveluväylän sivuilta](https://esuomi.fi/palveluntarjoajille/palveluvayla/tekninen-aineisto/rajapintakuvaukset/x-road-tiedonsiirtoprotokolla/#4_SOAP-otsikkotiedot).

Body-kenttien kuvaukset:

 * hetu: haettavan kansalaisen henkilötunnus.

Esimerkkivastaus:

    <?xml version="1.0" encoding="UTF-8"?>
    <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:id="http://x-road.eu/xsd/identifiers" xmlns:xrd="http://x-road.eu/xsd/xroad.xsd">
      <SOAP-ENV:Header>
        <xrd:client id:objectType="SUBSYSTEM">
          <id:xRoadInstance>FI-DEV</id:xRoadInstance>
          <id:memberClass>COM</id:memberClass>
          <id:memberCode>1234567-1</id:memberCode>
          <id:subsystemCode>acme</id:subsystemCode>
        </xrd:client>
        <xrd:service id:objectType="SERVICE">
          <id:xRoadInstance>FI-DEV</id:xRoadInstance>
          <id:memberClass>GOV</id:memberClass>
          <id:memberCode>2769790-1</id:memberCode>
          <id:subsystemCode>koski</id:subsystemCode>
          <id:serviceCode>opintoOikeudetService</id:serviceCode>
          <id:serviceVersion>v1</id:serviceVersion>
        </xrd:service>
        <xrd:userId>123456789</xrd:userId>
        <xrd:id>ID123456</xrd:id>
        <xrd:protocolVersion>4.0</xrd:protocolVersion>
      </SOAP-ENV:Header>
      <SOAP-ENV:Body>
        <kns1:opintoOikeudetServiceResponse xmlns:kns1="http://docs.koski-xroad.fi/producer">
          <kns1:opintoOikeudet>
            <![CDATA[{"henkilö":{"oid":"1.2.123.456.78.12345678901","syntymäaika":"1998-05-11","etunimet":"Aarne","kutsumanimi":"Aarne","sukunimi":"Ammattilainen"},"opiskeluoikeudet":[{"oid":"1.2.123.456.78.12345678902","versionumero":1,"aikaleima":"2018-06-06T10:51:23.814081","oppilaitos":{"oid":"1.2.123.456.78.12345678903","oppilaitosnumero":{"koodiarvo":"10105","nimi":{"fi":"Stadin ammattiopisto"},"lyhytNimi":{"fi":"Stadin ammattiopisto"},"koodistoUri":"oppilaitosnumero","koodistoVersio":1},"nimi":{"fi":"Stadin ammattiopisto"},"kotipaikka":{"koodiarvo":"091","nimi":{"fi":"Helsinki","sv":"Helsingfors"},"koodistoUri":"kunta","koodistoVersio":1}},"arvioituPäättymispäivä":"2020-05-01","tila":{"opiskeluoikeusjaksot":[{"alku":"2016-09-01","tila":{"koodiarvo":"lasna","nimi":{"fi":"Läsnä"},"koodistoUri":"koskiopiskeluoikeudentila","koodistoVersio":1}}]},"tyyppi":{"koodiarvo":"ammatillinenkoulutus","nimi":{"fi":"Ammatillinen koulutus"},"lyhytNimi":{"fi":"Ammatillinen koulutus"},"koodistoUri":"opiskeluoikeudentyyppi","koodistoVersio":1},"alkamispäivä":"2016-09-01","suoritukset":[{}]}]}]]>
          </kns1:opintoOikeudet>
        </kns1:opintoOikeudetServiceResponse>
      </SOAP-ENV:Body>
    </SOAP-ENV:Envelope>


Vastauksen tietosisältö on kuvattu [wikissä](https://confluence.csc.fi/pages/viewpage.action?pageId=76536741), 
mutta käytännössä se on osajoukko Kosken [tietomallista](/koski/dokumentaatio/tietomalli).


Mikäli kansalainen ei ole antanut lupaa tietojensa käyttöön, lähetetään tietojen pyytäjälle allaoleva virheviesti:

    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
    <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
      <SOAP-ENV:Header/>
      <SOAP-ENV:Body>
        <SOAP-ENV:Fault>
          <faultcode>SOAP-ENV:Server.Forbidden</faultcode>
          <faultstring>Opinto-oikeus search failed due to insufficient permissions: ...</faultstring>
          <detail/>
        </SOAP-ENV:Fault>
      </SOAP-ENV:Body>
    </SOAP-ENV:Envelope>

Jonka `faultcode`-kenttä ilmaisee, ettei tietoja voida hakea puutteellisten oikeuksien takia.
