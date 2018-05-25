package fi.oph.koski.mydata

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.GlobalExecutionContext
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueries
import fi.oph.koski.servlet.{ApiServlet, InvalidRequestException, NoCache}
import org.scalatra.ContentEncodingSupport
import scala.collection.JavaConverters._


class ApiProxyServlet(implicit val application: KoskiApplication) extends ApiServlet with Logging with GlobalExecutionContext with OpiskeluoikeusQueries with ContentEncodingSupport with NoCache {

  get("/:oid") {
    def studentId = params("oid")
    request.header("X-ROAD-MEMBER") match {
      case Some(memberCode) => {
        logger.info(s"Requesting MyData content for user ${studentId} by client ${memberCode}")

        def member = application.config.getConfigList("mydata.members").asScala.find(member =>
          member.getStringList("membercodes").contains(memberCode)
        )

        member match {
          case Some(memberConf) => {
            def memberId = memberConf.getString("id")
            def hasAuthorized = application.mydataService.hasAuthorizedMember(studentId, memberId)
            if (hasAuthorized) {
              logger.info(s"Student ${studentId} has authorized ${memberId} to access their student data")
              servletContext.getRequestDispatcher("/api/oppija").forward(request, response)
            } else {
              logger.warn(s"Student ${studentId} has not authorized ${memberId} to access their student data")
              throw InvalidRequestException(KoskiErrorCategory.badRequest.header.unauthorizedXRoadHeader)
            }
          }
          case None => {
            logger.warn(s"Unknown X-ROAD-MEMBER ${memberCode} when requesting student data for ${studentId}")
            throw InvalidRequestException(KoskiErrorCategory.badRequest.header.invalidXRoadHeader)
          }
        }
      }
      case None => {
        logger.warn(s"Missing X-ROAD-MEMBER header when requesting student data for ${studentId}")
        throw InvalidRequestException(KoskiErrorCategory.badRequest.header.missingXRoadHeader)
      }
    }
  }

}
