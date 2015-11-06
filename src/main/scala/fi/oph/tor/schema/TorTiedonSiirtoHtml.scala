package fi.oph.tor.schema
import com.tristanhunt.knockoff.DefaultDiscounter._
object TorTiedonSiirtoHtml {
  def markdown = """

# TOR-tiedonsiirtoprotokolla

Tässä kuvataan TOR-järjestelmän tiedonsiirrossa käytettävä JSON-formaatti.

JSON Schema: [tor-oppija-schema.json](/tor/documentation/tor-oppija-schema.json)

JSON Schema validaattori netissä: [jsonschemavalidator.net](http://www.jsonschemavalidator.net/). Voit kokeilla laittaa sinne meidän scheman ja esimerkin.

## Esimerkkidata annotoituna <small><a href="/tor/documentation/example.json">lataa JSON</a></small>

Esimerkki, johon lisätty lähes kaikki kaavaillut tietokentät.

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
          .object ul {{list-style-type: none; margin: 0; padding-left: 20px; }}
          .object div, .object li {{display: inline}}
          .object li.spacer::after {{display: block; content: ''}}
          .object {{
            font-family: monospace;
            font-size: 12px;
          }}
          .object .property {{
            position: relative;
            display: block;
          }}
          .object .description, .object koodisto {{
            font-family: arial;
          }}
          .object .description {{
            display: block;
            position: absolute;
            width: calc(100vw - 600px);
            top: 0;
            right: 0;
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
          }}
          .object .koodisto {{
            display: block;
            position: absolute;
            width: 200px;
            top: 0;
            right: calc(100vw - 600px);
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
          }}
          .object,.array {{ color: rgb(51, 51, 51); }}
          .object .key {{ color: red; }}
          .object .value {{ color: green; }}


        </style>
      </head>
      <body>
        {toXHTML( knockoff(markdown) )}
        {SchemaToJsonHtml.buildHtml(TorOppijaExamples.full, TorSchema.schemaType, TorSchema.schema)}
      </body>
    </html>
  }
}
