package fi.oph.koski.valpas.valpasuser

import java.net.InetAddress

import fi.oph.koski.json.SensitiveDataAllowed
import fi.oph.koski.koskiuser.Rooli.Role
import fi.oph.koski.koskiuser.{AuthenticationUser, Käyttöoikeus, KäyttöoikeusRepository, Session, UserLanguage}
import fi.oph.koski.log.LogUserContext
import fi.oph.koski.schema.Organisaatio.Oid
import org.scalatra.servlet.RichRequest

import scala.concurrent.{ExecutionContext, Future}

class ValpasSession(
  user: AuthenticationUser,
  lang: String,
  clientIp: InetAddress,
  userAgent: String,
  lähdeKäyttöoikeudet: => Set[Käyttöoikeus]
) extends Session(user, lang, clientIp, userAgent) with SensitiveDataAllowed {
  def varhaiskasvatusKoulutustoimijat: Set[Oid] = Set.empty
  def hasKoulutustoimijaVarhaiskasvatuksenJärjestäjäAccess: Boolean = false

  def hasGlobalReadAccess: Boolean = false
  def sensitiveDataAllowed(allowedRoles: Set[Role]): Boolean = false

  def hasGlobalValpasOikeus(requiredRoles: Set[String]): Boolean = {
    val käyttäjänGlobaalitValpasOikeudet = globalKäyttöoikeudet
      .flatMap(_.globalPalveluroolit.filter(_.palveluName == "VALPAS").map(_.rooli))
    requiredRoles.subsetOf(käyttäjänGlobaalitValpasOikeudet)
  }

  protected def kaikkiKäyttöoikeudet: Set[Käyttöoikeus] = käyttöoikeudet

  // Sessio luodaan aina uudestaan jokaisessa API-kutsussa, joten käyttöoikeudet voi tallentaa lazy val:iin eikä hakea ja filteröida aina uudestaan
  private lazy val käyttöoikeudet: Set[Käyttöoikeus] =
    Käyttöoikeus.withPalveluroolitFilter(lähdeKäyttöoikeudet, _.palveluName == "VALPAS")

  Future(lähdeKäyttöoikeudet)(ExecutionContext.global) // haetaan käyttöoikeudet toisessa säikeessä rinnakkain
}

object ValpasSession {
  def apply(
    user: AuthenticationUser,
    request: RichRequest,
    käyttöoikeudet: KäyttöoikeusRepository
  ): ValpasSession = {
    new ValpasSession(
      user,
      UserLanguage.getLanguageFromCookie(request),
      LogUserContext.clientIpFromRequest(request),
      LogUserContext.userAgent(request),
      käyttöoikeudet.käyttäjänKäyttöoikeudet(user)
    )
  }

  private val UNTRUSTED_SYSTEM_USER = "Valpas untrusted system user"

  val untrustedUser = new ValpasSession(
    AuthenticationUser(UNTRUSTED_SYSTEM_USER, UNTRUSTED_SYSTEM_USER, UNTRUSTED_SYSTEM_USER, None),
    "fi",
    InetAddress.getLoopbackAddress,
    "",
    Set()
  )
}
