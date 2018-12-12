package fi.oph.koski.luovutuspalvelu

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.OppilaitoksenOpiskeluoikeudet
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.omattiedot.OmatTiedotEditorModel
import fi.oph.koski.schema._

class SuomiFiService(application: KoskiApplication) extends Logging {
  def suomiFiOpiskeluoikeudet(hetu: String)(implicit user: KoskiSession): Either[HttpStatus, SuomiFiResponse] =
    application.oppijaFacade.findOppijaByHetuOrCreateIfInYtrOrVirta(hetu)
      .flatMap(_.warningsToLeft)
      .map(OmatTiedotEditorModel.piilotaKeskeneräisetPerusopetuksenPäättötodistukset)
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

  // Duplicates the logic from web/app/suoritus/OpiskeluoikeusEditor.jsx#suorituksenOtsikko
  private def suorituksenNimi(oo: Opiskeluoikeus) = {
    val suoritukset = oo.suoritukset.filterNot(_.tyyppi == suoritusTyyppi("perusopetuksenvuosiluokka"))
    suoritukset
      .headOption.map(otsikko(suoritukset))
      .getOrElse(localization("Perusopetus"))
  }

  private def otsikko(kaikkiSuoritukset: List[PäätasonSuoritus])(suoritus: PäätasonSuoritus) = {
    val sisältääMontaOppimäärääTaiOpintojaksoa = kaikkiSuoritukset.length > 1 && kaikkiSuoritukset.forall(s => isOppiaineenOppimäärä(s) || isOpintojakso(s))
    if (sisältääMontaOppimäärääTaiOpintojaksoa) {
      oppimääräTaiOpintojaksoOtsikko(kaikkiSuoritukset)
    } else {
      val title = suoritus.koulutusmoduuli match {
        case a: AmmatillinenTutkintoKoulutus => a.perusteenNimi.get
        case x => x.tunniste.getNimi.get
      }
      if (suoritus.tyyppi == suoritusTyyppi("ammatillinentutkintoosittainen")) {
        title.concat(localization(", osittainen"))
      } else if (suoritus.tyyppi == suoritusTyyppi("aikuistenperusopetuksenoppimaara")) {
        suoritus.tyyppi.nimi.get
      } else {
        title
      }
    }
  }

  private def oppimääräTaiOpintojaksoOtsikko(kaikkiSuoritukset: List[PäätasonSuoritus]) = {
    val otsikkoKey = if (kaikkiSuoritukset.forall(isOppiaineenOppimäärä)) "oppiainetta" else "opintojaksoa"
    LocalizedString.finnish(s"${kaikkiSuoritukset.length} ").concat(localization(otsikkoKey))
  }

  private def isOpintojakso(suoritus: PäätasonSuoritus) =
    suoritus.tyyppi == suoritusTyyppi("korkeakoulunopintojakso")

  private def isOppiaineenOppimäärä(suoritus: PäätasonSuoritus) =
    suoritus.tyyppi == suoritusTyyppi("perusopetuksenoppiaineenoppimaara") ||
    suoritus.tyyppi == suoritusTyyppi("nuortenperusopetuksenoppiaineenoppimaara")

  private def emptyIfNotFound(error: HttpStatus) =
    if (error.errors.exists(_.key == KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia.key)) {
      Right(SuomiFiResponse(Nil))
    } else {
      Left(error)
    }

  private def localization(key: String) = application.localizationRepository.get(key)
  private def suoritusTyyppi(koodiarvo: String) = application.koodistoViitePalvelu.validateRequired("suorituksentyyppi", koodiarvo)
}

case class SuomiFiOpiskeluoikeus(tila: Option[LocalizedString], alku: Option[LocalDate], loppu: Option[LocalDate], nimi: LocalizedString)
case class SuomiFiOppilaitos(nimi: LocalizedString, opiskeluoikeudet: List[SuomiFiOpiskeluoikeus])
case class SuomiFiResponse(oppilaitokset: List[SuomiFiOppilaitos])
