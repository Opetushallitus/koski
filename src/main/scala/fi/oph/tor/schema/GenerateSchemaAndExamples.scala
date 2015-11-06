package fi.oph.tor.schema

import java.nio.charset.StandardCharsets
import java.nio.file.{Paths, Files}
import fi.oph.tor.json.Json
import fi.oph.tor.schema.generic.{ClassType, SchemaType, ScalaJsonSchema, DescriptionAnnotation}
import scala.reflect.runtime.universe

object GenerateSchemaAndExamples extends App {
  Json.writeFile("tiedonsiirto/example.json", TorOppijaExamples.full)
  Json.writeFile("tiedonsiirto/tor-oppija-schema.json", TorSchema.schemaJson)
  Files.write(Paths.get("tiedonsiirto/example.html"), generateHtml.toString.getBytes(StandardCharsets.UTF_8))

  println("Written to files in tiedonsiirto/")

  def generateHtml = {
    <html>
      <head>
        <meta charset="UTF-8"></meta>
        <style>
          ul {{list-style-type: none; margin: 0; padding-left: 20px}}
          div, li {{display: inline}}
          li.spacer::after {{display: block; content: ''}}
          .description {{ background: #ffffaa }}
          .koodisto {{ background: #aaffff }}
        </style>
      </head>
      <body>
        {SchemaToJsonHtml.buildHtml(TorOppijaExamples.full, TorSchema.schemaType, TorSchema.schema)}
      </body>
    </html>
  }
}

object TorSchema {
  val rootType = universe.typeOf[TorOppija]
  val schema = new ScalaJsonSchema(DescriptionAnnotation, KoodistoAnnotation)
  val schemaType: SchemaType = schema.createSchema(rootType)
  val schemaJson = schema.toJsonSchema(schemaType)

}