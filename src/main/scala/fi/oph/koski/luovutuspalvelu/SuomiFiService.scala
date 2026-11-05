package fi.oph.koski.luovutuspalvelu

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.OppilaitoksenOpiskeluoikeudet
import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage, KoskiAuditLogMessageField, KoskiOperation, Logging}
import fi.oph.koski.omattiedot.OmatTiedotEditorModel
import fi.oph.koski.schema._

class SuomiFiService(application: KoskiApplication) extends Logging {
  import SuomiFiService._

  // Palautettavat tiedot rajataan tuettujen tyyppien listalla, ei kutsujan koulutusmuotorooleilla: haku tehdään
  // OPH-katselijan järjestelmäsessiolla, vastaavasti kuin Kela- ja SDG-rajapinnoissa. Kutsujan sessiota
  // käytetään vain auditlokitukseen.
  private val systemSession: KoskiSpecificSession = KoskiSpecificSession.systemKatselijaUser

  def suomiFiOpiskeluoikeudet(hetu: String)(implicit user: KoskiSpecificSession): Either[HttpStatus, SuomiFiResponse] = {
    val result = for {
      henkilö <- application.henkilöRepository.findByHetuOrCreateIfInYtrOrVirta(hetu).toRight(notFound)
      opiskeluoikeudet <- haePalautettavatOpiskeluoikeudet(henkilö)
    } yield {
      auditLogKatsominen(henkilö.oid)
      val oppija = Oppija(application.henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot(henkilö), opiskeluoikeudet)
      convertToSuomiFi(OmatTiedotEditorModel.opiskeluoikeudetOppilaitoksittain(oppija))
    }
    result.left.flatMap(emptyIfNotFound)
  }

  private def haePalautettavatOpiskeluoikeudet(henkilö: OppijaHenkilö): Either[HttpStatus, Seq[Opiskeluoikeus]] =
    application.opiskeluoikeusRepository
      .findByOppija(henkilö, useVirta = true, useYtr = true)(systemSession)
      .warningsToLeft
      .map(palautettavatOpiskeluoikeudet)
      .filterOrElse(_.nonEmpty, notFound)

  private def notFound: HttpStatus = KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia()

  private def convertToSuomiFi(oppilaitosOpiskeluoikeudet: List[OppilaitoksenOpiskeluoikeudet]) =
    SuomiFiResponse(oppilaitosOpiskeluoikeudet.map(toSuomiFiOpiskeluoikeus))

  private def toSuomiFiOpiskeluoikeus(oos: OppilaitoksenOpiskeluoikeudet) =
    SuomiFiOppilaitos(oos.oppilaitos.nimi.get, oos.opiskeluoikeudet.map { oo =>
      SuomiFiOpiskeluoikeus(
        tila = oo.tila.opiskeluoikeusjaksot.lastOption.map(_.tila.nimi.get),
        alku = oo.alkamispäivä,
        loppu = oo.päättymispäivä,
        nimi = suorituksenNimi(oo)
      )
    })

  // Duplicates the logic from web/app/suoritus/OpiskeluoikeusEditor.jsx#näytettäväPäätasonSuoritusTitle
  private def suorituksenNimi(oo: Opiskeluoikeus) = {
    def pääSuoritus = oo.suoritukset.head
    if (pelkkiäVuosiluokkia(oo)) {
      localization("Perusopetus")
    } else if (pelkkiäOpintojaksoja(oo)) {
      opintojaksojaOtsikko(oo.suoritukset)
    } else if (sisältääLukionOppiaineenOppimäärän(oo)) {
      localization("Lukion oppiaineen oppimäärä")
    } else if (sisältääPerusopetuksenOppiaineeinOppimäärän(oo)) {
      localization("Perusopetuksen oppiaineen oppimäärä")
    } else if (aikuistenPerusopetus(pääSuoritus)) {
      pääSuoritus.tyyppi.nimi.get
    } else if (ammatillinenTutkintoOsittainen(pääSuoritus)) {
      suoritusNimi(pääSuoritus).concat(localization(", osittainen"))
    } else {
      suoritusNimi(oo.suoritukset.find(isKorkeakoulututkinto).getOrElse(pääSuoritus))
    }
  }

  private def ammatillinenTutkintoOsittainen(suoritus: PäätasonSuoritus) =
    suoritus.tyyppi == suoritusTyyppi("ammatillinentutkintoosittainen")

  private def aikuistenPerusopetus(suoritus: PäätasonSuoritus) =
    suoritus.tyyppi == suoritusTyyppi("aikuistenperusopetuksenoppimaara")

  private def pelkkiäVuosiluokkia(oo: Opiskeluoikeus) =
    oo.suoritukset.forall(_.tyyppi == suoritusTyyppi("perusopetuksenvuosiluokka"))

  private def pelkkiäOpintojaksoja(oo: Opiskeluoikeus) =
    oo.suoritukset.length > 1 && oo.suoritukset.forall(isOpintojakso)

  private def sisältääLukionOppiaineenOppimäärän(oo: Opiskeluoikeus) =
    oo.suoritukset.exists(isLukionOppiaineenOppimäärä)

