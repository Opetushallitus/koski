package fi.oph.tor.toruser

import fi.oph.tor.log.Loggable
import fi.oph.tor.organisaatio.InMemoryOrganisaatioRepository

case class TorUser(oid: String, userOrganisations: InMemoryOrganisaatioRepository) extends Loggable {
  override def toString = "käyttäjä " + oid
}