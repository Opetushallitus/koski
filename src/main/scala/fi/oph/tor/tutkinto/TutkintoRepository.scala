package fi.oph.tor.tutkinto

import com.typesafe.config.Config
import fi.oph.tor.arvosana.ArviointiasteikkoRepository
import fi.oph.tor.eperusteet.EPerusteetClient

trait TutkintoRepository {
  def findTutkinnot(oppilaitosId: String, query: String): List[Tutkinto]
  def findByEPerusteDiaarinumero(diaariNumero: String): Option[Tutkinto]
  def findPerusteRakenne(diaariNumero: String)(implicit arviointiAsteikot: ArviointiasteikkoRepository): Option[TutkintoRakenne]
}

object TutkintoRepository {
  def apply(config: Config) = {
    if (config.hasPath("eperusteet")) {
      new EPerusteetClient(config.getString("eperusteet.url"))
    } else {
      new MockTutkintoRepository
    }
  }}

