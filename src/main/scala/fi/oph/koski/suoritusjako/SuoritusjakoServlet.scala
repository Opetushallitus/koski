package fi.oph.koski.suoritusjako


import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.{EditorApiServlet, EditorModel}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiSpecificAuthenticationSupport, KoskiSpecificSession}
import fi.oph.koski.log.Logging
import fi.oph.koski.omattiedot.OmatTiedotEditorModel
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.servlet.NoCache
import org.json4s.JValue

import java.time.LocalDate
import scala.reflect.runtime.universe.TypeTag

class SuoritusjakoServlet(implicit val application: KoskiApplication) extends EditorApiServlet with KoskiSpecificAuthenticationSupport with Logging with NoCache {

  post("/editor") {
    implicit val koskiSession = KoskiSpecificSession.suoritusjakoKatsominenUser(request)
    withJsonBody({ body =>
      val request = JsonSerializer.extract[SuoritusjakoRequest](body)
      renderEither[EditorModel](
        SuoritusjakoSecret.validate(request.secret)
          .flatMap(secret => application.suoritusjakoService.get(secret)(koskiSession))
          .map(oppija => OmatTiedotEditorModel.toEditorModel(userOppija = oppija, näytettäväOppija = oppija)(application, koskiSession))
      )
    })()
  }

  post("/") {
    requireKansalainen
    withJsonBody({ body =>
      var errors: List[HttpStatus] = List()
      def rightOrEmptyList[T](x: Either[HttpStatus, List[T]]) = x
        match {
          case Right(f) => f
          case Left(e) =>
            errors = errors :+ e
            List()
        }

      val suoritusIds = rightOrEmptyList(extract[List[SuoritusIdentifier]](body))
      val kokonaisuudet = rightOrEmptyList(extract[List[SuoritusjakoPayload]](body))

      logger.info(suoritusIds.toString())
      logger.info(kokonaisuudet.toString())

      val result = if (suoritusIds.nonEmpty) {
        application.suoritusjakoService.putBySuoritusIds(user.oid, suoritusIds)(user)
      } else if (kokonaisuudet.nonEmpty) {
        application.suoritusjakoService.putByKokonaisuudet(user.oid, kokonaisuudet)(user)
      } else {
        Left(HttpStatus.fold(errors))
      }

      result match {
        case Right(suoritusjako) =>
          renderObject(suoritusjako)
        case Left(status) =>
          logger.info(status.errors.toString)
          logger.error(s"Suoritusjaon luonti epäonnistui: oppija: ${user.oid}, suoritukset: ${suoritusIds.mkString}, kokonaisuudet ${kokonaisuudet.mkString}: ${status.errorString.mkString}")
          renderStatus(status)
      }
    })()
  }

  post("/delete") {
    requireKansalainen
    withJsonBody({ body =>
      val request = JsonSerializer.extract[SuoritusjakoRequest](body)
      renderStatus(application.suoritusjakoService.delete(user.oid, request.secret))
    })()
  }

  post("/update") {
    requireKansalainen
    withJsonBody({ body =>
      val request = JsonSerializer.extract[SuoritusjakoUpdateRequest](body)
      val expirationDate = request.expirationDate
      application.suoritusjakoService.update(user.oid, request.secret, expirationDate) match {
        case status if status.isOk =>
          renderObject(SuoritusjakoUpdateResponse(expirationDate))
        case status =>
          renderStatus(status)
      }
    })()
  }

  get("/") {
    requireKansalainen
    renderObject(application.suoritusjakoService.getAll(user.oid))
  }

  private def user = koskiSessionOption.get

  private def extract[T: TypeTag](body: JValue) = {
    application.validatingAndResolvingExtractor.extract[T](body, strictDeserialization.copy(allowEmptyStrings = true))
  }
}

case class SuoritusjakoRequest(secret: String)
case class SuoritusjakoUpdateRequest(secret: String, expirationDate: LocalDate)
case class SuoritusjakoUpdateResponse(expirationDate: LocalDate)
