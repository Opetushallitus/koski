package fi.oph.koski.mydata

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.Hetu
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresHSL
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.TäydellisetHenkilötiedot
import fi.oph.koski.schema.filter.MyDataOppija
import fi.oph.koski.servlet.{InvalidRequestException, KoskiSpecificApiServlet, NoCache}
import org.scalatra.ContentEncodingSupport


class ApiProxyServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet
                                                                          with Logging with ContentEncodingSupport with NoCache with MyDataSupport with RequiresHSL {

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

            application.oppijaFacade.findOppijaByHetuOrCreateIfInYtrOrVirta(oppijaHenkilö.hetu.get, useVirta = true, useYtr = false).flatMap(_.warningsToLeft)
              .map(oppija => {

              val paattymisPaiva = application.mydataService.getAll(oppijaHenkilö.oid).find(auth => memberId == auth.asiakasId)
                .map(item => item.expirationDate)

              MyDataOppija(
                henkilö = oppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot],
                opiskeluoikeudet = oppija.opiskeluoikeudet,
                suostumuksenPaattymispaiva = paattymisPaiva
              )
            })
          } else {
            //Aiheuttaa merkittävän määrän turhaa logitusta HSL:n hakiessa näitä tietoja jatkuvasti -> debug
            logger.debug(s"Student ${oppijaHenkilö.oid} has not authorized $memberId to access their student data")
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
}

case class HetuRequest(hetu: String)
