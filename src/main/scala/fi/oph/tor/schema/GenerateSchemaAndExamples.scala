package fi.oph.tor.schema

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import fi.oph.tor.json.Json

object GenerateSchemaAndExamples extends App {
  Json.writeFile("tiedonsiirto/example.json", TorOppijaExamples.perustutkintoNäyttönä)
  Json.writeFile("tiedonsiirto/tor-oppija-schema.json", TorSchema.schemaJson)
  Files.write(Paths.get("tiedonsiirto/example.html"), TorTiedonSiirtoHtml.html.toString.getBytes(StandardCharsets.UTF_8))

  println("Written to files in tiedonsiirto/")
}

