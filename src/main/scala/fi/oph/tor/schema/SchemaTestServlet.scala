package fi.oph.tor.schema

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.databind.node.ArrayNode
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.LogLevel.ERROR
import com.github.fge.jsonschema.main.JsonSchemaFactory
import fi.oph.tor.ErrorHandlingServlet

import scala.collection.JavaConversions._

class SchemaTestServlet extends ErrorHandlingServlet {

  private val schema = JsonSchemaFactory.byDefault.getJsonSchema(JsonLoader.fromString(TorSchema.schemaJsonString))
  private val mapper = new ObjectMapper().enable(INDENT_OUTPUT)

  put("/dev/null") {
    val report = schema.validate(JsonLoader.fromString(request.body))

    if(!report.isSuccess) {
      val errorNodes: ArrayNode = mapper.createArrayNode()
      report.filter(message => message.getLogLevel == ERROR).map(_.asJson).foreach(errorNodes.add)

      halt(400, mapper.writeValueAsString(mapper.createObjectNode().set("errors", errorNodes)))
    }
  }
}
