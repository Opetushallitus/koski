package fi.oph.koski.valpas.valpasuser

import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.json.SensitiveDataAllowed
import fi.oph.koski.koskiuser.Rooli.Role
import fi.oph.koski.koskiuser._
import fi.oph.koski.log.LogUserContext
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.schema.Organisaatio.Oid
import org.scalatra.servlet.RichRequest

import java.net.InetAddress
import scala.concurrent.Future

class ValpasSession(
  user: AuthenticationUser,
  lang: String,
  clientIp: InetAddress,
  userAgent: String,
  lähdeKäyttöoikeudet: => Set[Käyttöoikeus]
) extends Session(user, lang, clientIp, userAgent) with SensitiveDataAllowed with GlobalExecutionContext {
  def varhaiskasvatusKoulutustoimijat: Set[Oid] = Set.empty
  def hasKoulutustoimijaVarhaiskasvatuksenJärjestäjäAccess: Boolean = false

  def hasGlobalReadAccess: Boolean = false
  def sensitiveDataAllowed(allowedRoles: Set[Role]): Boolean = false

  def hasGlobalValpasOikeus(requiredRoles: Set[String]): Boolean = {
    val käyttäjänGlobaalitValpasOikeudet = globalKäyttöoikeudet
      .flatMap(_.globalPalveluroolit.filter(_.palveluName == "VALPAS").map(_.rooli))
    requiredRoles.subsetOf(käyttäjänGlobaalitValpasOikeudet)
  }

  def hasKelaAccess: Boolean = globalViranomaisKäyttöoikeudet.flatMap(_.globalPalveluroolit).intersect(Set(Palvelurooli("VALPAS", ValpasRooli.KELA))).nonEmpty

  def hasYtlAccess: Boolean = globalViranomaisKäyttöoikeudet.flatMap(_.globalPalveluroolit).intersect(Set(Palvelurooli("VALPAS", ValpasRooli.YTL))).nonEmpty

  def organisaationRoolit(organisaatioOid: Organisaatio.Oid): Set[ValpasRooli.Role] =
    käyttöoikeudet.flatMap {
      case k: KäyttöoikeusOrg if k.organisaatio.oid == organisaatioOid => k.organisaatiokohtaisetPalveluroolit.map(_.rooli)
      case _ => Set.empty
    }

  protected def kaikkiKäyttöoikeudet: Set[Käyttöoikeus] = käyttöoikeudet

  // Sessio luodaan aina uudestaan jokaisessa API-kutsussa, joten käyttöoikeudet voi tallentaa lazy val:iin eikä hakea ja filteröida aina uudestaan
  private lazy val käyttöoikeudet: Set[Käyttöoikeus] =
    Käyttöoikeus.withPalveluroolitFilter(lähdeKäyttöoikeudet, _.palveluName == "VALPAS")

  Future(lähdeKäyttöoikeudet) // haetaan käyttöoikeudet toisessa säikeessä rinnakkain
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
