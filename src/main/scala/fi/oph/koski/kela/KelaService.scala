package fi.oph.koski.kela

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSession

class KelaService(application: KoskiApplication) {
  def findKelaOppijaByHetu(hetu: String)(implicit koskiSession: KoskiSession): Either[HttpStatus, KelaOppija] = {
    //Kela ei ole kiinnostunut tässä tapauksessa korkeakoulujen opiskeluoikeuksista
    application.oppijaFacade.findOppijaByHetuOrCreateIfInYtrOrVirta(hetu, useVirta = false, useYtr = true)
      .flatMap(_.warningsToLeft)
      .flatMap(KelaOppijaConverter.convertOppijaToKelaOppija)
  }
}
