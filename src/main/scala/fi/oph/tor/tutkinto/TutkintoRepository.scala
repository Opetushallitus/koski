package fi.oph.tor.tutkinto

import com.typesafe.config.Config
import fi.oph.tor.arvosana.ArviointiasteikkoRepository
import fi.oph.tor.tutkinto.EPerusteetTutkintoRepository

trait TutkintoRepository {
  def findTutkinnot(oppilaitosId: String, query: String): List[Tutkinto]
  def findByEPerusteDiaarinumero(diaariNumero: String): Option[Tutkinto]
  def findPerusteRakenne(diaariNumero: String)(implicit arviointiAsteikot: ArviointiasteikkoRepository): Option[TutkintoRakenne]
}

object TutkintoRepository {
  def apply(config: Config) = {
    if (config.hasPath("eperusteet")) {
      new EPerusteetTutkintoRepository(config.getString("eperusteet.url"))
    } else {
      new MockTutkintoRepository
    }
  }}

