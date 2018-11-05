package fi.oph.koski.mydata

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.Hetu
import fi.oph.koski.http.{JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresLuovutuspalvelu
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Oppija
import fi.oph.koski.servlet.{ApiServlet, InvalidRequestException, NoCache}
import org.scalatra.ContentEncodingSupport


class ApiProxyServlet(implicit val application: KoskiApplication) extends ApiServlet
  with Logging with ContentEncodingSupport with NoCache with MyDataSupport with RequiresLuovutuspalvelu {

  post("/") {
    val memberId = getMemberId

    withJsonBody { parsedJson =>
      val resp = JsonSerializer.validateAndExtract[HetuRequest](parsedJson)
        .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
        .flatMap(req => Hetu.validFormat(req.hetu))
        .flatMap { hetu =>
          application.henkilöRepository.findByHetuOrCreateIfInYtrOrVirta(hetu).toRight(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia())
        }
        .flatMap { oppijaHenkilö =>
          if (application.mydataService.hasAuthorizedMember(oppijaHenkilö.oid, memberId)) {
            application.oppijaFacade.findOppijaByHetuOrCreateIfInYtrOrVirta(oppijaHenkilö.hetu.get, useVirta = true, useYtr = false).flatMap(_.warningsToLeft)
          } else {
            logger.info(s"Student ${oppijaHenkilö.oid} has not authorized $memberId to access their student data")
            Left(KoskiErrorCategory.forbidden.forbiddenXRoadHeader())
          }
        }
      renderEither[Oppija](resp)
    }()
  }

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

  private def getMemberId = {
    val memberCode = request.header("X-ROAD-MEMBER").getOrElse({
      logger.warn(s"Missing X-ROAD-MEMBER header when requesting student data")
      throw InvalidRequestException(KoskiErrorCategory.badRequest.header.missingXRoadHeader)
    })

    findMemberForMemberCode(memberCode).getOrElse(
      throw InvalidRequestException(KoskiErrorCategory.badRequest.header.invalidXRoadHeader))
      .getString("id")
  }
}

case class HetuRequest(hetu: String)
