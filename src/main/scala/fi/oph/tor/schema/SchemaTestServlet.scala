package fi.oph.tor.schema

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper, SerializationFeature}
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.main.{JsonSchemaFactory, JsonValidator}
import fi.oph.tor.ErrorHandlingServlet

import scala.collection.JavaConversions._

class SchemaTestServlet extends ErrorHandlingServlet {

  private val validator: JsonValidator = JsonSchemaFactory.byDefault.getValidator
  private val schema: JsonNode =  JsonLoader.fromString(TorSchema.schemaJsonString)
  private val mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)

  put("/dev/null") {
    val oppija = JsonLoader.fromString(request.body)
    val report = validator.validate(schema, oppija)

    if(!report.isSuccess) {
      val errorNodes: ArrayNode = mapper.createArrayNode()
      val errorReport = mapper.createObjectNode().set("errors", errorNodes)
      report.filter(message => message.getLogLevel.toString == "error").map(_.asJson).foreach(errorNodes.add)

      halt(400, mapper.writeValueAsString(errorReport))
    }
  }
}
