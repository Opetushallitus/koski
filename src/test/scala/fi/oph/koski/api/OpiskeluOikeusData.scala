package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat.{eero, asUusiOppija}
import fi.oph.koski.schema._

trait OpiskeluoikeusData[Oikeus <: Opiskeluoikeus] {
  val defaultHenkilö: UusiHenkilö = asUusiOppija(eero)
  def defaultOpiskeluoikeus: Oikeus
}
