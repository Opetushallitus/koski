package fi.oph.koski.etk

import java.time.LocalDate
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.OpintopolkuHenkilöRepository
import fi.oph.koski.http.{JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods
import org.scalatra.servlet.FileUploadSupport

import scala.io.{BufferedSource, Source}
import scala.collection.JavaConverters._

class ElaketurvakeskusServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with FileUploadSupport with RequiresVirkailijaOrPalvelukäyttäjä with Logging with NoCache {

  val elaketurvakeskusService = new ElaketurvakeskusService(application)
  val oppijanumerorekisteri: OpintopolkuHenkilöRepository = application.henkilöRepository.opintopolku

  before() {
    callsOnlyFrom(application.config.getStringList("elaketurvakeskus.kutsutSallittuOsoitteesta").asScala.toList)
  }

  post("/") {
    renderEither(request.contentType match {
      case Some(contentType) if isMultipart(contentType) => {
        val json = parseJson
        val file = parseFile

        elaketurvakeskusService.tutkintotiedot(json, file, oppijanumerorekisteri) match {
          case Some(response) => Right(response)
          case None => Left(KoskiErrorCategory.notFound.suoritustaEiLöydy(""))
        }
      }
      case wrongType => Left(KoskiErrorCategory.badRequest(s"$wrongType"))
    })
  }

  private def isMultipart(string: String) =
    string.split(";")(0) == "multipart/form-data"

  private def parseFile: Option[BufferedSource] = fileMultiParams.get("csv")
    .map(_.head)
    .map(file => Source.fromInputStream(file.getInputStream))

  private def parseJson: Option[TutkintotietoRequest] = params.get("json")
    .map(JsonMethods.parse(_))
    .map(parseTutkintotieto)
    .map {
      case Right(tutkintotieto) => tutkintotieto
      case Left(status) => haltWithStatus(status)
    }

  private def parseTutkintotieto(parsedJson: JValue) =
    JsonSerializer.validateAndExtract[TutkintotietoRequest](parsedJson)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
}

case class TutkintotietoRequest(alku: LocalDate, loppu: LocalDate, vuosi: Int)
