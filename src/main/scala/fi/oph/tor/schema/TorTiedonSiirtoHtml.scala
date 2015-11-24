package fi.oph.tor.schema
import com.tristanhunt.knockoff.DefaultDiscounter._

object TorTiedonSiirtoHtml {
  def markdown =

"""

# TOR-tiedonsiirtoprotokolla

Tässä kuvataan TOR-järjestelmän tiedonsiirrossa käytettävä JSON-formaatti.

JSON Schema:

- [tor-oppija-schema.json](/tor/documentation/tor-oppija-schema.json)
- [visualisointi ja validointi](/tor/json-schema-viewer#tor-oppija-schema.json)

## Esimerkkidata annotoituna

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
