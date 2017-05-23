package fi.oph.koski.henkilo

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus.justStatus
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.RequiresAuthentication
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
      case Some(query) if query.length >= 3 =>
        val henkilöt = henkilötiedotFacade.findHenkilötiedot(query.toUpperCase)(koskiSession).toList
        val canAddNew = henkilöt.isEmpty && koskiSession.hasAnyWriteAccess

        if (Hetu.validFormat(query).isRight) {
          Hetu.validate(query) match {
            case Right(hetu) =>
              val canAddNew = henkilöt.isEmpty && koskiSession.hasAnyWriteAccess
              HenkilötiedotSearchResponse(henkilöt, canAddNew, hetu = Some(query))
            case Left(status) =>
              henkilöt match {
                case Nil =>
                  val error = status.errors.headOption.map(_.message.toString)
                  HenkilötiedotSearchResponse(henkilöt, false, error)
                case _ =>
                  HenkilötiedotSearchResponse(henkilöt, false)
              }
          }
        } else if (henkilöt.isEmpty && HenkilöOid.isValidHenkilöOid(query)) {
          henkilötiedotFacade.findByOid(query)(koskiSession) match {
            case Right(oppija) =>
              oppija.headOption match {
                case Some(tiedot) => HenkilötiedotSearchResponse(henkilöt, canAddNew, oid = Some(query))
                case None => HenkilötiedotSearchResponse(henkilöt, false)
              }
            case Left(_) => HenkilötiedotSearchResponse(henkilöt, false)
          }
        } else {
          HenkilötiedotSearchResponse(henkilöt, false)
        }

      case _ =>
        throw InvalidRequestException(KoskiErrorCategory.badRequest.queryParam.searchTermTooShort)
    }
  }


  get("/hetu/:hetu") {
    renderEither(henkilötiedotFacade.findByHetu(params("hetu"))(koskiSession))
  }

  get("/oid/:oid") {
    renderEither(henkilötiedotFacade.findByOid(params("oid"))(koskiSession).right.map(_.map(_.copy(hetu = None)))) // poistetaan hetu tuloksista, sillä käytössä ei ole organisaatiorajausta
  }
}

case class HenkilötiedotSearchResponse(henkilöt: List[HenkilötiedotJaOid], canAddNew: Boolean, error: Option[String] = None, hetu: Option[String] = None, oid: Option[String] = None)