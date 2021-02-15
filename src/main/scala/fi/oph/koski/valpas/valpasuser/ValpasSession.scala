package fi.oph.koski.valpas.valpasuser

import java.net.InetAddress

import fi.oph.koski.json.SensitiveDataAllowed
import fi.oph.koski.koskiuser.Rooli.Role
import fi.oph.koski.koskiuser.{AuthenticationUser, Session, UserLanguage, Käyttöoikeus, KäyttöoikeusOrg, KäyttöoikeusRepository, UserWithOid, UserWithUsername}
import fi.oph.koski.log.LogUserContext
import fi.oph.koski.schema.Organisaatio.Oid
import org.scalatra.servlet.RichRequest

import scala.concurrent.{ExecutionContext, Future}

class ValpasSession(user: AuthenticationUser, lang: String, clientIp: InetAddress, userAgent: String, käyttöoikeudet: => Set[Käyttöoikeus]) extends Session(user, lang, clientIp, userAgent) with SensitiveDataAllowed {
  def orgKäyttöoikeudet: Set[KäyttöoikeusOrg] = käyttöoikeudet.collect { case k : KäyttöoikeusOrg => k}.filter(hasValpasRooli)
  def varhaiskasvatusKoulutustoimijat: Set[Oid] = Set.empty
  def hasKoulutustoimijaVarhaiskasvatuksenJärjestäjäAccess: Boolean = false

  def hasGlobalReadAccess: Boolean = false

  def sensitiveDataAllowed(allowedRoles: Set[Role]): Boolean = false

  private def hasValpasRooli(organisaatioKäyttöoikeus: KäyttöoikeusOrg) = {
    organisaatioKäyttöoikeus.organisaatiokohtaisetPalveluroolit.exists(_.palveluName == "VALPAS")
  }

  // TODO: Filtteröi tässä lista niin, että sille jää vain Valpas-oikeudet
  Future(käyttöoikeudet)(ExecutionContext.global) // haetaan käyttöoikeudet toisessa säikeessä rinnakkain
}

object ValpasSession {
  def apply(user: AuthenticationUser, request: RichRequest, käyttöoikeudet: KäyttöoikeusRepository): ValpasSession = {
    new ValpasSession(user, UserLanguage.getLanguageFromCookie(request), LogUserContext.clientIpFromRequest(request), LogUserContext.userAgent(request), käyttöoikeudet.käyttäjänKäyttöoikeudet(user))
  }

  private val UNTRUSTED_SYSTEM_USER = "Valpas untrusted system user"

  val untrustedUser = new ValpasSession(AuthenticationUser(UNTRUSTED_SYSTEM_USER, UNTRUSTED_SYSTEM_USER, UNTRUSTED_SYSTEM_USER, None), "fi", InetAddress.getLoopbackAddress, "", Set())
}
