package fi.oph.tor.schema
import com.tristanhunt.knockoff.DefaultDiscounter._
import fi.oph.tor.schema.generic.Property

object TorTiedonSiirtoHtml {
  def markdown =

"""

# TOR-tiedonsiirtoprotokolla

Tässä kuvataan TOR-järjestelmän tiedonsiirrossa käytettävä JSON-formaatti.

JSON Schema: [tor-oppija-schema.json](/tor/documentation/tor-oppija-schema.json)

JSON Schema validaattori netissä: [jsonschemavalidator.net](http://www.jsonschemavalidator.net/). Voit kokeilla laittaa sinne meidän scheman ja esimerkin.

## Esimerkkidata annotoituna

"""

  def html = {
    <html>
      <head>
        <meta charset="UTF-8"></meta>
        <style>
          body {{
          font-family: Helvetica, Arial, sans-serif;
          color: rgb(51, 51, 51);
          font-size: 15px;
          }}
          small {{
          font-size: 10px;
          }}
          .json {{
          font-family: monospace;
          font-size: 12px;
          }}
          .metadata {{
          font-family: arial;
          }}
          .object,.array {{ color: rgb(51, 51, 51); }}
          .json .key {{ color: rgb(165, 116, 28); }}
          .json .value {{ color: green; }}
          .json tr:hover {{ background: #ffffbb; }}
          table.json {{ border-collapse: collapse; }}
          .koodisto {{ margin-left: 10px; }}
          a, a:visited {{
          text-decoration: none;
          color: teal;
          }}
        </style>
      </head>
      <body>
        {toXHTML( knockoff(markdown) )}
        {
        TorOppijaExamples.examples.map { example =>
          <div>
            <h3>{example.description} <small><a href={"/tor/documentation/examples/" + example.name + ".json"}>lataa JSON</a></small></h3>
            <table class="json">
              {SchemaToJsonHtml.buildHtml(Property("", TorSchema.schemaType, Nil), example.oppija, TorSchema.schema, 0)}
            </table>
          </div>
        }
        }
      </body>
    </html>
  }
}
