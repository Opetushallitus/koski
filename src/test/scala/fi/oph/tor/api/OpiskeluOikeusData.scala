package fi.oph.tor.api

import fi.oph.tor.oppija.MockOppijat
import fi.oph.tor.schema._

trait OpiskeluOikeusData[Oikeus <: Opiskeluoikeus] {
  val defaultHenkilö = MockOppijat.eero.vainHenkilötiedot

  def defaultOpiskeluoikeus: Oikeus

  val tilaValmis: Koodistokoodiviite = Koodistokoodiviite("VALMIS", "suorituksentila")
  val tilaKesken: Koodistokoodiviite = Koodistokoodiviite("KESKEN", "suorituksentila")
}
