package fi.oph.tor.opintooikeus

import com.typesafe.config.Config
import fi.oph.tor.oppija.Oppija

trait OpintoOikeusRepository {
  def filterOppijat(oppijat: List[Oppija]): List[Oppija]
  def findBy(oppija: Oppija): List[OpintoOikeus]
  def create(opintoOikeus: OpintoOikeus)
  def resetMocks {}
}

object OpintoOikeusRepository {
  def apply(config: Config) = new MockOpintoOikeusRepository
}


