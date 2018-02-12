package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat.eero
import fi.oph.koski.schema._

trait OpiskeluoikeusData[Oikeus <: Opiskeluoikeus] {
  val defaultHenkilö: UusiHenkilö =
    UusiHenkilö(eero.henkilö.hetu.get, eero.henkilö.etunimet, eero.henkilö.kutsumanimi, eero.henkilö.sukunimi)
  def defaultOpiskeluoikeus: Oikeus
}
