package fi.oph.tor.eperusteet

import com.typesafe.config.Config
import fi.oph.tor.log.TimedProxy

trait EPerusteetRepository {
  def findPerusteet(query: String): List[EPeruste]

  def findPerusteetByDiaarinumero(diaarinumero: String): List[EPeruste]

  def findRakenne(diaariNumero: String): Option[EPerusteRakenne]
}

object EPerusteetRepository {

  def apply(config: Config) = {
    TimedProxy(if (config.hasPath("eperusteet")) {
      new RemoteEPerusteetRepository(config.getString("eperusteet.url"))
    } else {
      new MockEPerusteetRepository
    })
  }
}