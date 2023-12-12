package fi.oph.koski.koskiuser

import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.huoltaja.{Huollettava, HuollettavienHakuOnnistui}
import fi.oph.koski.json.SensitiveDataAllowed
import fi.oph.koski.koskiuser.Rooli._
import fi.oph.koski.log.{LogUserContext, Loggable, Logging}
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.schema.{OpiskeluoikeudenTyyppi, Organisaatio, OrganisaatioWithOid}
import org.scalatra.servlet.RichRequest

import java.net.InetAddress
import scala.concurrent.Future

abstract class Session(val user: AuthenticationUser, val lang: String, val clientIp: InetAddress, val userAgent: String)
  extends LogUserContext
    with UserWithUsername
    with UserWithOid
    with Loggable
    with Logging {

  def oid = user.oid
  def username = user.username
  def userOption = Some(user)
  def logString = "käyttäjä " + username + " / " + user.oid

  lazy val globalKäyttöoikeudet: Set[KäyttöoikeusGlobal] = kaikkiKäyttöoikeudet.collect { case k: KäyttöoikeusGlobal => k}
  lazy val orgKäyttöoikeudet: Set[KäyttöoikeusOrg] = kaikkiKäyttöoikeudet.collect { case k : KäyttöoikeusOrg => k}
  lazy val globalViranomaisKäyttöoikeudet: Set[KäyttöoikeusViranomainen] = kaikkiKäyttöoikeudet.collect { case k: KäyttöoikeusViranomainen => k}

  def varhaiskasvatusKoulutustoimijat: Set[Oid]
  def hasKoulutustoimijaVarhaiskasvatuksenJärjestäjäAccess: Boolean

  def hasGlobalReadAccess: Boolean

  def hasPalvelurooli(palvelurooliFilter: Palvelurooli => Boolean) = Käyttöoikeus.hasPalvelurooli(kaikkiKäyttöoikeudet, palvelurooliFilter)

  protected def kaikkiKäyttöoikeudet: Set[Käyttöoikeus]
}

