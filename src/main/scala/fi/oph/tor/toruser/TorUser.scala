package fi.oph.tor.toruser

import fi.oph.tor.log.{Loggable, Logging}
import fi.oph.tor.schema.OrganisaatioWithOid
import rx.lang.scala.Observable

class TorUser(val oid: String, val clientIp: String, val lang: String, val organisationOidsObservable: Observable[Set[String]]) extends Loggable with Logging {
  def logString = "käyttäjä " + oid
  lazy val organisationOids: Set[String] = organisationOidsObservable.toBlocking.first
  def hasWriteAccess(organisaatio: OrganisaatioWithOid) = hasAccess(organisaatio, AccessType.write)
  def hasAccess(organisaatio: OrganisaatioWithOid, accessType: AccessType.Value) = accessType match {
    case AccessType.read => true // TODO: käyttöoikeusryhmät
    case AccessType.write => organisationOids.contains(organisaatio.oid)
  }
  organisationOidsObservable.foreach(org => {}) // <- force evaluation to ensure parallel operation
}

object TorUser {
  def apply(oid: String, clientIp: String, userOrganisationsRepository: UserOrganisationsRepository) = {
    new TorUser(oid, clientIp, "fi", userOrganisationsRepository.getUserOrganisations(oid))
  }
}