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

## REST-rajapinnat

Kaikki rajapinnat vaativat HTTP Basic Authentication -tunnistautumisen, eli käytännössä `Authorization`-headerin HTTP-pyyntöön.


"""

  def html = {
    <html>
      <head>
        <meta charset="UTF-8"></meta>
        <link rel="stylesheet" type="text/css" href="css/documentation.css"></link>
      </head>
      <body>
        {toXHTML( knockoff(markdown) )}
        <div>
        {
          TorApiOperations.operations.map { operation =>
            <div>
              <h3>{operation.method} {operation.path}</h3>
              {operation.doc}
              <h4>Paluukoodit</h4>
              <ul class="status-codes">
                {operation.statusCodes.map { case (status, text) =>
                  <li>{status} {text}</li>
                }}
              </ul>
              <h4>Kokeile heti</h4>
              <div class="api-tester" data-method={operation.method} data-path={operation.path}>
                {
                  if (operation.examples.length > 0) {
                    <div class="postdata">
                      <h4>Syötedata</h4>
                      <div class="examples"><label>Esimerkkejä<select>
                        {operation.examples.map { example =>
                          <option data-exampledata={Json.writePretty(example.data)}>{example.name}</option>
                        }}
                      </select></label></div>
                      <textarea cols="80" rows="50">{Json.writePretty(operation.examples(0).data)}</textarea>
                    </div>
                  } else if (operation.parameters.length > 0) {
                    <div class="parameters">
                      <h4>Parametrit</h4>
                      <table>
                        <thead>
                            <tr><th>Nimi</th><th>Merkitys</th><th>Arvo</th></tr>
                        </thead>
                        <tbody>
                          { operation.parameters.map { parameter =>
                            <tr><td>{parameter.name}</td><td>{parameter.description}</td><td><input name={parameter.name} value={parameter.example}></input></td></tr>
                          }}
                        </tbody>
                      </table>
                    </div>
                  } else {
                    <div></div>
                  }
                }

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
        <script src="js/polyfills/promise.js"></script>
        <script src="js/polyfills/fetch.js"></script>
        <script src="js/polyfills/dataset.js"></script>
        <script src="js/documentation.js"></script>
      </body>
    </html>
  }
}

object TorApiOperations {
 val operations = List(
   ApiOperation(
      "GET", "/tor/api/oppija/search",
      <div>Etsii oppijoita annetulla hakusanalla. Hakusana voi olla hetu, oppija-oid tai nimen osa. Tuloksiin sisällytetään vain ne oppijat, joilla on vähintään yksi opinto-oikeus, johon käyttäjällä on katseluoikeus.</div>,
      Nil,
      List(Parameter("query", "Hakusana, joka voi olla hetu, oppija-oid tai nimen osa.", "eero")),
      List(
        (200, "OK, jos haku onnistuu. Myös silloin kun ei löydy yhtään tulosta."),
        (400, "BAD REQUEST jos hakusana puuttuu"),
        (401,"UNAUTHORIZED jos käyttäjä ei ole tunnistautunut")
      )
   ),
   ApiOperation(
    "GET", "/tor/api/oppija",
     <div>Etsii oppijoita annetuilla parametreilla. Tuloksiin sisällytetään vain ne oppijat, joilla on vähintään yksi opinto-oikeus, johon käyttäjällä on katseluoikeus.</div>,
     Nil,
     List(
       Parameter("opiskeluoikeusPäättynytAikaisintaan","Päivämäärä jonka jälkeen opiskeluoikeus on päättynyt","2016-01-01"),
       Parameter("opiskeluoikeusPäättynytViimeistään","Päivämäärä jota ennen opiskeluoikeus on päättynyt","2016-12-31"),
       Parameter("tutkinnonTila","Opiskeluoikeuden juurisuorituksen tila: VALMIS, KESKEN, KESKEYTYNYT","VALMIS")
     ),
     List(
       (200, "OK jos haku onnistuu"),
       (400, "BAD REQUEST jos hakuparametria ei tueta, tai hakuparametri on virheellinen."),
       (401, "UNAUTHORIZED jos käyttäjä ei ole tunnistautunut")
     )
   ),
   ApiOperation(
     "GET", "/tor/api/oppija/validate",
     <div>Etsii oppijoita annetuilla parametreilla ja validoi hakutulokset. Tuloksiin sisällytetään vain ne oppijat, joilla on vähintään yksi opinto-oikeus, johon käyttäjällä on katseluoikeus.</div>,
     Nil,
     List(
       Parameter("opiskeluoikeusPäättynytAikaisintaan","Päivämäärä jonka jälkeen opiskeluoikeus on päättynyt","2016-01-01"),
       Parameter("opiskeluoikeusPäättynytViimeistään","Päivämäärä jota ennen opiskeluoikeus on päättynyt","2016-12-31"),
       Parameter("tutkinnonTila","Opiskeluoikeuden juurisuorituksen tila: VALMIS, KESKEN, KESKEYTYNYT","VALMIS")
     ),
     List(
       (200, "OK jos haku onnistuu. Mahdolliset validointivirheet palautuu json-vastauksessa."),
       (400, "BAD REQUEST jos hakuparametria ei tueta, tai hakuparametri on virheellinen."),
       (401, "UNAUTHORIZED jos käyttäjä ei ole tunnistautunut")
     )
   ),
   ApiOperation(
     "GET", "/tor/api/oppija/{oid}",
     <div>Hakee oppijan tiedot ja opiskeluoikeudet suorituksineen.</div>,
     Nil,
     List(Parameter("oid", "Oppijan tunniste", "1.2.246.562.24.00000000001")),
     List(
       (200, "OK, jos haku onnistuu."),
       (401,"UNAUTHORIZED jos käyttäjä ei ole tunnistautunut"),
       (404, "NOT FOUND jos oppijaa ei löydy tai käyttäjällä ei ole oikeuksia oppijan tietojen katseluun.")
     )
   ),
   ApiOperation(
     "GET", "/tor/api/oppija/validate/{oid}",
     <div>Validoi oppijan kantaan tallennetun datan oikeellisuuden</div>,
     Nil,
     List(Parameter("oid", "Oppijan tunniste", "1.2.246.562.24.00000000001")),
     List(
       (200, "OK, jos haku onnistuu. Mahdolliset validointivirheet palautuu json-vastauksessa."),
       (401,"UNAUTHORIZED jos käyttäjä ei ole tunnistautunut"),
       (404, "NOT FOUND jos oppijaa ei löydy tai käyttäjällä ei ole oikeuksia oppijan tietojen katseluun.")
     )
   ),
   ApiOperation(
     "PUT", "/tor/api/oppija",
     <div>
       <p>Lisää/päivittää oppijan ja opiskeluoikeuksia. Palauttaa henkilön <em>oid</em>-arvon, eli henkilön yksilöivän tunnisteen TOR ja Opintopolku-järjestelmissä.</p>
       <p>
         Tallennettava henkilö tunnistetaan joko henkilötunnuksen tai <em>oid</em>in perusteella. Tietojen päivittäminen on huomattavasti
         tehokkaampaa käytettäessä oidia, joten sen käyttöä suositellaan vahvasti. Jos lähdejärjestelmässä ei alun perin ole oideja, on ne mahdollista
         kerätä tätä rajapintaa kutsuttaessa; rajapinta palauttaa aina oppijan oidin.
       </p>
     </div>,
     TorOppijaExamples.examples,
     Nil,
     List(
       (200,"OK jos lisäys/päivitys onnistuu"),
       (401,"UNAUTHORIZED jos käyttäjä ei ole tunnistautunut"),
       (403,"FORBIDDEN jos käyttäjällä ei ole tarvittavia oikeuksia tiedon päivittämiseen"),
       (400,"BAD REQUEST jos syöte ei ole validi")
     )
   )
 )
}

case class ApiOperation(method: String, path: String, doc: Elem, examples: List[Example], parameters: List[Parameter], statusCodes: List[(Int, String)])
case class Parameter(name: String, description: String, example: String)