package fi.oph.koski.omattiedot

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Oppija
import fi.oph.koski.util.WithWarnings

class HuoltajaService(application: KoskiApplication) extends Logging {
  def findUserOppijaAllowEmpty(implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, WithWarnings[Oppija]] = {
    application.oppijaFacade.findUserOppija.left.flatMap(status => application.oppijaFacade.opinnotonOppija(koskiSession.oid).toRight(status))
  }

  def findHuollettavaOppija(oid: String)(implicit koskiSession: KoskiSpecificSession): Either[HttpStatus, WithWarnings[Oppija]] = {
    application.oppijaFacade.findHuollettavaOppija(oid).left.flatMap(status => application.oppijaFacade.opinnotonOppija(oid).toRight(status))
  }
}
