package fi.oph.koski.luovutuspalvelu

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.OppilaitoksenOpiskeluoikeudet
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.omattiedot.OmatTiedotEditorModel
import fi.oph.koski.schema.{AmmatillinenTutkintoKoulutus, Koodistokoodiviite, LocalizedString, Opiskeluoikeus}

class SuomiFiService(application: KoskiApplication) extends Logging {
  // TODO: lang
  def suomiFiOpiskeluoikeudet(hetu: String)(implicit user: KoskiSession): Either[HttpStatus, SuomiFiResponse] =
    application.oppijaFacade.findOppijaByHetuOrCreateIfInYtrOrVirta(hetu).flatMap(_.warningsToLeft)
      .map(OmatTiedotEditorModel.piilotaKeskeneräisetPerusopetuksenPäättötodistukset)
      .map(OmatTiedotEditorModel.opiskeluoikeudetOppilaitoksittain)
      .map(convertToSuomiFi)
      .map(SuomiFiResponse)

  private def convertToSuomiFi(oppilaitosOpiskeluoikeudet: List[OppilaitoksenOpiskeluoikeudet]) =
    oppilaitosOpiskeluoikeudet.flatMap(toSuomiFiOpiskeluoikeus)

  private def toSuomiFiOpiskeluoikeus(oos: OppilaitoksenOpiskeluoikeudet) = try {
    Some(SuomiFiOppilaitos(oos.oppilaitos.nimi.get, oos.opiskeluoikeudet.map { oo =>
      SuomiFiOpiskeluoikeus(
        tila = oo.tila.opiskeluoikeusjaksot.lastOption.map(_.tila.nimi.get),
        alku = oo.alkamispäivä,
        loppu = oo.päättymispäivä,
        nimi = suorituksenNimi(oo)
      )
    }))
  } catch {
    case e: NoSuchElementException =>
      logger.error(e)("Suomi.fi opiskeluoikeuden konvertointi epäonnistui")
      None
  }

  private def suorituksenNimi(oo: Opiskeluoikeus) = {
    val suoritus = oo.suoritukset.head
    val title = suoritus.koulutusmoduuli match {
      case a: AmmatillinenTutkintoKoulutus => a.perusteenNimi.get
      case x => x.tunniste.getNimi.get
    }
    if (suoritus.tyyppi ==  ammatillinenOsittainen) {
      title.concat(application.localizationRepository.get(", osittainen"))
    } else if (suoritus.tyyppi == aikuistenPerusopetus) {
      suoritus.tyyppi.nimi.get
    } else {
      title
    }
  }

  private val ammatillinenOsittainen = Koodistokoodiviite("ammatillinentutkintoosittainen", "suorituksentyyppi")
  private val aikuistenPerusopetus = Koodistokoodiviite("aikuistenperusopetuksenoppimaara", "suorituksentyyppi")
}

case class SuomiFiOpiskeluoikeus(tila: Option[LocalizedString], alku: Option[LocalDate], loppu: Option[LocalDate], nimi: LocalizedString)
case class SuomiFiOppilaitos(nimi: LocalizedString, opiskeluoikeudet: List[SuomiFiOpiskeluoikeus])
case class SuomiFiResponse(oppilaitokset: List[SuomiFiOppilaitos])
