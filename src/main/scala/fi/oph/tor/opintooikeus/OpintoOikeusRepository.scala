package fi.oph.tor.opintooikeus

import fi.oph.tor.oppija.Oppija
import fi.oph.tor.user.UserContext

trait OpintoOikeusRepository {
  def filterOppijat(oppijat: List[Oppija])(implicit userContext: UserContext): List[Oppija]
  def findBy(oppija: Oppija)(implicit userContext: UserContext): List[OpintoOikeus]
  def create(opintoOikeus: OpintoOikeus)
  def resetFixtures {}
}

