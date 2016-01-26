package fi.oph.tor.schema

import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.main.{JsonSchemaFactory, JsonValidator}
import fi.oph.tor.json.Json
import org.scalatest.{FreeSpec, Matchers}

import scala.collection.JavaConversions._

class TorOppijaExamplesValidationSpec extends FreeSpec with Matchers {

  private val validator: JsonValidator = JsonSchemaFactory.byDefault.getValidator
  private val schema: JsonNode =  JsonLoader.fromString(TorSchema.schemaJsonString)

  "Validation" - {
    TorOppijaExamples.examples.foreach { example =>
      example.name in {
        val json = JsonLoader.fromString(Json.write(example.data))
        val report = validator.validate(schema, json)
        assert(report.isSuccess, "Example \"" + example.name + "\" failed to validate: \n\n" + report.filter(m => m.getLogLevel.toString == "error").mkString("\n"))
      }
    }
  }
}
