package fi.oph.koski.hsl

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.luovutuspalvelu.HslResponse
import fi.oph.koski.luovutuspalvelu.opiskeluoikeus.HslOpiskeluoikeus
import fi.oph.koski.schema.Oppija

class HslOmaDataOAuth2Service(application: KoskiApplication) extends Logging {

  def findOpiskeluoikeudet(oppijaOid: String)(implicit session: KoskiSpecificSession): Either[HttpStatus, List[HslOpiskeluoikeus]] = {
    application.oppijaFacade
      .findOppija(oppijaOid)
      .flatMap(_.warningsToLeft)
      .map { case Oppija(_, opiskeluoikeudet) =>
        opiskeluoikeudet
          .filter(oo => HslResponse.schemassaTuetutOpiskeluoikeustyypit.contains(oo.tyyppi.koodiarvo))
          .flatMap(HslOpiskeluoikeus(_))
          .toList
      }
  }
}
