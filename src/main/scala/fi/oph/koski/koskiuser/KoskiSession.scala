package fi.oph.koski.koskiuser

import java.net.InetAddress

import fi.oph.koski.koskiuser.Rooli._
import fi.oph.koski.log.{LogUserContext, Loggable, Logging}
import fi.oph.koski.schema.{Organisaatio, OrganisaatioWithOid}
import org.scalatra.servlet.RichRequest

import scala.concurrent.{ExecutionContext, Future}

class KoskiSession(val user: AuthenticationUser, val lang: String, val clientIp: InetAddress, val userAgent: String, käyttöoikeudet: => Set[Käyttöoikeus]) extends LogUserContext with UserWithUsername with UserWithOid with Loggable with Logging {
  def oid = user.oid
  def username = user.username
  def userOption = Some(user)
  def logString = "käyttäjä " + username + " / " + user.oid

  lazy val orgKäyttöoikeudet: Set[KäyttöoikeusOrg] = käyttöoikeudet.collect { case k : KäyttöoikeusOrg => k}
  lazy val globalKäyttöoikeudet: Set[KäyttöoikeusGlobal] = käyttöoikeudet.collect { case k: KäyttöoikeusGlobal => k}
  lazy val globalKoulutusmuotoKäyttöoikeudet: Set[KäyttöoikeusGlobalByKoulutusmuoto] = käyttöoikeudet.collect { case k: KäyttöoikeusGlobalByKoulutusmuoto => k}

  def organisationOids(accessType: AccessType.Value): Set[String] = orgKäyttöoikeudet.collect { case k: KäyttöoikeusOrg if k.organisaatioAccessType.contains(accessType) => k.organisaatio.oid }
  lazy val globalAccess = globalKäyttöoikeudet.flatMap { _.globalAccessType }
  def isRoot = globalAccess.contains(AccessType.write)
  def isPalvelukäyttäjä = orgKäyttöoikeudet.flatMap(_.organisaatiokohtaisetPalveluroolit).contains(Palvelurooli(TIEDONSIIRTO))
  def hasReadAccess(organisaatio: Organisaatio.Oid) = hasAccess(organisaatio, AccessType.read)
  def hasWriteAccess(organisaatio: Organisaatio.Oid) = hasAccess(organisaatio, AccessType.write) && hasRole(LUOTTAMUKSELLINEN)
  def hasTiedonsiirronMitätöintiAccess(organisaatio: Organisaatio.Oid) = hasAccess(organisaatio, AccessType.tiedonsiirronMitätöinti)
  def hasAccess(organisaatio: Organisaatio.Oid, accessType: AccessType.Value) = {
    val access = globalAccess.contains(accessType) || organisationOids(accessType).contains(organisaatio)
    access && (accessType != AccessType.write || hasRole(LUOTTAMUKSELLINEN))
  }

  def hasGlobalKoulutusmuotoReadAccess: Boolean = globalKoulutusmuotoKäyttöoikeudet.flatMap(_.globalAccessType).contains(AccessType.read)

  def allowedOpiskeluoikeusTyypit: Set[String] = globalKoulutusmuotoKäyttöoikeudet.flatMap(_.allowedOpiskeluoikeusTyypit)
  def hasGlobalReadAccess = globalAccess.contains(AccessType.read)
  def hasAnyWriteAccess = (globalAccess.contains(AccessType.write) || organisationOids(AccessType.write).nonEmpty) && hasRole(LUOTTAMUKSELLINEN)
  def hasLocalizationWriteAccess = globalKäyttöoikeudet.find(_.globalPalveluroolit.contains(Palvelurooli("LOKALISOINTI", "CRUD"))).isDefined
  def hasAnyReadAccess = hasGlobalReadAccess || orgKäyttöoikeudet.nonEmpty || hasGlobalKoulutusmuotoReadAccess

  // Note: keep in sync with PermissionCheckServlet's hasSufficientRoles function. See PermissionCheckServlet for more comments.
  private val HenkilonhallintaCrud = Palvelurooli("HENKILONHALLINTA", "CRUD")
  private val HenkilonhallintaReadUpdate = Palvelurooli("HENKILONHALLINTA", "READ_UPDATE")
  def hasHenkiloUiWriteAccess = globalKäyttöoikeudet.exists(ko => ko.globalPalveluroolit.contains(HenkilonhallintaCrud) || ko.globalPalveluroolit.contains(HenkilonhallintaReadUpdate)) ||
    orgKäyttöoikeudet.exists(ko => ko.organisaatiokohtaisetPalveluroolit.contains(HenkilonhallintaCrud) || ko.organisaatiokohtaisetPalveluroolit.contains(HenkilonhallintaReadUpdate))

  def hasRole(role: String): Boolean = {
    val palveluRooli = Palvelurooli("KOSKI", role)
    globalKäyttöoikeudet.exists(_.globalPalveluroolit.contains(palveluRooli)) || orgKäyttöoikeudet.exists(_.organisaatiokohtaisetPalveluroolit.contains(palveluRooli))
  }

  def juuriOrganisaatio: Option[OrganisaatioWithOid] = {
    val juuret = organisaatiot
    if (juuret.size > 1) {
      None
    } else {
      juuret.headOption
    }
  }

  def organisaatiot: List[OrganisaatioWithOid] = orgKäyttöoikeudet.collect { case r: KäyttöoikeusOrg if r.juuri => r.organisaatio }.toList

  Future(käyttöoikeudet)(ExecutionContext.global) // haetaan käyttöoikeudet toisessa säikeessä rinnakkain
}

object KoskiSession {
  def apply(user: AuthenticationUser, request: RichRequest, käyttöoikeudet: KäyttöoikeusRepository): KoskiSession = {
    new KoskiSession(user, KoskiUserLanguage.getLanguageFromCookie(request), LogUserContext.clientIpFromRequest(request), LogUserContext.userAgent(request), käyttöoikeudet.käyttäjänKäyttöoikeudet(user))
  }

  private val KOSKI_SYSTEM_USER: String = "Koski system user"
  private val UNTRUSTED_SYSTEM_USER = "Koski untrusted system user"
  // Internal user with root access
  val systemUser = new KoskiSession(AuthenticationUser(KOSKI_SYSTEM_USER, KOSKI_SYSTEM_USER, KOSKI_SYSTEM_USER, None), "fi", InetAddress.getLoopbackAddress, "", Set(KäyttöoikeusGlobal(List(Palvelurooli(OPHPAAKAYTTAJA), Palvelurooli(LUOTTAMUKSELLINEN)))))

  val untrustedUser = new KoskiSession(AuthenticationUser(UNTRUSTED_SYSTEM_USER, UNTRUSTED_SYSTEM_USER, UNTRUSTED_SYSTEM_USER, None), "fi", InetAddress.getLoopbackAddress, "", Set())
}

