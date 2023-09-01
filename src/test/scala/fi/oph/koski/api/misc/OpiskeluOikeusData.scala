package fi.oph.koski.api.misc

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.eero
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.schema._

trait OpiskeluoikeusData[Oikeus <: Opiskeluoikeus] {
  val defaultHenkilö: UusiHenkilö = asUusiOppija(eero)
  def defaultOpiskeluoikeus: Oikeus
}
