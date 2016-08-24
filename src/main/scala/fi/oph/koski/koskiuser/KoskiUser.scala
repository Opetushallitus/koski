package fi.oph.koski.koskiuser

import javax.servlet.http.HttpServletRequest

import fi.oph.koski.log.{LogUserContext, Loggable, Logging}
import fi.oph.koski.organisaatio.Opetushallitus
import fi.oph.koski.schema.{OrganisaatioWithOid, Organisaatio}
import rx.lang.scala.Observable

class KoskiUser(val oid: String, val clientIp: String, val lang: String, käyttöoikeudetObservable: Observable[Set[Käyttöoikeus]]) extends LogUserContext with Loggable with Logging {
  def oidOption = Some(oid)
  def logString = "käyttäjä " + oid

  private lazy val käyttöoikeudet: Set[Käyttöoikeus] = käyttöoikeudetObservable.toBlocking.first
  def organisationOids(accessType: AccessType.Value): Set[String] = käyttöoikeudet.filter(_.ryhmä.orgAccessType.contains(accessType)).flatMap {
    case o:OrganisaatioKäyttöoikeus => Some(o.organisaatio.oid)
    case _ => None
  }
  lazy val globalAccess = käyttöoikeudet.map(_.ryhmä).flatMap(_.globalAccessType)
  def isRoot = käyttöoikeudet.map(_.ryhmä).contains(Käyttöoikeusryhmät.ophPääkäyttäjä)
  def isMaintenance = käyttöoikeudet.map(_.ryhmä).intersect(Set(Käyttöoikeusryhmät.ophPääkäyttäjä, Käyttöoikeusryhmät.ophKoskiYlläpito)).nonEmpty
  def isPalvelukäyttäjä = käyttöoikeudet.map(_.ryhmä).contains(Käyttöoikeusryhmät.oppilaitosPalvelukäyttäjä)
  def hasReadAccess(organisaatio: Organisaatio.Oid) = hasAccess(organisaatio, AccessType.read)
  def hasWriteAccess(organisaatio: Organisaatio.Oid) = hasAccess(organisaatio, AccessType.write)
  def hasAccess(organisaatio: Organisaatio.Oid, accessType: AccessType.Value) = globalAccess.contains(accessType) || organisationOids(accessType).contains(organisaatio)
  def hasGlobalReadAccess = globalAccess.contains(AccessType.read)

  def juuriOrganisaatio: Option[OrganisaatioWithOid] = {
    val juuret = käyttöoikeudet.collect { case r: OrganisaatioKäyttöoikeus if r.juuri => r.organisaatio }
    if (juuret.size > 1) None else juuret.headOption
  }

  käyttöoikeudetObservable.foreach(org => {}) // <- force evaluation to ensure parallel operation
}

object KoskiUser {
  def apply(oid: String, request: HttpServletRequest, käyttöoikeudet: KäyttöoikeusRepository): KoskiUser = {
    new KoskiUser(oid, LogUserContext.clientIpFromRequest(request), "fi", käyttöoikeudet.käyttäjänKäyttöoikeudet(oid))
  }

  // Internal user with root access
  val systemUser = new KoskiUser("Koski", "-", "fi", Observable.just(Set(GlobaaliKäyttöoikeus(Käyttöoikeusryhmät.ophPääkäyttäjä))))
}