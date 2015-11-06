package fi.oph.tor.schema

import fi.oph.tor.json.Json
import fi.oph.tor.schema.generic.{ScalaJsonSchema, DescriptionAnnotation}
import scala.reflect.runtime.universe

object GenerateSchemaAndExamples extends App {
  val rootType = universe.typeOf[TorOppija]
  val schema = new ScalaJsonSchema(DescriptionAnnotation, KoodistoAnnotation)
  val schemaJson = schema.toJsonSchema(schema.createSchema(rootType))

  Json.writeFile("tiedonsiirto/example.json", TorOppijaExamples.full)
  Json.writeFile("tiedonsiirto/tor-oppija-schema.json", schemaJson)

  println("Written to files in tiedonsiirto/")
}