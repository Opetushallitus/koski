package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.schema._

trait OpiskeluoikeusData[Oikeus <: Opiskeluoikeus] {
  val defaultHenkilö = MockOppijat.eero.vainHenkilötiedot
  def defaultOpiskeluoikeus: Oikeus
}
