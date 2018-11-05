package fi.oph.koski.mydata

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.GlobalExecutionContext
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueries
import fi.oph.koski.servlet.{ApiServlet, InvalidRequestException, NoCache}
import org.scalatra.ContentEncodingSupport


class ApiProxyServlet(implicit val application: KoskiApplication) extends ApiServlet
  with Logging with GlobalExecutionContext with OpiskeluoikeusQueries with ContentEncodingSupport with NoCache with MyDataSupport {

  get("/:oid") {
    val studentId = params("oid")
    val memberCode = request.header("X-ROAD-MEMBER").getOrElse({
      logger.warn(s"Missing X-ROAD-MEMBER header when requesting student data for ${studentId}")
      throw InvalidRequestException(KoskiErrorCategory.badRequest.header.missingXRoadHeader)
    })

    proxyRequestDispatcher(studentId, memberCode)
  }

  private def proxyRequestDispatcher(studentId: String, memberCode: String) = {
    logger.info(s"Requesting MyData content for user ${studentId} by client ${memberCode}")

    val memberId = findMemberForMemberCode(memberCode).getOrElse(
      throw InvalidRequestException(KoskiErrorCategory.badRequest.header.invalidXRoadHeader))
      .getString("id")

    if (application.mydataService.hasAuthorizedMember(studentId, memberId)) {
      logger.info(s"Student ${studentId} has authorized ${memberId} to access their student data")
      servletContext.getRequestDispatcher("/api/oppija").forward(request, response)
    } else {
      logger.warn(s"Student ${studentId} has not authorized ${memberId} to access their student data")
      throw InvalidRequestException(KoskiErrorCategory.forbidden.forbiddenXRoadHeader)
    }
  }
}