class KoskiSpecificSession(
  user: AuthenticationUser,
  lang: String,
  clientIp: InetAddress,
  userAgent: String,
  lähdeKäyttöoikeudet: => Set[Käyttöoikeus]
) extends Session(user, lang, clientIp, userAgent)  with SensitiveDataAllowed with GlobalExecutionContext {

  lazy val varhaiskasvatusKäyttöoikeudet: Set[KäyttöoikeusVarhaiskasvatusToimipiste] = käyttöoikeudet.collect { case k: KäyttöoikeusVarhaiskasvatusToimipiste => k }
  lazy val varhaiskasvatusKoulutustoimijat: Set[Oid] = varhaiskasvatusKäyttöoikeudet.map(_.koulutustoimija.oid)
  lazy val hasKoulutustoimijaVarhaiskasvatuksenJärjestäjäAccess: Boolean = varhaiskasvatusKäyttöoikeudet.nonEmpty
  lazy val allowedOpiskeluoikeusTyypit: Set[String] = käyttöoikeudet.flatMap(_.allowedOpiskeluoikeusTyypit)
  lazy val hasKoulutusmuotoRestrictions: Boolean = allowedOpiskeluoikeusTyypit != OpiskeluoikeudenTyyppi.kaikkiTyypit.map(_.koodiarvo)
  lazy val kaikkiKäyttöoikeudet: Set[Käyttöoikeus] = käyttöoikeudet

  def organisationOids(accessType: AccessType.Value): Set[String] = orgKäyttöoikeudet.collect { case k: KäyttöoikeusOrg if k.organisaatioAccessType.contains(accessType) => k.organisaatio.oid }
  lazy val globalAccess = globalKäyttöoikeudet.flatMap { _.globalAccessType }
  def isRoot = globalAccess.contains(AccessType.write)
  def isPalvelukäyttäjä = orgKäyttöoikeudet.flatMap(_.organisaatiokohtaisetPalveluroolit).contains(Palvelurooli(TIEDONSIIRTO))
  def hasReadAccess(organisaatio: Organisaatio.Oid, koulutustoimija: Option[Organisaatio.Oid]) = hasAccess(organisaatio, koulutustoimija, AccessType.read)

  def hasRaporttiReadAccess(organisaatio: Organisaatio.Oid): Boolean = {
    hasReadAccess(organisaatio, None) ||
      varhaiskasvatusKäyttöoikeudet
        .filter(_.onVarhaiskasvatuksenToimipiste)
        .exists(oikeus =>
           oikeus.ulkopuolinenOrganisaatio.oid == organisaatio &&
             hasReadAccess(organisaatio, Some(oikeus.koulutustoimija.oid))
        )
  }

  def hasWriteAccess(organisaatio: Organisaatio.Oid, koulutustoimija: Option[Organisaatio.Oid]) = hasAccess(organisaatio, koulutustoimija, AccessType.write) && hasRole(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)
  def hasTiedonsiirronMitätöintiAccess(organisaatio: Organisaatio.Oid, koulutustoimija: Option[Organisaatio.Oid]) = hasAccess(organisaatio, koulutustoimija, AccessType.tiedonsiirronMitätöinti)

  // HUOM!
  // Kun lisäät uuden luovutuspalvelukäyttöoikeuden alle, muista lisätä se myös
  // KoskiSpecificAuthenticationSupport.requireVirkailijaOrPalvelukäyttäjä -metodiin
  // vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
  def hasLuovutuspalveluAccess: Boolean = globalViranomaisKäyttöoikeudet.exists(_.isLuovutusPalveluAllowed)
  def hasTilastokeskusAccess: Boolean = globalViranomaisKäyttöoikeudet.flatMap(_.globalPalveluroolit).contains(Palvelurooli("KOSKI", TILASTOKESKUS))
  def hasMitätöidytOpiskeluoikeudetAccess: Boolean = hasTilastokeskusAccess || hasYtlAccess || globalKäyttöoikeudet.exists(_.globalPalveluroolit.exists(_.rooli == MITATOIDYT_OPISKELUOIKEUDET))
  def hasPoistetutOpiskeluoikeudetAccess: Boolean = globalKäyttöoikeudet.exists(_.globalPalveluroolit.exists(_.rooli == POISTETUT_OPISKELUOIKEUDET))
  def hasValviraAccess: Boolean = globalViranomaisKäyttöoikeudet.flatMap(_.globalPalveluroolit).contains(Palvelurooli("KOSKI", VALVIRA))
  def hasMigriAccess: Boolean = globalViranomaisKäyttöoikeudet.flatMap(_.globalPalveluroolit).contains(Palvelurooli("KOSKI", MIGRI))
  def hasKelaAccess: Boolean = !globalViranomaisKäyttöoikeudet.flatMap(_.globalPalveluroolit).intersect(Set(Palvelurooli("KOSKI", LUOTTAMUKSELLINEN_KELA_LAAJA), Palvelurooli("KOSKI", LUOTTAMUKSELLINEN_KELA_SUPPEA))).isEmpty
  def hasYtlAccess: Boolean = globalViranomaisKäyttöoikeudet.flatMap(_.globalPalveluroolit).contains(Palvelurooli("KOSKI", YTL))
  // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  // HUOM!
  // Kun lisäät uuden luovutuspalvelukäyttöoikeuden ylle, muista lisätä se myös
  // KoskiSpecificAuthenticationSupport.requireVirkailijaOrPalvelukäyttäjä -metodiin

  def hasVarhaiskasvatusAccess(koulutustoimijaOid: Organisaatio.Oid, organisaatioOid: Organisaatio.Oid, accessType: AccessType.Value): Boolean = {
    val oikeudet: Set[KäyttöoikeusVarhaiskasvatusToimipiste] = varhaiskasvatusKäyttöoikeudet.filter(_.organisaatioAccessType.contains(accessType))
    globalAccess.contains(accessType) || oikeudet.exists { case KäyttöoikeusVarhaiskasvatusToimipiste(koulutustoimija, organisaatio, _, _) =>
      koulutustoimijaOid == koulutustoimija.oid && organisaatioOid == organisaatio.oid
    }
  }

  def hasTallennetutYlioppilastutkinnonOpiskeluoikeudetAccess: Boolean =
    globalKäyttöoikeudet.exists(_.globalPalveluroolit.contains(Palvelurooli(TALLENNETUT_YLIOPPILASTUTKINNON_OPISKELUOIKEUDET)))

  def getKoulutustoimijatWithWriteAccess: List[Oid] = orgKäyttöoikeudet
    .filter(_.organisaatioAccessType.contains(AccessType.write))
    .flatMap(_.organisaatio.toKoulutustoimija).map(_.oid)
    .toList

  def hasKoulutustoimijaOrganisaatioTaiGlobaaliWriteAccess = {
    val koulutustoimijat = getKoulutustoimijatWithWriteAccess
    koulutustoimijat.nonEmpty || globalAccess.contains(AccessType.write)
  }

  def hasTaiteenPerusopetusAccess(organisaatio: Organisaatio.Oid, koulutustoimija: Option[Organisaatio.Oid], accessType: AccessType.Value): Boolean = {
    globalAccess.contains(accessType) ||
      organisationOids(accessType).contains(organisaatio) ||
      orgKäyttöoikeudet
        .filter(_.organisaatio.toKoulutustoimija.isDefined)
        .exists(k => koulutustoimija.contains(k.organisaatio.oid) && k.organisaatioAccessType.contains(accessType))
  }

  def hasAccess(organisaatio: Organisaatio.Oid, koulutustoimija: Option[Organisaatio.Oid], accessType: AccessType.Value): Boolean = {
    val access = globalAccess.contains(accessType) ||
      organisationOids(accessType).contains(organisaatio) ||
      koulutustoimija.exists(kt => hasVarhaiskasvatusAccess(kt, organisaatio, accessType))

    access && (accessType != AccessType.write || hasRole(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  }

  def hasGlobalKoulutusmuotoReadAccess: Boolean = globalViranomaisKäyttöoikeudet.flatMap(_.globalAccessType).contains(AccessType.read)

  def hasGlobalReadAccess = globalAccess.contains(AccessType.read)
  def hasAnyWriteAccess = (globalAccess.contains(AccessType.write) || organisationOids(AccessType.write).nonEmpty) && hasRole(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)
  def hasLocalizationWriteAccess = globalKäyttöoikeudet.find(_.globalPalveluroolit.contains(Palvelurooli("LOKALISOINTI", "CRUD"))).isDefined
  def hasAnyReadAccess = hasGlobalReadAccess || orgKäyttöoikeudet.exists(_.organisaatioAccessType.contains(AccessType.read)) || hasGlobalKoulutusmuotoReadAccess
  def hasAnyTiedonsiirronMitätöintiAccess = globalAccess.contains(AccessType.tiedonsiirronMitätöinti) || organisationOids(AccessType.tiedonsiirronMitätöinti).nonEmpty
  def hasRaportitAccess = hasAnyReadAccess && hasRole(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT) && !hasGlobalKoulutusmuotoReadAccess
  def sensitiveDataAllowed(requiredRoles: Set[Role]) = requiredRoles.exists(hasRole)

  // Note: keep in sync with PermissionCheckServlet's hasSufficientRoles function. See PermissionCheckServlet for more comments.
  private val OppijanumerorekisteriRekisterinpitäjä = Palvelurooli("OPPIJANUMEROREKISTERI", "REKISTERINPITAJA")
  private val OppijanumerorekisteriReadUpdate = Palvelurooli("OPPIJANUMEROREKISTERI", "HENKILON_RU")
  def hasHenkiloUiWriteAccess = globalKäyttöoikeudet.exists(ko => ko.globalPalveluroolit.contains(OppijanumerorekisteriRekisterinpitäjä) || ko.globalPalveluroolit.contains(OppijanumerorekisteriReadUpdate)) ||
    orgKäyttöoikeudet.exists(ko => ko.organisaatiokohtaisetPalveluroolit.contains(OppijanumerorekisteriRekisterinpitäjä) || ko.organisaatiokohtaisetPalveluroolit.contains(OppijanumerorekisteriReadUpdate))

  def hasRole(role: String): Boolean = {
    val palveluRooli = Palvelurooli("KOSKI", role)
    globalKäyttöoikeudet.exists(_.globalPalveluroolit.contains(palveluRooli)) ||
    globalViranomaisKäyttöoikeudet.exists(_.globalPalveluroolit.contains(palveluRooli)) ||
    orgKäyttöoikeudet.exists(_.organisaatiokohtaisetPalveluroolit.contains(palveluRooli))
  }

  def huollettavat: Either[HttpStatus, List[Huollettava]] = {
    if (user.isSuoritusjakoKatsominen) {
      Right(Nil)
    } else {
      user.huollettavat.collect {
        case haku: HuollettavienHakuOnnistui => Right(haku.huollettavat)
      }.getOrElse(Left(KoskiErrorCategory.unavailable.huollettavat()))
    }
  }

  def isUsersHuollettava(oid: String): Boolean = huollettavat.exists(_.exists(huollettava => huollettava.oid.contains(oid)))

  def juuriOrganisaatiot: List[OrganisaatioWithOid] = orgKäyttöoikeudet.collect { case r: KäyttöoikeusOrg if r.juuri => r.organisaatio }.toList

  // Filtteröi pois Valpas-käyttöoikeudet. Oikeampi vaihtoehto olisi filteröidä pois white listin perusteella mukaan vain käyttöoikeudet, joista
  // Koski on kiinnostunut. Sitä varten pitäisi koodia tutkimalla selvittää, mitä whitelistillä pitäisi olla. Se ei ole triviaalia, koska Koski
  // käyttää myös muita kuin oman palvelunsa käyttöoikeuksia tarkoituksella.
  // Sessio luodaan aina uudestaan jokaisessa API-kutsussa, joten käyttöoikeudet voi tallentaa lazy val:iin eikä hakea ja filteröida aina uudestaan
  private lazy val käyttöoikeudet: Set[Käyttöoikeus] = Käyttöoikeus.withPalveluroolitFilter(lähdeKäyttöoikeudet, _.palveluName != "VALPAS")

  Future(lähdeKäyttöoikeudet) // haetaan käyttöoikeudet toisessa säikeessä rinnakkain
}

object KoskiSpecificSession {
  def apply(user: AuthenticationUser, request: RichRequest, käyttöoikeudet: KäyttöoikeusRepository): KoskiSpecificSession = {
    new KoskiSpecificSession(user, UserLanguage.getLanguageFromCookie(request), LogUserContext.clientIpFromRequest(request), LogUserContext.userAgent(request), käyttöoikeudet.käyttäjänKäyttöoikeudet(user))
  }

  def huollettavaSession(huoltajaSession: KoskiSpecificSession, huollettava: OppijaHenkilö): KoskiSpecificSession = {
    val user = huoltajaSession.user.copy(oid = huollettava.oid, username = huollettava.oid, name = s"${huollettava.etunimet} ${huollettava.sukunimi}", huollettava = true)
    new KoskiSpecificSession(user, huoltajaSession.lang, huoltajaSession.clientIp, huoltajaSession.userAgent, huoltajaSession.kaikkiKäyttöoikeudet)
  }

  private val systemKäyttöoikeudet: Set[Käyttöoikeus] = Set(KäyttöoikeusGlobal(List(Palvelurooli(OPHPAAKAYTTAJA), Palvelurooli(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))))
  private val KOSKI_SYSTEM_USER: String = "Koski system user"
  val KOSKI_SYSTEM_USER_TALLENNETUT_YLIOPPILASTUTKINNON_OPISKELUOIKEUDET = "Koski system user tallennetut ylioppilastutkinnon opiskeluoikeudet"
  private val KOSKI_SYSTEM_USER_MITÄTÖIDYT_JA_POISTETUT: String = "Koski system user mitätöidyt ja poistetut"
  private val UNTRUSTED_SYSTEM_USER = "Koski untrusted system user"
  val SUORITUSJAKO_KATSOMINEN_USER = "Koski suoritusjako katsominen"
  // Internal user with root access
  val systemUser = new KoskiSpecificSession(
    AuthenticationUser(
      KOSKI_SYSTEM_USER,
      KOSKI_SYSTEM_USER,
      KOSKI_SYSTEM_USER, None
    ),
    "fi",
    InetAddress.getLoopbackAddress,
    "",
    systemKäyttöoikeudet
  )
  // Internal user with access to YO-opiskeluoikeudet
  val systemUserTallennetutYlioppilastutkinnonOpiskeluoikeudet = new KoskiSpecificSession(
    AuthenticationUser(
      KOSKI_SYSTEM_USER_TALLENNETUT_YLIOPPILASTUTKINNON_OPISKELUOIKEUDET,
      KOSKI_SYSTEM_USER_TALLENNETUT_YLIOPPILASTUTKINNON_OPISKELUOIKEUDET,
      KOSKI_SYSTEM_USER_TALLENNETUT_YLIOPPILASTUTKINNON_OPISKELUOIKEUDET, None
    ),
    "fi",
    InetAddress.getLoopbackAddress,
    "",
    systemKäyttöoikeudet ++
      Set(KäyttöoikeusGlobal(List(Palvelurooli(TALLENNETUT_YLIOPPILASTUTKINNON_OPISKELUOIKEUDET))))
  )
  // Internal user with root access to also mitätöidyt and poistetut opiskeluoikeudet
  val systemUserMitätöidytJaPoistetut = new KoskiSpecificSession(
    AuthenticationUser(
      KOSKI_SYSTEM_USER_MITÄTÖIDYT_JA_POISTETUT,
      KOSKI_SYSTEM_USER_MITÄTÖIDYT_JA_POISTETUT,
      KOSKI_SYSTEM_USER_MITÄTÖIDYT_JA_POISTETUT, None
    ),
    "fi",
    InetAddress.getLoopbackAddress,
    "",
    systemKäyttöoikeudet ++
      Set(KäyttöoikeusGlobal(List(Palvelurooli(MITATOIDYT_OPISKELUOIKEUDET), Palvelurooli(POISTETUT_OPISKELUOIKEUDET))))
  )

  val untrustedUser = new KoskiSpecificSession(AuthenticationUser(UNTRUSTED_SYSTEM_USER, UNTRUSTED_SYSTEM_USER, UNTRUSTED_SYSTEM_USER, None), "fi", InetAddress.getLoopbackAddress, "", Set())

  def suoritusjakoKatsominenUser(request: RichRequest) = new KoskiSpecificSession(AuthenticationUser(SUORITUSJAKO_KATSOMINEN_USER, SUORITUSJAKO_KATSOMINEN_USER, SUORITUSJAKO_KATSOMINEN_USER, None), UserLanguage.getLanguageFromCookie(request), LogUserContext.clientIpFromRequest(request), LogUserContext.userAgent(request), Set(KäyttöoikeusGlobal(List(Palvelurooli(OPHKATSELIJA)))))
}

