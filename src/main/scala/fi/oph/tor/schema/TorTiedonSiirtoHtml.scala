package fi.oph.tor.schema

object TorTiedonSiirtoHtml {
  def generateHtml = {
    <html>
      <head>
        <meta charset="UTF-8"></meta>
        <style>
          ul {{list-style-type: none; margin: 0; padding-left: 20px}}
          div, li {{display: inline}}
          li.spacer::after {{display: block; content: ''}}
          .object {{
          font-family: monospace;
          font-size: 12px;
          }}
          .property {{
          position: relative;
          display: block;
          }}
          .description,.koodisto {{
          font-family: arial;
          }}
          .description {{
          display: block;
          position: absolute;
          width: calc(100vw - 600px);
          top: 0;
          right: 0;
          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
          }}
          .koodisto {{
          display: block;
          position: absolute;
          width: 200px;
          top: 0;
          right: calc(100vw - 600px);
          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
          }}
          .object,.array {{ color: black }}
          .key {{ color: red }}
          .value {{ color: green }}


        </style>
      </head>
      <body>
        {SchemaToJsonHtml.buildHtml(TorOppijaExamples.full, TorSchema.schemaType, TorSchema.schema)}
      </body>
    </html>
  }
}
