package fi.oph.tor.tutkinto

import com.typesafe.config.Config
import fi.oph.tor.eperusteet.EPerusteetClient

trait TutkintoRepository {
  def findTutkinnot(oppilaitosId: String, query: String): List[Tutkinto]
  def findByEPerusteDiaarinumero(diaariNumero: String): Option[Tutkinto]
  def findPerusteRakenne(diaariNumero: String): Option[RakenneOsa]
}

object TutkintoRepository {
  def apply(config: Config) = {
    if (config.hasPath("eperusteet")) {
      new EPerusteetClient(config.getString("eperusteet.url"))
    } else {
      new MockTutkintoRepository
    }
  }}

