package fi.oph.tor.tor

import java.time.LocalDate

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.databind.node.ArrayNode
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.LogLevel.ERROR
import com.github.fge.jsonschema.main.JsonSchemaFactory
import fi.oph.tor.henkilo.HenkiloOid
import fi.oph.tor.http.HttpStatus
import fi.oph.tor.json.Json
import fi.oph.tor.koodisto.KoodistoPalvelu
import fi.oph.tor.organisaatio.OrganisaatioRepository
import fi.oph.tor.schema.{Henkilö, TorOppija, TorSchema}
import fi.oph.tor.toruser.{RequiresAuthentication, UserOrganisationsRepository}
import fi.oph.tor.{ErrorHandlingServlet, InvalidRequestException}
import fi.vm.sade.security.ldap.DirectoryClient
import fi.vm.sade.utils.slf4j.Logging

import scala.collection.JavaConversions._

class TorServlet(rekisteri: TodennetunOsaamisenRekisteri, val userRepository: UserOrganisationsRepository, val directoryClient: DirectoryClient, val koodistoPalvelu: KoodistoPalvelu, val organisaatioRepository: OrganisaatioRepository) extends ErrorHandlingServlet with Logging with RequiresAuthentication {

  private val schema = JsonSchemaFactory.byDefault.getJsonSchema(JsonLoader.fromString(TorSchema.schemaJsonString))
  private val mapper = new ObjectMapper().enable(INDENT_OUTPUT)

  put("/") {
    withJsonBody { parsedJson =>
      jsonSchemaValidate // TODO: this actually causes a double parse
      implicit val koodistoPalvelu = this.koodistoPalvelu
      val extractionResult: Either[HttpStatus, TorOppija] = ValidatingAndResolvingExtractor.extract[TorOppija](parsedJson, ValidationAndResolvingContext(koodistoPalvelu, organisaatioRepository))
      val result: Either[HttpStatus, Henkilö.Oid] = extractionResult.right.flatMap (rekisteri.createOrUpdate _)

      result.left.foreach { case HttpStatus(code, errors) =>
        logger.warn("Opinto-oikeuden päivitys estetty: " + code + " " + errors + " for request " + describeRequest)
      }
      renderEither(result)
    }
  }

  get("/") {
    contentType = "application/json;charset=utf-8"

    val filters = params.toList.map {
      case ("valmistunutViimeistaan", päivä) => ValmistunutViimeistään(LocalDate.parse(päivä))
      case ("valmistunutAikaisintaan", päivä) => ValmistunutAikaisintaan(LocalDate.parse(päivä))
      case (param,_) => throw InvalidRequestException("Unknown query parameter: " + param)
    }

    Json.write(rekisteri.findOppijat(filters))
  }

  get("/:oid") {
    renderEither(HenkiloOid.validateHenkilöOid(params("oid")).right.flatMap { oid =>
      rekisteri.findTorOppija(oid)
    })
  }

  get("/search") {
    contentType = "application/json;charset=utf-8"
    params.get("query") match {
      case Some(query) if (query.length >= 3) =>
        Json.write(rekisteri.findOppijat(query.toUpperCase))
      case _ =>
        throw new InvalidRequestException("query parameter length must be at least 3")
    }
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

trait QueryFilter

case class ValmistunutAikaisintaan(päivä: LocalDate) extends QueryFilter
case class ValmistunutViimeistään(päivä: LocalDate) extends QueryFilter
