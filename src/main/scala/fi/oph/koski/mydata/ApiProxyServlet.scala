package fi.oph.koski.mydata

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.Hetu
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresLuovutuspalvelu
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Oppija
import fi.oph.koski.schema.filter.MyDataOppija
import fi.oph.koski.servlet.{ApiServlet, InvalidRequestException, NoCache}
import org.scalatra.ContentEncodingSupport


class ApiProxyServlet(implicit val application: KoskiApplication) extends ApiServlet
  with Logging with ContentEncodingSupport with NoCache with MyDataSupport with RequiresLuovutuspalvelu {

  post("/") {
    val memberId = getMemberId

    withJsonBody { parsedJson =>
      val resp: Either[HttpStatus, MyDataOppija] = JsonSerializer.validateAndExtract[HetuRequest](parsedJson)
        .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
        .flatMap(req => Hetu.validFormat(req.hetu))
        .flatMap { hetu =>
          application.henkilöRepository.findByHetuOrCreateIfInYtrOrVirta(hetu).toRight(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia())
        }
        .flatMap { oppijaHenkilö =>
          if (application.mydataService.hasAuthorizedMember(oppijaHenkilö.oid, memberId)) {
            toMyDataOppijaResponse(
              oppijaHenkilö.oid,
              memberId,
              application.oppijaFacade.findOppijaByHetuOrCreateIfInYtrOrVirta(oppijaHenkilö.hetu.get, useVirta = true, useYtr = false).flatMap(_.warningsToLeft)
            )
          } else {
            logger.info(s"Student ${oppijaHenkilö.oid} has not authorized $memberId to access their student data")
            Left(KoskiErrorCategory.forbidden.forbiddenXRoadHeader())
          }
        }

      renderEither[MyDataOppija](resp)
    }()
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

  private def toMyDataOppijaResponse(oid: String, memberId: String, value: Either[HttpStatus, Oppija]): Either[HttpStatus, MyDataOppija] = {
    value match {
      case Left(s) => Left(s)
      case Right(oppija) => {

        val paattymisPaiva = application.mydataService.getAll(oid).find(auth => memberId == auth.asiakasId)
          .map(item => item.expirationDate)

        Right(MyDataOppija(
          henkilö = oppija.henkilö,
          opiskeluoikeudet = oppija.opiskeluoikeudet,
          suostumuksenPaattymispaiva = paattymisPaiva
        ))
      }
    }
  }
}

case class HetuRequest(hetu: String)