  private def sisältääPerusopetuksenOppiaineeinOppimäärän(oo: Opiskeluoikeus) =
    oo.suoritukset.exists(isPerusopetuksenoppiaineenOppimäärä)

  private def opintojaksojaOtsikko(kaikkiSuoritukset: List[PäätasonSuoritus]) =
    LocalizedString.finnish(s"${kaikkiSuoritukset.size} ").concat(localization("opintojaksoa"))

  private def suoritusNimi(suoritus: PäätasonSuoritus): LocalizedString =
    suoritus.koulutusmoduuli match {
      case a: AmmatillinenTutkintoKoulutus => a.perusteenNimi.getOrElse(a.tunniste.getNimi.get)
      case k => k.tunniste.getNimi.get
    }

  private def isKorkeakoulututkinto(suoritus: PäätasonSuoritus) =
    suoritus.tyyppi == suoritusTyyppi("korkeakoulututkinto") || suoritus.tyyppi == suoritusTyyppi("muukorkeakoulunsuoritus")

  private def isOpintojakso(suoritus: PäätasonSuoritus) =
    suoritus.tyyppi == suoritusTyyppi("korkeakoulunopintojakso")

  private def isLukionOppiaineenOppimäärä(suoritus: PäätasonSuoritus) =
    suoritus.tyyppi == suoritusTyyppi("lukionoppiaineenoppimaara")

  private def isPerusopetuksenoppiaineenOppimäärä(suoritus: PäätasonSuoritus) =
    suoritus.tyyppi == suoritusTyyppi("perusopetuksenoppiaineenoppimaara") ||
    suoritus.tyyppi == suoritusTyyppi("nuortenperusopetuksenoppiaineenoppimaara")

  private def emptyIfNotFound(error: HttpStatus) =
    if (error.errors.exists(_.key == KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia.key)) {
      Right(SuomiFiResponse(Nil))
    } else {
      Left(error)
    }

  private def auditLogKatsominen(oppijaOid: String)(implicit user: KoskiSpecificSession): Unit =
    AuditLog.log(KoskiAuditLogMessage(
      KoskiOperation.KANSALAINEN_SUOMIFI_KATSOMINEN,
      user,
      Map(KoskiAuditLogMessageField.oppijaHenkiloOid -> oppijaOid)
    ))

  private def localization(key: String) = application.koskiLocalizationRepository.get(key)
  private def suoritusTyyppi(koodiarvo: String) = application.koodistoViitePalvelu.validateRequired("suorituksentyyppi", koodiarvo)
}

object SuomiFiService {
  val palautettavatOpiskeluoikeustyypit: Set[Koodistokoodiviite] = Set(
    OpiskeluoikeudenTyyppi.aikuistenperusopetus,
    OpiskeluoikeudenTyyppi.ammatillinenkoulutus,
    OpiskeluoikeudenTyyppi.diatutkinto,
    OpiskeluoikeudenTyyppi.ebtutkinto,
    OpiskeluoikeudenTyyppi.esiopetus,
    OpiskeluoikeudenTyyppi.europeanschoolofhelsinki,
    OpiskeluoikeudenTyyppi.ibtutkinto,
    OpiskeluoikeudenTyyppi.internationalschool,
    OpiskeluoikeudenTyyppi.korkeakoulutus,
    OpiskeluoikeudenTyyppi.lukiokoulutus,
    OpiskeluoikeudenTyyppi.luva,
    OpiskeluoikeudenTyyppi.perusopetukseenvalmistavaopetus,
    OpiskeluoikeudenTyyppi.perusopetuksenlisaopetus,
    OpiskeluoikeudenTyyppi.perusopetus,
    OpiskeluoikeudenTyyppi.tuva,
    OpiskeluoikeudenTyyppi.vapaansivistystyonkoulutus,
    OpiskeluoikeudenTyyppi.ylioppilastutkinto,
  )

  val eiPalautettavatPäätasonSuoritustyypit: Set[Koodistokoodiviite] = Set(
    SuorituksenTyyppi.vstvapaatavoitteinenkoulutus,
  )

  private val palautettavatOpiskeluoikeusKoodiarvot = palautettavatOpiskeluoikeustyypit.map(_.koodiarvo)
  private val eiPalautettavatSuoritusKoodiarvot = eiPalautettavatPäätasonSuoritustyypit.map(_.koodiarvo)

  def palautettavatOpiskeluoikeudet(opiskeluoikeudet: Seq[Opiskeluoikeus]): Seq[Opiskeluoikeus] =
    opiskeluoikeudet
      .filter(oo => palautettavatOpiskeluoikeusKoodiarvot.contains(oo.tyyppi.koodiarvo))
      .map(oo => oo.withSuoritukset(oo.suoritukset.filterNot(s => eiPalautettavatSuoritusKoodiarvot.contains(s.tyyppi.koodiarvo))))
      .filter(_.suoritukset.nonEmpty)
}

case class SuomiFiOpiskeluoikeus(tila: Option[LocalizedString], alku: Option[LocalDate], loppu: Option[LocalDate], nimi: LocalizedString)
case class SuomiFiOppilaitos(nimi: LocalizedString, opiskeluoikeudet: List[SuomiFiOpiskeluoikeus])
case class SuomiFiResponse(oppilaitokset: List[SuomiFiOppilaitos])
