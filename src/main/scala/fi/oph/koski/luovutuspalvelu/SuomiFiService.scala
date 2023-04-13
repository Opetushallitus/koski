package fi.oph.koski.luovutuspalvelu

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.OppilaitoksenOpiskeluoikeudet
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.omattiedot.OmatTiedotEditorModel
import fi.oph.koski.schema._

class SuomiFiService(application: KoskiApplication) extends Logging {
  def suomiFiOpiskeluoikeudet(hetu: String)(implicit user: KoskiSpecificSession): Either[HttpStatus, SuomiFiResponse] =
    application.oppijaFacade.findOppijaByHetuOrCreateIfInYtrOrVirta(hetu)
      .flatMap(_.warningsToLeft)
      .map(OmatTiedotEditorModel.opiskeluoikeudetOppilaitoksittain)
      .map(convertToSuomiFi)
      .left.flatMap(emptyIfNotFound)

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

  private def localization(key: String) = application.koskiLocalizationRepository.get(key)
  private def suoritusTyyppi(koodiarvo: String) = application.koodistoViitePalvelu.validateRequired("suorituksentyyppi", koodiarvo)
}

case class SuomiFiOpiskeluoikeus(tila: Option[LocalizedString], alku: Option[LocalDate], loppu: Option[LocalDate], nimi: LocalizedString)
case class SuomiFiOppilaitos(nimi: LocalizedString, opiskeluoikeudet: List[SuomiFiOpiskeluoikeus])
case class SuomiFiResponse(oppilaitokset: List[SuomiFiOppilaitos])
