package fi.oph.koski.suoritusjako

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.EditorModelSerializer
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.{JsonSerializer, LegacyJsonSerialization}
import fi.oph.koski.koskiuser.{AuthenticationSupport, KoskiSession}
import fi.oph.koski.log.Logging
import fi.oph.koski.omattiedot.OmatTiedotEditorModel
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import org.json4s.jackson.Serialization

import scala.util.Try

class SuoritusjakoServlet(implicit val application: KoskiApplication) extends ApiServlet with AuthenticationSupport with Logging with NoCache {

  post("/editor") {
    val koskiSession = KoskiSession.suoritusjakoKatsominenUser(request)
    withJsonBody({ body =>
      val request = JsonSerializer.extract[SuoritusjakoRequest](body)
      renderEither(
        application.suoritusjakoService.validateSuoritusjakoSecret(request.secret)
          .flatMap(secret => application.suoritusjakoService.get(secret)(koskiSession))
          .map(oppija => OmatTiedotEditorModel.toEditorModel(oppija)(application, koskiSession))
      )
    })()
  }

  put("/") {
    requireKansalainen
    withJsonBody({ body =>
      Try(JsonSerializer.extract[List[SuoritusIdentifier]](body)).toOption match {
        case Some(suoritusIds) =>
          renderEither(application.suoritusjakoService.put(koskiSessionOption.get.oid, suoritusIds)(koskiSessionOption.get))
        case None =>
          haltWithStatus(KoskiErrorCategory.badRequest.format())
      }
    })()
  }

  post("/delete") {
    requireKansalainen
    withJsonBody({ body =>
      val request = JsonSerializer.extract[SuoritusjakoRequest](body)
      render(application.suoritusjakoService.delete(koskiSessionOption.get.oid, request.secret))
    })()
  }

  get("/") {
    requireKansalainen
    render(application.suoritusjakoService.getAll(koskiSessionOption.get.oid))
  }

  import reflect.runtime.universe.TypeTag
  override def toJsonString[T: TypeTag](x: T): String = Serialization.write(x.asInstanceOf[AnyRef])(LegacyJsonSerialization.jsonFormats + EditorModelSerializer)
}

case class SuoritusjakoRequest(secret: String)
