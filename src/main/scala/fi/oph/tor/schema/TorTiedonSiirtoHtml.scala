package fi.oph.tor.schema
import com.tristanhunt.knockoff.DefaultDiscounter._
import fi.oph.tor.json.Json
import fi.oph.tor.schema.generic.Schema
import scala.xml.Elem

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

## REST-rajapinnat

### PUT /tor/api/oppija

Lisää/päivittää oppijan ja opiskeluoikeuksia.

Käytetään alla tarkemmin kuvattua JSON-dataformaattia, jossa on seuraava rakenne:

```json
{
  "henkilö" : {
    "hetu" : "010101-123N",
    "etunimet" : "matti pekka",
    "kutsumanimi" : "matti",
    "sukunimi" : "virtanen"
  },
  "opiskeluoikeudet" : [ {
    "paikallinenId" : "847823465",
    "alkamispäivä" : "2016-09-01",
    ...
  }]
}
```

Edellyttää HTTP Basic Authentication -tunnistautumisen, eli käytännössä `Authorization`-headerin HTTP-pyyntöön.

Kokeile rajapintaa curlilla:

     curl -v -H 'Content-Type: application/json' --user username:password -X PUT  -d '{"henkilö": {"oid":"123"}, "opiskeluoikeudet": []}' http://tordev.tor.oph.reaktor.fi/tor/api/oppija

Muista korvata username ja password oikealla käyttäjätunnuksella ja salasanalla.

Rajapinta palauttaa

- 401 UNAUTHORIZED jos käyttäjä ei ole tunnistautunut
- 403 FORBIDDEN jos käyttäjällä ei ole tarvittavia oikeuksia tiedon päivittämiseen
- 400 BAD REQUEST jos syöte ei ole validi
- 200 OK jos lisäys/päivitys onnistuu

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

Scalaa osaaville ehkä nopein tapa tutkia tietomallia on kuitenkin sen lähdekoodi. Githubista löytyy sekä [scheman](https://github.com/Opetushallitus/tor/blob/master/src/main/scala/fi/oph/tor/schema/TorOppija.scala), että [esimerkkien](https://github.com/Opetushallitus/tor/blob/master/src/main/scala/fi/oph/tor/schema/TorOppijaExamples.scala) lähdekoodit.

"""

  def html = {
    <html>
      <head>
        <meta charset="UTF-8"></meta>
        <link rel="stylesheet" type="text/css" href="css/documentation.css"></link>
      </head>
      <body>
        {toXHTML( knockoff(markdown) )}
        <h2>REST-rajapinnat</h2>
        Kaikki rajapinnat vaativat HTTP Basic Authentication -tunnistautumisen, eli käytännössä `Authorization`-headerin HTTP-pyyntöön.
        <div>
        {
          TorApiOperations.operations.map { operation =>
            <div>
              <h3>{operation.method} {operation.path}</h3>
              {operation.doc}
              <ul class="status-codes">
                {operation.statusCodes.map { case (status, text) =>
                  <li>{status} {text}</li>
                }}
              </ul>
              <p>Kokeile heti!</p>
              <div class="api-tester" data-method={operation.method} data-path={operation.path}>
                <div class="examples"><label>Esimerkkejä<select>
                  {operation.examples.map { example =>
                    <option data-exampledata={Json.writePretty(example.data)}>{example.name}</option>
                  }}
                </select></label></div>
                <textarea cols="80" rows="50">{Json.writePretty(operation.examples(0).data)}</textarea>
                <div class="buttons">
                  <button class="try">Kokeile</button>
                </div>
                <div class="result"></div>
              </div>
            </div>
          }
        }
        </div>
        <div>
          <h2>Esimerkkidata annotoituna</h2>
          <p>Toinen hyvä tapa tutustua tiedonsiirtoprotokollaan on tutkia esimerkkiviestejä. Alla joukko viestejä, joissa oppijan opinnot ovat eri vaiheissa. Kussakin esimerkissa on varsinaisen JSON-sisällön lisäksi schemaan pohjautuva annotointi ja linkitykset koodistoon ja OKSA-sanastoon.</p>
        </div>{
        TorOppijaExamples.examples.map { example =>
          <div>
            <h3>{example.description} <small><a href={"/tor/documentation/examples/" + example.name + ".json"}>lataa JSON</a></small></h3>
            <table class="json">
              {SchemaToJsonHtml.buildHtml(TorSchema.schema, example.data)}
            </table>
          </div>
        }
        }
        <script src="js/documentation.js"></script>
      </body>
    </html>
  }
}

object TorApiOperations {
 val operations = List(
   ApiOperation(
     "PUT", "/tor/api/oppija",
     <div>Lisää/päivittää oppijan ja opiskeluoikeuksia.</div>,
     TorOppijaExamples.examples, TorSchema.schema,
     List(
       (401,"UNAUTHORIZED jos käyttäjä ei ole tunnistautunut"),
       (403,"FORBIDDEN jos käyttäjällä ei ole tarvittavia oikeuksia tiedon päivittämiseen"),
       (400,"BAD REQUEST jos syöte ei ole validi"),
       (200,"OK jos lisäys/päivitys onnistuu")
     )
   )
 )
}

case class ApiOperation(method: String, path: String, doc: Elem, examples: List[Example], schema: Schema, statusCodes: List[(Int, String)])
