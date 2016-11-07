package fi.oph.koski.koskiuser

import javax.servlet.http.HttpServletRequest

import fi.oph.koski.henkilo.AuthenticationServiceClient.PalveluRooli
import fi.oph.koski.koskiuser.Rooli.OPHPAAKAYTTAJA
import fi.oph.koski.log.{LogUserContext, Loggable, Logging}
import fi.oph.koski.schema.{Organisaatio, OrganisaatioWithOid}

import scala.concurrent.{ExecutionContext, Future}

class KoskiUser(user: AuthenticationUser, val clientIp: String, käyttöoikeudet: => Set[Käyttöoikeus]) extends LogUserContext with UserWithUsername with Loggable with Logging {
  def oid = user.oid
  def username = user.username
  def lang = "fi"

  def oidOption = Some(oid)
  def logString = "käyttäjä " + username + " / " + user.oid

  def organisationOids(accessType: AccessType.Value): Set[String] = käyttöoikeudet.collect { case k: KäyttöoikeusOrg if (k.orgAccessType.contains(accessType)) => k.organisaatio.oid }
  lazy val globalAccess = (käyttöoikeudet.collect { case k:KäyttöoikeusGlobal => k.globalAccessType }).flatten
  def isRoot = globalAccess.contains(AccessType.write)
  def isMaintenance = (käyttöoikeudet.collect { case k:KäyttöoikeusGlobal if k.globalPalveluroolit.contains(Rooli.YLLAPITAJA) => k }).nonEmpty
  def isPalvelukäyttäjä = {
    käyttöoikeudet.flatMap(_.palveluRoolit).map(_.rooli).contains(Rooli.TIEDONSIIRTO)
  }
  def hasReadAccess(organisaatio: Organisaatio.Oid) = hasAccess(organisaatio, AccessType.read)
  def hasWriteAccess(organisaatio: Organisaatio.Oid) = hasAccess(organisaatio, AccessType.write)
  def hasAccess(organisaatio: Organisaatio.Oid, accessType: AccessType.Value) = globalAccess.contains(accessType) || organisationOids(accessType).contains(organisaatio)
  def hasGlobalReadAccess = globalAccess.contains(AccessType.read)

  def juuriOrganisaatio: Option[OrganisaatioWithOid] = {
    val juuret = käyttöoikeudet.collect { case r: KäyttöoikeusOrg if r.juuri => r.organisaatio }
    if (juuret.size > 1) {
      None
    } else {
      juuret.headOption
    }
  }

  Future(käyttöoikeudet)(ExecutionContext.global) // haetaan käyttöoikeudet toisessa säikeessä rinnakkain
}

object KoskiUser {
  def apply(user: AuthenticationUser, request: HttpServletRequest, käyttöoikeudet: KäyttöoikeusRepository): KoskiUser = {
    new KoskiUser(user, LogUserContext.clientIpFromRequest(request), käyttöoikeudet.käyttäjänKäyttöoikeudet(user))
  }

  private val KOSKI_SYSTEM_USER: String = "Koski system user"
  // Internal user with root access
  val systemUser = new KoskiUser(AuthenticationUser(KOSKI_SYSTEM_USER, KOSKI_SYSTEM_USER, KOSKI_SYSTEM_USER, None), "KOSKI_SYSTEM", Set(KäyttöoikeusGlobal(List(PalveluRooli(OPHPAAKAYTTAJA)))))
}