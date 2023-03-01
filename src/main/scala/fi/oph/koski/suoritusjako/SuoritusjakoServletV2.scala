package fi.oph.koski.suoritusjako


import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.{EditorApiServlet, EditorModel}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{KoskiSpecificAuthenticationSupport, KoskiSpecificSession}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema._
import fi.oph.koski.servlet.NoCache
import org.json4s.JValue

import scala.reflect.runtime.universe.TypeTag

class SuoritusjakoServletV2(implicit val application: KoskiApplication) extends EditorApiServlet with KoskiSpecificAuthenticationSupport with Logging with NoCache {
  
  post("/editor") {
    implicit val suoritusjakoUser = KoskiSpecificSession.suoritusjakoKatsominenUser(request)
    renderEither(
      extractFromBodyReturning[SuoritusjakoRequest, Either[HttpStatus, EditorModel]] {
        request => application.suoritusjakoServiceV2.findSuoritusjako(request.secret)(suoritusjakoUser)
      }
    )
  }

  get("/available") {
    requireKansalainen
    application.suoritusjakoServiceV2.listActivesByUser(user)
  }

  post("/create") {
    requireKansalainen
    extractFromBodyReturning[List[Opiskeluoikeus], HttpStatus] {
      opiskeluoikeudet => application.suoritusjakoServiceV2.createSuoritusjako(opiskeluoikeudet)(user)
    }
  }

  post("/update") {
    requireKansalainen
    extractFromBodyReturning[SuoritusjakoUpdateRequest, HttpStatus] {
      request => application.suoritusjakoServiceV2.updateExpirationDate(request.secret, request.expirationDate)(user)
    }
  }

  post("/delete") {
    requireKansalainen
    extractFromBodyReturning[SuoritusjakoDeleteRequest, HttpStatus] {
      request => application.suoritusjakoServiceV2.deleteSuoritujako(request.secret)(user)
    }
  }

  private def extractFromBodyReturning[A: TypeTag, B: TypeTag](f: A => B): B = {
    withJsonBody { body =>
      extract[A](body) match {
        case Left(status) => haltWithStatus(status)
        case Right(x) => f(x)
      }
    }()
  }

  private def extract[T: TypeTag](body: JValue) =
    application.validatingAndResolvingExtractor.extract[T](body, strictDeserialization.copy(allowEmptyStrings = true))
      .left.map(_ => KoskiErrorCategory.badRequest())

  private def user = koskiSessionOption.get
}

case class SuoritusjakoDeleteRequest(secret: String)
