package fi.oph.tor.tutkinto

import com.typesafe.config.Config
import fi.oph.tor.oppija.Oppija

trait TutkintoRepository {
  def findBy(oppija: Oppija): List[Tutkinto]
  def create(tutkinto: Tutkinto)
}

object TutkintoRepository {
  def apply(config: Config) = new MockTutkinkoRepository
}

class MockTutkinkoRepository extends TutkintoRepository {
  private def defaultTutkinnot = List(Tutkinto(oppija = "1.2.246.562.24.00000000001", peruste = "1013059", oppilaitos =  "1"))
  private var tutkinnot = defaultTutkinnot

  override def findBy(oppija: Oppija): List[Tutkinto] = tutkinnot.filter(_.oppija == oppija.oid)

  override def create(tutkinto: Tutkinto) = tutkinnot = tutkinnot :+ tutkinto
}
