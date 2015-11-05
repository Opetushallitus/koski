package fi.oph.tor.schema

import fi.oph.tor.json.Json

import scala.reflect.runtime.universe

object GenerateSchemaAndExamples extends App {
  val rootType = universe.typeOf[TorOppija]
  val schemaJson = ScalaJsonSchema.toJsonSchema(ScalaJsonSchema.createSchema(rootType))

  Json.writeFile("tiedonsiirto/example.json", TorOppijaExamples.full)
  Json.writeFile("tiedonsiirto/tor-oppija-schema.json", schemaJson)

  println("Written to files in tiedonsiirto/")
}

