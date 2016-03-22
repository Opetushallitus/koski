package fi.oph.tor.documentation

import com.tristanhunt.knockoff.DefaultDiscounter._
import fi.oph.tor.http.ErrorCategory
import fi.oph.tor.json.Json
import fi.oph.tor.schema.TorSchema

import scala.xml.Elem

object TorTiedonSiirtoHtml {
  def markdown ="""

# TOR-tiedonsiirtoprotokolla

Tässä dokumentissa kuvataan TOR (Todennetun Osaamisen Rekisteri) -järjestelmän tiedonsiirrossa käytettävä protokolla.

Protokolla, kuten TOR-järjestelmäkin, on työn alla, joten kaikki voi vielä muuttua.

Muutama perusasia tullee kuitenkin säilymään:

- Rajapinnan avulla järjestelmään voi tallentaa tietoja oppijoiden opinto-oikeuksista, opintosuorituksista ja läsnäolosta oppilaitoksissa
- Rajapinnan avulla tietoja voi myös hakea ja muokata
- Rajapinnan käyttö vaatii autentikoinnin ja pääsy tietoihin rajataan käyttöoikeusryhmillä.
  Näin ollen esimerkiksi oikeus oppilaan tietyssä oppilaitoksessa suorittamien opintojen päivittämiseen voidaan antaa kyseisen oppilaitoksen henkilöstölle
- Rajapinta mahdollistaa myös automaattiset tiedonsiirrot tietojärjstelmien välillä. Näin esimerkiksi tietyt viranomaiset voivat saada tietoja TORista.
  Samoin oppilaitoksen tietojärjestelmät voivat päivittää tietoja TORiin.
- Järjestelmä tarjoaa REST-tyyppisen tiedonsiirtorajapinnan, jossa dataformaattina on JSON
- Samaa tiedonsiirtoprotokollaa ja dataformaattia pyritään soveltuvilta osin käyttämään sekä käyttöliittymille,
  jotka näyttävät tietoa loppukäyttäjille, että järjestelmien väliseen kommunikaatioon

## JSON-dataformaatti

Käytettävästä JSON-formaatista on laadittu työversio, jonka toivotaan vastaavan ammatillisen koulutuksen tarpeisiin.
Tällä formaatilla siis tulisi voida siirtää tietoja ammatillista koulutusta tarjoavien koulutustoimijoiden tietojärjestelmistä TORiin ja eteenpäin
tietoja tarvitsevien viramomaisten järjestelmiin ja loppukäyttäjiä, kuten oppilaitosten virkailijoita palveleviin käyttöliittymiin.
Formaattia on tarkoitus laajentaa soveltumaan myös muiden koulutustyyppien tarpeisiin, mutta näitä tarpeita ei ole vielä riittävällä tasolla kartoitettu,
jotta konkreettista dataformaattia voitaisiin suunnitella. Yksi formaatin suunnittelukriteereistä on toki ollut sovellettavuus muihinkin koulutustyyppeihin.

### JSON Schema

Käytettävä JSON-dataformaatti on kuvattu [JSON-schemalla](http://json-schema.org/), jota vasten siirretyt tiedot voidaan myös automaattisesti validoida.

- Tarkastele schemaa  [visualisointityökalun](/tor/json-schema-viewer#tor-oppija-schema.json) avulla.
  Tällä työkalulla voi myös validoida JSON-viestejä schemaa vasten. Klikkaamalla kenttiä saat näkyviin niiden tarkemmat kuvaukset.
- Lataa schema tiedostona: [tor-oppija-schema.json](/tor/documentation/tor-oppija-schema.json).


Tietokentät, joissa validit arvot on lueteltavissa, on kooditettu käyttäen hyväksi Opintopolku-järjestelmään kuuluvaa [Koodistopalvelua](https://github.com/Opetushallitus/koodisto).
Esimerkki tällaisesta kentästä on tutkintoon johtavan koulutuksen [koulutuskoodi](/tor/documentation/koodisto/koulutus/latest).

Scalaa osaaville ehkä nopein tapa tutkia tietomallia on kuitenkin sen lähdekoodi. Githubista löytyy sekä [scheman](https://github.com/Opetushallitus/tor/blob/master/src/main/scala/fi/oph/tor/schema/TorOppija.scala),
että [esimerkkien](https://github.com/Opetushallitus/tor/blob/master/src/main/scala/fi/oph/tor/documentation/TorOppijaExamples.scala) lähdekoodit.

## REST-rajapinnat

Kaikki rajapinnat vaativat HTTP Basic Authentication -tunnistautumisen, eli käytännössä `Authorization`-headerin HTTP-pyyntöön.

Rajapinnat on lueteltu ja kuvattu alla. Voit myös testata rajapintojen toimintaa tällä sivulla, kunhan käyt ensin [kirjautumassa sisään](/tor) järjestelmään.
Saat tarvittavat tunnukset TOR-kehitystiimiltä pyydettäessä.

Rajapintojen käyttämät virhekoodit on myös kuvattu alla. Virhetapauksissa rajapinnat käyttävät alla kuvattuja HTTP-statuskoodeja ja sisällyttävät tarkemmat virhekoodit ja selitteineen JSON-tyyppiseen paluuviestiin.
Samaan virhevastaukseen voi liittyä useampi virhekoodi/selite.

"""

  def html = {
    <html>
      <head>
        <meta charset="UTF-8"></meta>
        <link rel="stylesheet" type="text/css" href="css/documentation.css"></link>
        <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.1.0/styles/default.min.css"/>
        <link rel="stylesheet" type="text/css" href="/tor/codemirror/lib/codemirror.css"/>
        <script src="//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.1.0/highlight.min.js"></script>
        <script src="/tor/codemirror/lib/codemirror.js"></script>
        <script src="/tor/codemirror/mode/javascript/javascript.js"></script>
      </head>
      <body>
        {toXHTML( knockoff(markdown) )}
        { ApiTesterHtml.apiOperationsHtml }
        <div>
          <h2>Esimerkkidata annotoituna</h2>
          <p>
            Toinen hyvä tapa tutustua tiedonsiirtoprotokollaan on tutkia esimerkkiviestejä.
            Alla joukko viestejä, joissa oppijan opinnot ovat eri vaiheissa. Kussakin esimerkissa on varsinaisen JSON-sisällön lisäksi schemaan pohjautuva annotointi ja linkitykset koodistoon ja OKSA-sanastoon.
          </p>
        </div>
        { examplesHtml }
        <script src="js/polyfills/promise.js"></script>
        <script src="js/polyfills/fetch.js"></script>
        <script src="js/polyfills/dataset.js"></script>
        <script src="js/documentation.js"></script>
      </body>
    </html>
  }

  def examplesHtml: List[Elem] = {
    TorOppijaExamples.examples.map { example =>
      <div>
        <h3>
          {example.description}<small>
          <a href={"/tor/documentation/examples/" + example.name + ".json"}>lataa JSON</a>
        </small>
        </h3>
        <table class="json">
          {SchemaToJsonHtml.buildHtml(TorSchema.schema, example.data)}
        </table>
      </div>
    }
  }


}



case class ApiOperation(method: String, path: String, doc: Elem, examples: List[Example], parameters: List[Parameter], statusCodes: List[ErrorCategory])

sealed trait Parameter {
  def name: String
  def description: String
  def example: String
}

case class PathParameter(name: String, description: String, example: String) extends Parameter
case class QueryParameter(name: String, description: String, example: String) extends Parameter