package fi.oph.tor.tutkinto

import com.typesafe.config.Config

trait TutkintoRepository {
  def findTutkinnot(oppilaitosId: String, query: String): List[Tutkinto]
  def findByEPerusteDiaarinumero(id: String): Option[Tutkinto]
}

object TutkintoRepository {
  def apply(config: Config) = {
    if (config.hasPath("eperusteet")) {
      new EPerusteetTutkintoRepository(config.getString("eperusteet.url"))
    } else {
      new MockTutkintoRepository
    }
  }}

