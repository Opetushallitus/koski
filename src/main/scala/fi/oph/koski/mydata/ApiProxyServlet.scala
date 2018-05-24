package fi.oph.koski.mydata

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.GlobalExecutionContext
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueries
import fi.oph.koski.servlet.{ApiServlet, InvalidRequestException, NoCache}
import org.scalatra.ContentEncodingSupport


class ApiProxyServlet(implicit val application: KoskiApplication) extends ApiServlet with Logging with GlobalExecutionContext with OpiskeluoikeusQueries with ContentEncodingSupport with NoCache {

  get("/:oid") {
    def studentId = params("oid")
    request.header("X-ROAD-MEMBER") match {
      case Some(memberCode) => {
        logger.info(s"Requesting MyData content for user ${studentId} by client ${memberCode}")
        servletContext.getRequestDispatcher("/api/oppija").forward(request, response)
      }
      case None => {
        logger.warn(s"Missing X-ROAD-MEMBER header when requesting student data for ${studentId}")
        throw InvalidRequestException(KoskiErrorCategory.badRequest.header.missingXRoadHeader)
      }
    }
  }

}
