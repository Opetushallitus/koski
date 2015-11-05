package fi.oph.tor.schema

import fi.oph.tor.json.Json

import scala.reflect.runtime.universe

object GenerateSchemaAndExamples extends App {
  val rootType = universe.typeOf[TorOppija]
  val schema = new ScalaJsonSchema(Description)
  val schemaJson = schema.toJsonSchema(schema.createSchema(rootType))

  Json.writeFile("tiedonsiirto/example.json", TorOppijaExamples.full)
  Json.writeFile("tiedonsiirto/tor-oppija-schema.json", schemaJson)

  println("Written to files in tiedonsiirto/")
}

