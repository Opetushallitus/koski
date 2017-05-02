package fi.oph.koski.henkilo

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{AccessType, RequiresAuthentication}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.HenkilötiedotJaOid
import fi.oph.koski.servlet.{ApiServlet, InvalidRequestException, NoCache}
import fi.oph.koski.util.Timing
import org.scalatra._

class HenkilötiedotServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with Logging with GZipSupport with NoCache with Timing {
  private val henkilötiedotFacade = HenkilötiedotFacade(application.henkilöRepository, application.opiskeluoikeusRepository)

  get("/search") {
    contentType = "application/json;charset=utf-8"

    params.get("query") match {
      case Some(query) if (query.length >= 3) =>
        val henkilöt = henkilötiedotFacade.findHenkilötiedot(query.toUpperCase)(koskiSession).toList
        if (Hetu.validFormat(query).isRight) {
          Hetu.validate(query) match {
            case Right(hetu) =>
              val canAddNew = henkilöt.isEmpty && koskiSession.hasAnyWriteAccess
              HenkilötiedotSearchResponse(henkilöt, canAddNew, None)
            case Left(status) =>
              henkilöt match {
                case Nil =>
                  val error = status.errors.headOption.map(_.message.toString)
                  HenkilötiedotSearchResponse(henkilöt, false, error)
                case _ =>
                  HenkilötiedotSearchResponse(henkilöt, false, None)
              }
          }
        } else {
          HenkilötiedotSearchResponse(henkilöt, false, None)
        }
      case _ =>
        throw InvalidRequestException(KoskiErrorCategory.badRequest.queryParam.searchTermTooShort)
    }
  }
  get("/hetu/:hetu") {
    renderEither(henkilötiedotFacade.findByHetu(params("hetu"))(koskiSession))
  }
}

case class HenkilötiedotSearchResponse(henkilöt: List[HenkilötiedotJaOid], canAddNew: Boolean, error: Option[String])