package fi.oph.tor.tor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.databind.node.ArrayNode
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.LogLevel.ERROR
import com.github.fge.jsonschema.main.JsonSchemaFactory
import fi.oph.tor.schema.TorSchema

import scala.collection.JavaConversions._

object JsonSchemaValidator {
  private val schema = JsonSchemaFactory.byDefault.getJsonSchema(JsonLoader.fromString(TorSchema.schemaJsonString))
  private val mapper = new ObjectMapper().enable(INDENT_OUTPUT)

  def jsonSchemaValidate(jsonString: String): Option[String] = {
    val schemaValidationReport = schema.validate(JsonLoader.fromString(jsonString))

    if (!schemaValidationReport.isSuccess) {
      val errorNodes: ArrayNode = mapper.createArrayNode()
      schemaValidationReport.filter(message => message.getLogLevel == ERROR).map(_.asJson).foreach(errorNodes.add)

      Some(mapper.writeValueAsString(mapper.createObjectNode().set("errors", errorNodes)))
    } else {
      None
    }
  }
}
