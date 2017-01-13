package fi.oph.koski.koskiuser

import javax.servlet.http.HttpServletRequest

import fi.oph.koski.henkilo.AuthenticationServiceClient.Palvelurooli
import fi.oph.koski.koskiuser.Rooli._
import fi.oph.koski.log.{LogUserContext, Loggable, Logging}
import fi.oph.koski.schema.{Organisaatio, OrganisaatioWithOid}

import scala.concurrent.{ExecutionContext, Future}

class KoskiSession(val user: AuthenticationUser, val lang: String, val clientIp: String, käyttöoikeudet: => Set[Käyttöoikeus]) extends LogUserContext with UserWithUsername with UserWithOid with Loggable with Logging {
  def oid = user.oid
  def username = user.username
  def userOption = Some(user)
  def logString = "käyttäjä " + username + " / " + user.oid

  lazy val orgKäyttöoikeudet = käyttöoikeudet.collect { case k : KäyttöoikeusOrg => k}
  lazy val globalKäyttöoikeudet = käyttöoikeudet.collect { case k: KäyttöoikeusGlobal => k}
  def organisationOids(accessType: AccessType.Value): Set[String] = orgKäyttöoikeudet.collect { case k: KäyttöoikeusOrg if (k.organisaatioAccessType.contains(accessType)) => k.organisaatio.oid }
  lazy val globalAccess = globalKäyttöoikeudet.flatMap { _.globalAccessType }
  def isRoot = globalAccess.contains(AccessType.write)
  def isMaintenance = globalKäyttöoikeudet.find { k => k.globalPalveluroolit.contains(Palvelurooli(YLLAPITAJA))}.isDefined
  def isPalvelukäyttäjä = orgKäyttöoikeudet.flatMap(_.organisaatiokohtaisetPalveluroolit).contains(Palvelurooli(TIEDONSIIRTO))
  def hasReadAccess(organisaatio: Organisaatio.Oid) = hasAccess(organisaatio, AccessType.read)
  def hasWriteAccess(organisaatio: Organisaatio.Oid) = hasAccess(organisaatio, AccessType.write)
  def hasAccess(organisaatio: Organisaatio.Oid, accessType: AccessType.Value) = globalAccess.contains(accessType) || organisationOids(accessType).contains(organisaatio)
  def hasGlobalReadAccess = globalAccess.contains(AccessType.read)

  def juuriOrganisaatio: Option[OrganisaatioWithOid] = {
    val juuret = orgKäyttöoikeudet.collect { case r: KäyttöoikeusOrg if r.juuri => r.organisaatio }
    if (juuret.size > 1) {
      None
    } else {
      juuret.headOption
    }
  }

  Future(käyttöoikeudet)(ExecutionContext.global) // haetaan käyttöoikeudet toisessa säikeessä rinnakkain
}

object KoskiSession {
  def apply(user: AuthenticationUser, lang: String, request: HttpServletRequest, käyttöoikeudet: KäyttöoikeusRepository): KoskiSession = {
    new KoskiSession(user, lang, LogUserContext.clientIpFromRequest(request), käyttöoikeudet.käyttäjänKäyttöoikeudet(user))
  }

  private val KOSKI_SYSTEM_USER: String = "Koski system user"
  // Internal user with root access
  val systemUser = new KoskiSession(AuthenticationUser(KOSKI_SYSTEM_USER, KOSKI_SYSTEM_USER, KOSKI_SYSTEM_USER, None), "fi", "KOSKI_SYSTEM", Set(KäyttöoikeusGlobal(List(Palvelurooli(OPHPAAKAYTTAJA)))))
}