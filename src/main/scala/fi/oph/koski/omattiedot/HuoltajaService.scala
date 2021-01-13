package fi.oph.koski.omattiedot

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.common.koskiuser.KoskiSession
import fi.oph.common.log.Logging
import fi.oph.koski.schema.Oppija
import fi.oph.koski.util.WithWarnings

class HuoltajaService(application: KoskiApplication) extends Logging {
  def findUserOppijaAllowEmpty(implicit koskiSession: KoskiSession): Either[HttpStatus, WithWarnings[Oppija]] = {
    application.oppijaFacade.findUserOppija.left.flatMap(status => opinnotonOppija(koskiSession.oid).toRight(status))
  }

  def findHuollettavaOppija(oid: String)(implicit koskiSession: KoskiSession): Either[HttpStatus, WithWarnings[Oppija]] = {
    application.oppijaFacade.findHuollettavaOppija(oid).left.flatMap(status => opinnotonOppija(oid).toRight(status))
  }

  private def opinnotonOppija(oid: String) =
    application.henkilöRepository.findByOid(oid)
      .map(application.henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot)
      .map(Oppija(_, Nil))
      .map(WithWarnings(_, Nil))
}
