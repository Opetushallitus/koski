package fi.oph.tor.tor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.LogLevel.ERROR
import com.github.fge.jsonschema.main.JsonSchemaFactory
import fi.oph.tor.http.HttpStatus
import fi.oph.tor.schema.TorSchema
import org.json4s.JValue
import org.json4s.jackson.JsonMethods._

import scala.collection.JavaConversions._

object TorJsonSchemaValidator {
  private val schema = JsonSchemaFactory.byDefault.getJsonSchema(JsonLoader.fromString(TorSchema.schemaJsonString))
  private val mapper = new ObjectMapper().enable(INDENT_OUTPUT)

  def jsonSchemaValidate(node: JValue): HttpStatus = {
    val schemaValidationReport = schema.validate(asJsonNode(node))

    if (!schemaValidationReport.isSuccess) {
      val errors: List[JValue] = schemaValidationReport.filter(message => message.getLogLevel == ERROR)
        .map(_.asJson)
        .map(fromJsonNode)
        .toList

      HttpStatus.badRequest(errors)
    } else {
      HttpStatus.ok
    }
  }
}
