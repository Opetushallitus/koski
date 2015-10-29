package fi.oph.tor.eperusteet

import com.typesafe.config.Config

trait EPerusteetRepository {
  def findPerusteet(query: String): EPerusteet

  def findPerusteetByDiaarinumero(diaarinumero: String): EPerusteet

  def findRakenne(diaariNumero: String): Option[EPerusteRakenne]
}

object EPerusteetRepository {
  def apply(config: Config) = {
    if (config.hasPath("eperusteet")) {
      new RemoteEPerusteetRepository(config.getString("eperusteet.url"))
    } else {
      new MockEPerusteetRepository
    }
  }
}