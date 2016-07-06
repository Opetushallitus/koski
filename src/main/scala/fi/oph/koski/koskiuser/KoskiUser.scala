package fi.oph.koski.koskiuser

import javax.servlet.http.HttpServletRequest

import fi.oph.koski.log.{LogUserContext, Loggable, Logging}
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.schema.Organisaatio.Oid
import rx.lang.scala.Observable

class KoskiUser(val oid: String, val clientIp: String, val lang: String, käyttöoikeusryhmätObservable: Observable[Set[(Organisaatio.Oid, Käyttöoikeusryhmä)]]) extends LogUserContext with Loggable with Logging {
  def oidOption = Some(oid)
  def logString = "käyttäjä " + oid

  private lazy val käyttöoikeusryhmät: Set[(Oid, Käyttöoikeusryhmä)] = käyttöoikeusryhmätObservable.toBlocking.first
  def organisationOids(accessType: AccessType.Value) = käyttöoikeusryhmät.filter(_._2.orgAccessType.contains(accessType)).map(_._1)
  lazy val globalAccess = käyttöoikeusryhmät.map(_._2).flatMap(_.globalAccessType).toSet
  def isRoot = käyttöoikeusryhmät.map(_._2).contains(Käyttöoikeusryhmät.ophPääkäyttäjä)
  def isMaintenance = käyttöoikeusryhmät.map(_._2).intersect(Set(Käyttöoikeusryhmät.ophPääkäyttäjä, Käyttöoikeusryhmät.ophKoskiYlläpito)).nonEmpty
  def hasReadAccess(organisaatio: Organisaatio.Oid) = hasAccess(organisaatio, AccessType.read)
  def hasWriteAccess(organisaatio: Organisaatio.Oid) = hasAccess(organisaatio, AccessType.write)
  def hasAccess(organisaatio: Organisaatio.Oid, accessType: AccessType.Value) = globalAccess.contains(accessType) || organisationOids(accessType).contains(organisaatio)
  käyttöoikeusryhmätObservable.foreach(org => {}) // <- force evaluation to ensure parallel operation
}

object KoskiUser {
  def apply(oid: String, request: HttpServletRequest, userOrganisationsRepository: UserOrganisationsRepository): KoskiUser = {
    new KoskiUser(oid, LogUserContext.clientIpFromRequest(request), "fi", userOrganisationsRepository.getUserOrganisations(oid))
  }

  // Internal user with root access
  val systemUser = new KoskiUser("Koski", "-", "fi", Observable.just(Set(("", Käyttöoikeusryhmät.ophPääkäyttäjä))))
}