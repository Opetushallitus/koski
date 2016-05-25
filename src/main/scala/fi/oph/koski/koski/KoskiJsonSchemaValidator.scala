package fi.oph.koski.koski

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.LogLevel.ERROR
import com.github.fge.jsonschema.main.JsonSchemaFactory
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema.KoskiSchema
import org.json4s.JValue
import org.json4s.jackson.JsonMethods._

import scala.collection.JavaConversions._

object KoskiJsonSchemaValidator {
  private val schema = JsonSchemaFactory.byDefault.getJsonSchema(JsonLoader.fromString(KoskiSchema.schemaJsonString))
  private val mapper = new ObjectMapper().enable(INDENT_OUTPUT)

  def jsonSchemaValidate(node: JValue): HttpStatus = {
    val schemaValidationReport = schema.validate(asJsonNode(node))

    if (!schemaValidationReport.isSuccess) {
      val errors: List[JValue] = schemaValidationReport.filter(message => message.getLogLevel == ERROR)
        .map(_.asJson)
        .map(fromJsonNode)
        .toList

      HttpStatus.fold(errors.map((error: JValue) => KoskiErrorCategory.badRequest.validation.jsonSchema.apply(error)))
    } else {
      HttpStatus.ok
    }
  }
}
