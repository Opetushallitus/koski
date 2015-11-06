package fi.oph.tor.schema

import java.nio.charset.StandardCharsets
import java.nio.file.{Paths, Files}
import fi.oph.tor.json.Json
import fi.oph.tor.schema.generic.{ClassType, SchemaType, ScalaJsonSchema, DescriptionAnnotation}

object GenerateSchemaAndExamples extends App {
  Json.writeFile("tiedonsiirto/example.json", TorOppijaExamples.full)
  Json.writeFile("tiedonsiirto/tor-oppija-schema.json", TorSchema.schemaJson)
  Files.write(Paths.get("tiedonsiirto/example.html"), TorTiedonSiirtoHtml.html.toString.getBytes(StandardCharsets.UTF_8))

  println("Written to files in tiedonsiirto/")
}

