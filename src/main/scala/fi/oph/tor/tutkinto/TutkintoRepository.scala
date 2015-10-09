package fi.oph.tor.tutkinto

import com.typesafe.config.Config

trait TutkintoRepository {
  def findTutkinnot(oppilaitosId: String, query: String): List[Tutkinto]
  def findById(id: String): Option[Tutkinto]
}

object TutkintoRepository {
  def apply(config: Config) = new MockTutkintoRepository
}

class MockTutkintoRepository extends TutkintoRepository {
  def tutkinnot = List(
    Tutkinto("Autoalan työnjohdon erikoisammattitutkinto", ePerusteDiaarinumero =  "1013059", tutkintoKoodi =  "357305")
  )

  override def findTutkinnot(oppilaitosId: String, query: String) = {
    tutkinnot.filter(_.toString.toLowerCase.contains(query.toLowerCase))
  }

  override def findById(id: String) = tutkinnot.filter(_.ePerusteDiaarinumero == id).headOption
}