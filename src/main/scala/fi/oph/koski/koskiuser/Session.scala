package fi.oph.koski.koskiuser

import fi.oph.koski.executors.GlobalExecutionContext
import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.huoltaja.{Huollettava, HuollettavienHakuOnnistui}
import fi.oph.koski.json.SensitiveDataAllowed
import fi.oph.koski.koskiuser.Rooli._
import fi.oph.koski.log.{LogUserContext, Loggable, Logging}
import fi.oph.koski.organisaatio.{Opetushallitus, OrganisaatioRepository}
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

  lazy val varhaiskasvatuksenOstopalvelukäyttöoikeudet: Set[KäyttöoikeusVarhaiskasvatuksenOstopalveluihinMuistaOrganisaatioista] = käyttöoikeudet.collect { case k: KäyttöoikeusVarhaiskasvatuksenOstopalveluihinMuistaOrganisaatioista => k }
  lazy val varhaiskasvatusKoulutustoimijat: Set[Oid] = varhaiskasvatuksenOstopalvelukäyttöoikeudet.map(_.ostavaKoulutustoimija.oid)
  lazy val hasKoulutustoimijaVarhaiskasvatuksenJärjestäjäAccess: Boolean = varhaiskasvatusKoulutustoimijat.nonEmpty
  lazy val allowedOpiskeluoikeudetJaPäätasonSuoritukset: Set[OoPtsMask] = if (isRoot) OpiskeluoikeudenTyyppi.kaikkiOpiskeluoikeudetJaPäätasonSuoritukset else käyttöoikeudet.flatMap(_.allowedOpiskeluoikeusTyypit)
  lazy val deniedOpiskeluoikeudetJaPäätasonSuoritukset: Set[OoPtsMask] = OpiskeluoikeudenTyyppi.kaikkiOpiskeluoikeudetJaPäätasonSuoritukset -- allowedOpiskeluoikeudetJaPäätasonSuoritukset
  lazy val hasKoulutusmuotoRestrictions: Boolean = !allowedOpiskeluoikeudetJaPäätasonSuoritukset.satisfiesAll(OpiskeluoikeudenTyyppi.kaikkiOpiskeluoikeudetJaPäätasonSuoritukset)
  lazy val allowedPäätasonSuorituksenTyypit: Set[String] = allowedOpiskeluoikeudetJaPäätasonSuoritukset.flatMap(_.päätasonSuoritukset).flatten
  lazy val hasPäätasonsuoritusRestrictions: Boolean = allowedPäätasonSuorituksenTyypit.nonEmpty
  lazy val kaikkiKäyttöoikeudet: Set[Käyttöoikeus] = käyttöoikeudet

  def organisationOids(accessType: AccessType.Value): Set[String] = orgKäyttöoikeudet.collect { case k: KäyttöoikeusOrg if k.organisaatioAccessType.contains(accessType) => k.organisaatio.oid }
  lazy val globalAccess = globalKäyttöoikeudet.flatMap { _.globalAccessType }
  def isRoot = globalAccess.contains(AccessType.write)
  def isPalvelukäyttäjä = orgKäyttöoikeudet.flatMap(_.organisaatiokohtaisetPalveluroolit).contains(Palvelurooli(TIEDONSIIRTO)) || isKielitutkintorekisteri
  def hasReadAccess(organisaatio: Organisaatio.Oid, koulutustoimija: Option[Organisaatio.Oid]) = hasAccess(organisaatio, koulutustoimija, AccessType.read)

  def hasRaporttiReadAccess(organisaatio: Organisaatio.Oid): Boolean = {
    hasReadAccess(organisaatio, None) ||
      varhaiskasvatuksenOstopalvelukäyttöoikeudet
        .exists(oikeus =>
          oikeus.ostajanUlkopuolisetVarhaiskasvatusToimipisteet.exists(oid => oid == organisaatio && hasReadAccess(organisaatio, Some(oikeus.ostavaKoulutustoimija.oid)))
        )
  }

  def hasWriteAccess(organisaatio: Organisaatio.Oid, koulutustoimija: Option[Organisaatio.Oid]) = hasAccess(organisaatio, koulutustoimija, AccessType.write) && hasRole(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)
  def hasTiedonsiirronMitätöintiAccess(organisaatio: Organisaatio.Oid, koulutustoimija: Option[Organisaatio.Oid]) = hasAccess(organisaatio, koulutustoimija, AccessType.tiedonsiirronMitätöinti)
  def hasKäyttöliittymäsiirronMitätöintiAccess(organisaatio: Organisaatio.Oid, koulutustoimija: Option[Organisaatio.Oid]) = hasAccess(organisaatio, koulutustoimija, AccessType.käyttöliittymäsiirronMitätöinti)
  def hasLähdejärjestelmäkytkennänPurkaminenAccess(organisaatio: Organisaatio.Oid, koulutustoimija: Option[Organisaatio.Oid]): Boolean = hasAccess(organisaatio, koulutustoimija, AccessType.lähdejärjestelmäkytkennänPurkaminen)

  // HUOM!
  // Kun lisäät uuden luovutuspalvelukäyttöoikeuden alle, muista lisätä se myös
  // KoskiSpecificAuthenticationSupport.requireVirkailijaOrPalvelukäyttäjä -metodiin
  def hasHSLAccess: Boolean = globalViranomaisKäyttöoikeudet.flatMap(_.globalPalveluroolit).contains(Palvelurooli("KOSKI", HSL))
  def hasSuomiFiAccess: Boolean = globalViranomaisKäyttöoikeudet.flatMap(_.globalPalveluroolit).contains(Palvelurooli("KOSKI", SUOMIFI))
  def hasTilastokeskusAccess: Boolean = globalViranomaisKäyttöoikeudet.flatMap(_.globalPalveluroolit).contains(Palvelurooli("KOSKI", TILASTOKESKUS))
  def hasMitätöidytOpiskeluoikeudetAccess: Boolean = hasTilastokeskusAccess || hasYtlAccess || globalKäyttöoikeudet.exists(_.globalPalveluroolit.exists(_.rooli == MITATOIDYT_OPISKELUOIKEUDET))
  def hasPoistetutOpiskeluoikeudetAccess: Boolean = globalKäyttöoikeudet.exists(_.globalPalveluroolit.exists(_.rooli == POISTETUT_OPISKELUOIKEUDET))
  def hasValviraAccess: Boolean = globalViranomaisKäyttöoikeudet.flatMap(_.globalPalveluroolit).contains(Palvelurooli("KOSKI", VALVIRA))
  def hasMigriAccess: Boolean = globalViranomaisKäyttöoikeudet.flatMap(_.globalPalveluroolit).contains(Palvelurooli("KOSKI", MIGRI))
  def hasKelaAccess: Boolean = !globalViranomaisKäyttöoikeudet.flatMap(_.globalPalveluroolit).intersect(Set(Palvelurooli("KOSKI", LUOTTAMUKSELLINEN_KELA_LAAJA), Palvelurooli("KOSKI", LUOTTAMUKSELLINEN_KELA_SUPPEA))).isEmpty
  def hasYtlAccess: Boolean = globalViranomaisKäyttöoikeudet.flatMap(_.globalPalveluroolit).contains(Palvelurooli("KOSKI", YTL))
  def hasSdgAccess: Boolean = globalViranomaisKäyttöoikeudet.exists(_.globalPalveluroolit.contains(Palvelurooli("KOSKI", SDG)))

  // OAuth2-käyttäjillä on oikeudet tiettyihin OAuth2-scopeihin, siksi käsittely poikkeaa muista luovutuspalvelukäyttäjistä, jotka ovat viranomaisia.
  def hasSomeOmaDataOAuth2Access: Boolean = {
    globalKäyttöoikeudet
      .flatMap(_.globalPalveluroolit)
      .exists(p => p.palveluName == "KOSKI" && p.rooli.startsWith(omadataOAuth2Prefix))
  }
  def omaDataOAuth2Scopes: Set[String] = {
    globalKäyttöoikeudet
      .flatMap(_.globalPalveluroolit)
      .filter(_.palveluName == "KOSKI")
      .flatMap(_.toOmaDataOAuth2Scope())
  }
  // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  // HUOM!
  // Kun lisäät uuden luovutuspalvelukäyttöoikeuden ylle, muista lisätä se myös
  // KoskiSpecificAuthenticationSupport.requireVirkailijaOrPalvelukäyttäjä -metodiin

  def hasVarhaiskasvatusAccess(koulutustoimijaOid: Organisaatio.Oid, organisaatioOid: Organisaatio.Oid, accessType: AccessType.Value): Boolean = {
    val onOikeusKoulutustoimijanKautta = varhaiskasvatuksenOstopalvelukäyttöoikeudet.exists(
      k => k.ostavaKoulutustoimija.oid == koulutustoimijaOid &&
        k.organisaatioAccessType.contains(accessType) && k.ostajanUlkopuolisetVarhaiskasvatusToimipaikat.contains(organisaatioOid)
    )

    globalAccess.contains(accessType) || onOikeusKoulutustoimijanKautta
  }

  def hasTallennetutYlioppilastutkinnonOpiskeluoikeudetAccess: Boolean =
    globalKäyttöoikeudet.exists(_.globalPalveluroolit.contains(Palvelurooli(TALLENNETUT_YLIOPPILASTUTKINNON_OPISKELUOIKEUDET)))

  def hasKiosAccess: Boolean = globalKäyttöoikeudet.exists(_.globalPalveluroolit.contains(Palvelurooli(KIOS))) ||
    globalKäyttöoikeudet.exists(_.globalPalveluroolit.contains(Palvelurooli(VKT))) // TODO: TOR-2471: Poista kun VKT-rooli on korvattu KIOS-roolilla
  def hasHakemuspalveluAccess: Boolean = globalKäyttöoikeudet.exists(_.globalPalveluroolit.contains(Palvelurooli(HAKEMUSPALVELU_API)))

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

  def hasKielitutkintoAccess(organisaatio: Organisaatio.Oid, koulutustoimija: Option[Organisaatio.Oid], accessType: AccessType.Value): Boolean =
    hasAccess(organisaatio, koulutustoimija, accessType) || (isKielitutkintorekisteri && globalAccess.contains(accessType))

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
  def hasAnyMitätöintiAccess = globalAccess.contains(AccessType.tiedonsiirronMitätöinti) || globalAccess.contains(AccessType.käyttöliittymäsiirronMitätöinti) || organisationOids(AccessType.tiedonsiirronMitätöinti).nonEmpty || organisationOids(AccessType.käyttöliittymäsiirronMitätöinti).nonEmpty
  def hasRaportitAccess = hasAnyReadAccess && hasRole(LUOTTAMUKSELLINEN_KAIKKI_TIEDOT) && !hasGlobalKoulutusmuotoReadAccess
  def sensitiveDataAllowed(allowedRoles: Set[Role]) = allowedRoles.exists(hasRole)
  def hasAnyLähdejärjestelmäkytkennänPurkaminenAccess = globalAccess.contains(AccessType.lähdejärjestelmäkytkennänPurkaminen) || organisationOids(AccessType.lähdejärjestelmäkytkennänPurkaminen).nonEmpty
  def isKielitutkintorekisteri: Boolean = globalKäyttöoikeudet.exists(_.globalPalveluroolit.contains(Palvelurooli("KOSKI", KIELITUTKINTOREKISTERI)))

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
  def prefetchKäyttöoikeudet(): Unit = {
    Future(lähdeKäyttöoikeudet)
  }
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
  val OAUTH2_KATSOMINEN_USER = "Koski OAuth2 katsominen"
  val OPH_KATSELIJA_USER = "Koski OPH katselija"
  // Internal user with root access
  val systemUser = new KoskiSpecificSession(
    AuthenticationUser(
      Opetushallitus.organisaatioOid,
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
  def suoritusjakoKatsominenErityisilläHenkilötiedoillaUser(request: RichRequest) = new KoskiSpecificSession(AuthenticationUser(SUORITUSJAKO_KATSOMINEN_USER, SUORITUSJAKO_KATSOMINEN_USER, SUORITUSJAKO_KATSOMINEN_USER, None), UserLanguage.getLanguageFromCookie(request), LogUserContext.clientIpFromRequest(request), LogUserContext.userAgent(request), Set(KäyttöoikeusGlobal(List(Palvelurooli(OPHKATSELIJA), Palvelurooli(SUORITUSJAKO_KATSELIJA)))))
  def ophKatselijaUser(request: RichRequest) = new KoskiSpecificSession(AuthenticationUser(OPH_KATSELIJA_USER, OPH_KATSELIJA_USER, OPH_KATSELIJA_USER, None), UserLanguage.getLanguageFromCookie(request), LogUserContext.clientIpFromRequest(request), LogUserContext.userAgent(request), Set(KäyttöoikeusGlobal(List(Palvelurooli(OPHKATSELIJA)))))
  def oauth2KatsominenUser(request: RichRequest) = new KoskiSpecificSession(AuthenticationUser(OAUTH2_KATSOMINEN_USER, OAUTH2_KATSOMINEN_USER, OAUTH2_KATSOMINEN_USER, None), UserLanguage.getLanguageFromCookie(request), LogUserContext.clientIpFromRequest(request), LogUserContext.userAgent(request), Set(KäyttöoikeusGlobal(List(Palvelurooli(OPHKATSELIJA)))))
}
