package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.schema._

trait OpiskeluOikeusData[Oikeus <: Opiskeluoikeus] {
  val defaultHenkilö = MockOppijat.eero.vainHenkilötiedot

  def defaultOpiskeluoikeus: Oikeus

  val tilaValmis: Koodistokoodiviite = Koodistokoodiviite("VALMIS", "suorituksentila")
  val tilaKesken: Koodistokoodiviite = Koodistokoodiviite("KESKEN", "suorituksentila")
}
