package fi.oph.koski.suoritusjako


import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.{EditorApiServlet, EditorModel}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{AuthenticationSupport, KoskiSession}
import fi.oph.common.log.Logging
import fi.oph.koski.omattiedot.OmatTiedotEditorModel
import fi.oph.koski.schema.KoskiSchema.deserializationContext
import fi.oph.koski.servlet.NoCache
import fi.oph.koski.validation.ValidatingAndResolvingExtractor
import org.json4s.JValue
import scala.reflect.runtime.universe.TypeTag

class SuoritusjakoServlet(implicit val application: KoskiApplication) extends EditorApiServlet with AuthenticationSupport with Logging with NoCache {

  post("/editor") {
    implicit val koskiSession = KoskiSession.suoritusjakoKatsominenUser(request)
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
      val suoritusIds = extract[List[SuoritusIdentifier]](body)
      suoritusIds.flatMap(application.suoritusjakoService.put(user.oid, _)(user)) match {
        case Right(suoritusjako) =>
          renderObject(suoritusjako)
        case Left(status) =>
          logger.warn(s"Suoritusjaon luonti epäonnistui: oppija: ${user.oid}, suoritukset: ${suoritusIds.getOrElse(Nil).mkString}: ${status.errorString.mkString}")
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
    ValidatingAndResolvingExtractor.extract[T](body, deserializationContext.copy(allowEmptyStrings = true))
  }
}

case class SuoritusjakoRequest(secret: String)
case class SuoritusjakoUpdateRequest(secret: String, expirationDate: LocalDate)
case class SuoritusjakoUpdateResponse(expirationDate: LocalDate)
