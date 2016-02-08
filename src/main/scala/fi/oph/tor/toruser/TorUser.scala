package fi.oph.tor.toruser

import fi.oph.tor.log.Loggable
import fi.oph.tor.schema.OrganisaatioWithOid

case class TorUser(oid: String, organisationOids: Set[String]) extends Loggable {
  override def toString = "käyttäjä " + oid
  def hasReadAccess(organisaatio: OrganisaatioWithOid) = organisationOids.contains(organisaatio.oid)
}