package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.luovutuspalvelu.opiskeluoikeus._
import fi.oph.koski.schema._
import fi.oph.koski.schema.filter.MyDataOppija

import java.time.LocalDate

class HSLService(application: KoskiApplication) extends Logging {
  private val memberId = "hsl"

  def HSLOpiskeluoikeudet(hetu: String)(implicit user: KoskiSpecificSession): Either[HttpStatus, HslResponse] = {
    val henkilöEither = application.henkilöRepository.findByHetuOrCreateIfInYtrOrVirta(hetu).toRight(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia())

    henkilöEither.flatMap { oppijaHenkilö =>
      if (isAuthorized(oppijaHenkilö.oid)) {
        fetchOppija(hetu).map { oppija =>
          val suostumuksenPaattymispaiva = getPaattymispaiva(oppijaHenkilö.oid)

          HslResponse(
            henkilö = HslHenkilo.fromOppija(oppijaHenkilö),
            opiskeluoikeudet = oppija.opiskeluoikeudet.flatMap(oo => HslOpiskeluoikeus(oo)).toList,
            suostumuksenPaattymispaiva = suostumuksenPaattymispaiva
          )
        }
      } else {
        logUnauthorizedAccess(oppijaHenkilö.oid)
        Left(KoskiErrorCategory.forbidden.forbiddenXRoadHeader())
      }
    }
  }

  private def isAuthorized(oid: String)(implicit user: KoskiSpecificSession): Boolean = {
    application.mydataService.hasAuthorizedMember(oid, memberId)
  }

  private def fetchOppija(hetu: String)(implicit user: KoskiSpecificSession): Either[HttpStatus, Oppija] = {
    application.oppijaFacade
      .findOppijaByHetuOrCreateIfInYtrOrVirta(hetu, useVirta = true, useYtr = false)
      .flatMap(_.warningsToLeft)
  }

  private def getPaattymispaiva(oid: String)(implicit user: KoskiSpecificSession): Option[LocalDate] = {
    application.mydataService.getAll(oid)
      .find(auth => memberId == auth.asiakasId)
      .map(_.expirationDate)
  }

  private def logUnauthorizedAccess(oid: String): Unit = {
    logger.debug(s"Student $oid has not authorized $memberId to access their student data")
  }

}
