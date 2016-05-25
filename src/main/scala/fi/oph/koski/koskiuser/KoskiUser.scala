package fi.oph.koski.koskiuser

import javax.servlet.http.HttpServletRequest

import fi.oph.koski.log.{Loggable, Logging, LogUserContext}
import fi.oph.koski.schema.{Organisaatio, OrganisaatioWithOid}
import rx.lang.scala.Observable

class KoskiUser(val oid: String, val clientIp: String, val lang: String, val organisationOidsObservable: Observable[Set[String]]) extends LogUserContext with Loggable with Logging {
  def oidOption = Some(oid)
  def logString = "käyttäjä " + oid
  lazy val organisationOids: Set[String] = organisationOidsObservable.toBlocking.first
  def hasReadAccess(organisaatio: Organisaatio.Oid) = hasAccess(organisaatio, AccessType.read)
  def hasWriteAccess(organisaatio: Organisaatio.Oid) = hasAccess(organisaatio, AccessType.write)
  def hasAccess(organisaatio: Organisaatio.Oid, accessType: AccessType.Value) = accessType match {
    case AccessType.read => true // TODO: käyttöoikeusryhmät
    case AccessType.write => organisationOids.contains(organisaatio)
  }
  organisationOidsObservable.foreach(org => {}) // <- force evaluation to ensure parallel operation
}

object KoskiUser {
  def apply(oid: String, request: HttpServletRequest, userOrganisationsRepository: UserOrganisationsRepository): KoskiUser = {
    new KoskiUser(oid, LogUserContext.clientIpFromRequest(request), "fi", userOrganisationsRepository.getUserOrganisations(oid))
  }

  val systemUser = new KoskiUser("Koski", "-", "fi", Observable.empty) // TODO: not necessarily a good idea
}