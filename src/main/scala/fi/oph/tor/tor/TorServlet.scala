package fi.oph.tor.tor

import java.time.LocalDate
import java.time.format.DateTimeParseException

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.databind.node.ArrayNode
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.LogLevel.ERROR
import com.github.fge.jsonschema.main.JsonSchemaFactory
import fi.oph.tor.db.GlobalExecutionContext
import fi.oph.tor.henkilo.HenkiloOid
import fi.oph.tor.http.HttpStatus
import fi.oph.tor.json.{Json, JsonStreamWriter}
import fi.oph.tor.koodisto.KoodistoPalvelu
import fi.oph.tor.organisaatio.OrganisaatioRepository
import fi.oph.tor.schema.{Henkilö, TorOppija, TorSchema}
import fi.oph.tor.toruser.{RequiresAuthentication, UserOrganisationsRepository}
import fi.oph.tor.{ErrorHandlingServlet, InvalidRequestException}
import fi.vm.sade.security.ldap.DirectoryClient
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra.{GZipSupport, FutureSupport}

import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration

class TorServlet(rekisteri: TodennetunOsaamisenRekisteri, val userRepository: UserOrganisationsRepository, val directoryClient: DirectoryClient, val koodistoPalvelu: KoodistoPalvelu, val organisaatioRepository: OrganisaatioRepository)
  extends ErrorHandlingServlet with Logging with RequiresAuthentication with GlobalExecutionContext with FutureSupport with GZipSupport {

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
    logger.info("Haetaan opiskeluoikeuksia: " + request.getQueryString)

    val queryFilters: List[Either[HttpStatus, QueryFilter]] = params.toList.map {
      case (p, v) if p == "valmistunutAikaisintaan" => dateParam((p, v)).right.map(ValmistunutAikaisintaan(_))
      case (p, v) if p == "valmistunutViimeistaan" => dateParam((p, v)).right.map(ValmistunutViimeistään(_))
      case ("tutkinnonTila", v) => Right(TutkinnonTila(v))
      case (p, _) => Left(HttpStatus.badRequest("Unsupported query parameter: " + p))
    }

    queryFilters.partition(_.isLeft) match {
      case (Nil, queries) =>
        val filters = queries.map(_.right.get)
        val oppijat = rekisteri.findOppijat(filters)
        JsonStreamWriter.writeJsonStream(oppijat)(this, Json.jsonFormats)
      case (errors, _) =>
        renderStatus(HttpStatus.fold(errors.map(_.left.get)))
    }
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

  override protected def asyncTimeout = Duration.Inf

  private def jsonSchemaValidate: Unit = this.synchronized {
    val schemaValidationReport = schema.validate(JsonLoader.fromString(request.body))

    if (!schemaValidationReport.isSuccess) {
      val errorNodes: ArrayNode = mapper.createArrayNode()
      schemaValidationReport.filter(message => message.getLogLevel == ERROR).map(_.asJson).foreach(errorNodes.add)

      halt(400, mapper.writeValueAsString(mapper.createObjectNode().set("errors", errorNodes)))
    }
  }

  def dateParam(q: (String, String)): Either[HttpStatus, LocalDate] = q match {
    case (p, v) => try {
      Right(LocalDate.parse(v))
    } catch {
      case e: DateTimeParseException => Left(HttpStatus.badRequest("Invalid date parameter: " + p + "=" + v))
    }
  }
}

trait QueryFilter

case class ValmistunutAikaisintaan(päivä: LocalDate) extends QueryFilter
case class ValmistunutViimeistään(päivä: LocalDate) extends QueryFilter
case class TutkinnonTila(tila: String) extends QueryFilter
