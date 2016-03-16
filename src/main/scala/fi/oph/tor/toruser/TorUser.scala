package fi.oph.tor.toruser

import fi.oph.tor.log.{Loggable, Logging}
import fi.oph.tor.schema.OrganisaatioWithOid
import rx.lang.scala.Observable

class TorUser(val oid: String, val clientIp: String, val organisationOidsObservable: Observable[Set[String]]) extends Loggable with Logging {
  override def toString = "käyttäjä " + oid
  lazy val organisationOids: Set[String] = organisationOidsObservable.toBlocking.first
  def hasReadAccess(organisaatio: OrganisaatioWithOid) = organisationOids.contains(organisaatio.oid)
  organisationOidsObservable.foreach(org => {}) // <- force evaluation to ensure parallel operation
}

object TorUser {
  def apply(oid: String, clientIp: String, userOrganisationsRepository: UserOrganisationsRepository) = {
    new TorUser(oid, clientIp, userOrganisationsRepository.getUserOrganisations(oid))
  }
}