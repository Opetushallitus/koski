package fi.oph.tor.oppija

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.databind.node.ArrayNode
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.LogLevel.ERROR
import com.github.fge.jsonschema.main.JsonSchemaFactory
import fi.oph.tor.{ErrorHandlingServlet, InvalidRequestException}
import fi.oph.tor.json.Json
import fi.oph.tor.schema.{TorOppija, TorSchema}
import fi.oph.tor.security.RequiresAuthentication
import fi.oph.tor.tor.TodennetunOsaamisenRekisteri
import fi.oph.tor.user.UserRepository
import fi.vm.sade.security.ldap.DirectoryClient
import fi.vm.sade.utils.slf4j.Logging

import scala.collection.JavaConversions._

class OppijaServlet(rekisteri: TodennetunOsaamisenRekisteri, val userRepository: UserRepository, val directoryClient: DirectoryClient) extends ErrorHandlingServlet with Logging with RequiresAuthentication {

  private val schema = JsonSchemaFactory.byDefault.getJsonSchema(JsonLoader.fromString(TorSchema.schemaJsonString))
  private val mapper = new ObjectMapper().enable(INDENT_OUTPUT)

  put("/") {
    jsonSchemaValidate

    val oppija: TorOppija = Json.read[TorOppija](request.body)

    getClass.synchronized{
      renderEither(rekisteri.createOrUpdate(oppija))
    }
  }

  get("/") {
    contentType = "application/json;charset=utf-8"
    params.get("query") match {
      case Some(query) if (query.length >= 3) =>
        Json.write(rekisteri.findOppijat(query))
      case _ => throw new InvalidRequestException("query parameter length must be at least 3")
    }
  }

  get("/:oid") {
    renderEither(rekisteri.userView(params("oid")))
  }

  private def jsonSchemaValidate: Unit = this.synchronized {
    val schemaValidationReport = schema.validate(JsonLoader.fromString(request.body))

    if (!schemaValidationReport.isSuccess) {
      val errorNodes: ArrayNode = mapper.createArrayNode()
      schemaValidationReport.filter(message => message.getLogLevel == ERROR).map(_.asJson).foreach(errorNodes.add)

      halt(400, mapper.writeValueAsString(mapper.createObjectNode().set("errors", errorNodes)))
    }
  }
}
