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
  get("/search") {
    contentType = "application/json;charset=utf-8"
    params.get("query") match {
      case Some(query) if (query.length >= 3) =>
        val henkilöt: List[HenkilötiedotJaOid] = HenkilötiedotFacade(application.henkilöRepository, application.opiskeluoikeusRepository).findHenkilötiedot(query.toUpperCase)(koskiSession).toList
        val canAddNew = Hetu.validate(query).isRight && henkilöt.isEmpty && (koskiSession.hasGlobalWriteAccess || koskiSession.organisationOids(AccessType.write).nonEmpty)
        HenkilötiedotSearchResponse(henkilöt, canAddNew)
      case _ =>
        throw InvalidRequestException(KoskiErrorCategory.badRequest.queryParam.searchTermTooShort)
    }
  }
}

case class HenkilötiedotSearchResponse(henkilöt: List[HenkilötiedotJaOid], canAddNew: Boolean)