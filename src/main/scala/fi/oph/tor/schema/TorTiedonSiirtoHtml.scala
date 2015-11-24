package fi.oph.tor.schema
import com.tristanhunt.knockoff.DefaultDiscounter._

object TorTiedonSiirtoHtml {
  def markdown ="""

# TOR-tiedonsiirtoprotokolla

Tässä dokumentissa kuvataan TOR (Todennetun Osaamisen Rekisteri) -järjestelmän tiedonsiirrossa käytettävä protokolla.

Protokolla, kuten TOR-järjestelmäkin, on työn alla, joten kaikki voi vielä muuttua.

Muutama perusasia tullee kuitenkin säilymään:

- Rajapinnan avulla järjestelmään voi tallentaa tietoja oppijoiden opinto-oikeuksista, opintosuorituksista ja läsnäolosta oppilaitoksissa
- Rajapinnan avulla tietoja voi myös hakea ja muokata
- Rajapinnan käyttö vaatii autentikoinnin ja pääsy tietoihin rajataan käyttöoikeusryhmillä. Näin ollen esimerkiksi oikeus oppilaan tietyssä oppilaitoksessa suorittamien opintojen päivittämiseen voidaan antaa kyseisen oppilaitoksen henkilöstölle
- Rajapinta mahdollistaa myös automaattiset tiedonsiirrot tietojärjstelmien välillä. Näin esimerkiksi tietyt viranomaiset voivat saada tietoja TORista. Samoin oppilaitoksen tietojärjestelmät voivat päivittää tietoja TORiin.
- Järjestelmä tarjoaa REST-tyyppisen tiedonsiirtorajapinnan, jossa dataformaattina on JSON
- Samaa tiedonsiirtoprotokollaa ja dataformaattia pyritään soveltuvilta osin käyttämään sekä käyttöliittymille, jotka näyttävät tietoa loppukäyttäjille, että järjestelmien väliseen kommunikaatioon

## JSON-dataformaatti

Käytettävästä JSON-formaatista on laadittu työversio, jonka toivotaan vastaavan ammatillisen koulutuksen tarpeisiin.
Tällä formaatilla siis tulisi voida siirtää tietoja ammatillista koulutusta tarjoavien koulutustoimijoiden tietojärjestelmistä TORiin ja eteenpäin
tietoja tarvitsevien viramomaisten järjestelmiin ja loppukäyttäjiä, kuten oppilaitosten virkailijoita palveleviin käyttöliittymiin.
Formaattia on tarkoitus laajentaa soveltumaan myös muiden koulutustyyppien tarpeisiin, mutta näitä tarpeita ei ole vielä riittävällä tasolla kartoitettu,
jotta konkreettista dataformaattia voitaisiin suunnitella. Yksi formaatin suunnittelukriteereistä on toki ollut sovellettavuus muihinkin koulutustyyppeihin.

Käytettävä JSON-dataformaatti on kuvattu [JSON-schemalla](http://json-schema.org/), jota vasten siirretyt tiedot voidaan myös automaattisesti validoida. Voit ladata TOR:ssa käytetyn scheman täältä: [tor-oppija-schema.json](/tor/documentation/tor-oppija-schema.json).

Tutustuminen käytettyyn dataformaattiin onnistunee parhaiten tutustumalla schemaan [visualisointityökalun](/tor/json-schema-viewer#tor-oppija-schema.json) avulla. Tällä työkalulla voi myös validoida JSON-viestejä schemaa vasten.
Klikkaamalla kenttiä saat näkyviin niiden tarkemmat kuvaukset.

Tietokentät, joissa validit arvot on lueteltavissa, on kooditettu käyttäen hyväksi Opintopolku-järjestelmään kuuluvaa [Koodistopalvelua](https://github.com/Opetushallitus/koodisto). Esimerkki tällaisesta kentästä on tutkintoon johtavan koulutuksen [koulutuskoodi](/tor/documentation/koodisto/koulutus/latest).

## Esimerkkidata annotoituna

Toinen hyvä tapa tutustua tiedonsiirtoprotokollaan on tutkia esimerkkiviestejä. Alla joukko viestejä, joissa oppijan opinnot ovat eri vaiheissa. Kussakin esimerkissa on varsinaisen JSON-sisällön lisäksi schemaan pohjautuva annotointi ja linkitykset koodistoon ja OKSA-sanastoon.

"""

  def html = {
    <html>
      <head>
        <meta charset="UTF-8"></meta>
        <link rel="stylesheet" type="text/css" href="css/documentation.css"></link>
      </head>
      <body>
        {toXHTML( knockoff(markdown) )}
        {
        TorOppijaExamples.examples.map { example =>
          <div>
            <h3>{example.description} <small><a href={"/tor/documentation/examples/" + example.name + ".json"}>lataa JSON</a></small></h3>
            <table class="json">
              {SchemaToJsonHtml.buildHtml(TorSchema.schema, example.oppija)}
            </table>
          </div>
        }
        }
        <script src="js/documentation.js"></script>
      </body>
    </html>
  }
}
