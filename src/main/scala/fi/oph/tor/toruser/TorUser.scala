package fi.oph.tor.toruser

import javax.servlet.http.HttpServletRequest

import fi.oph.tor.log.{Loggable, Logging, LogUserContext}
import fi.oph.tor.schema.OrganisaatioWithOid
import rx.lang.scala.Observable

class TorUser(val oid: String, val clientIp: String, val lang: String, val organisationOidsObservable: Observable[Set[String]]) extends LogUserContext with Loggable with Logging {
  def oidOption = Some(oid)
  def logString = "käyttäjä " + oid
  lazy val organisationOids: Set[String] = organisationOidsObservable.toBlocking.first
  def hasReadAccess(organisaatio: OrganisaatioWithOid) = hasAccess(organisaatio, AccessType.read)
  def hasWriteAccess(organisaatio: OrganisaatioWithOid) = hasAccess(organisaatio, AccessType.write)
  def hasAccess(organisaatio: OrganisaatioWithOid, accessType: AccessType.Value) = accessType match {
    case AccessType.read => true // TODO: käyttöoikeusryhmät
    case AccessType.write => organisationOids.contains(organisaatio.oid)
  }
  organisationOidsObservable.foreach(org => {}) // <- force evaluation to ensure parallel operation
}

object TorUser {
  def apply(oid: String, request: HttpServletRequest, userOrganisationsRepository: UserOrganisationsRepository): TorUser = {
    new TorUser(oid, LogUserContext.clientIpFromRequest(request), "fi", userOrganisationsRepository.getUserOrganisations(oid))
  }

  val systemUser = new TorUser("Koski", "-", "fi", Observable.empty) // TODO: not necessarily a good idea
}