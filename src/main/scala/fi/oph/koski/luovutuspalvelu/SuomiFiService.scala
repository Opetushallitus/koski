package fi.oph.koski.luovutuspalvelu

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.OppilaitoksenOpiskeluoikeudet
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.omattiedot.OmatTiedotEditorModel
import fi.oph.koski.schema.{AmmatillinenTutkintoKoulutus, LocalizedString, Opiskeluoikeus}

class SuomiFiService(koskiApplication: KoskiApplication) extends Logging {
  def suomiFiOpiskeluoikeudet(hetu: String)(implicit user: KoskiSession): Either[HttpStatus, SuomiFiResponse] =
    koskiApplication.oppijaFacade.findOppijaByHetuOrCreateIfInYtrOrVirta(hetu).flatMap(_.warningsToLeft)
      .map(OmatTiedotEditorModel.opiskeluoikeudetOppilaitoksittain)
      .map(convertToSuomiFi)
      .map(SuomiFiResponse)

  private def convertToSuomiFi(oppilaitosOpiskeluoikeudet: List[OppilaitoksenOpiskeluoikeudet]) =
    oppilaitosOpiskeluoikeudet.flatMap(toSuomiFiOpiskeluoikeus)

  private def toSuomiFiOpiskeluoikeus(oos: OppilaitoksenOpiskeluoikeudet) = try {
    Some(SuomiFiOppilaitos(oos.oppilaitos.nimi.get, oos.opiskeluoikeudet.map { oo =>
      SuomiFiOpiskeluoikeus(
        tila = oo.tila.opiskeluoikeusjaksot.last.tila.nimi.get,
        alku = oo.alkamispäivä.get,
        loppu = oo.päättymispäivä,
        nimi = suorituksenNimi(oo).get
      )
    }))
  } catch {
    case e: NoSuchElementException =>
      logger.error(e)("Suomi.fi opiskeluoikeuden konvertointi epäonnistui")
      None
  }

  private def suorituksenNimi(oo: Opiskeluoikeus) =
    oo.suoritukset.head.koulutusmoduuli match {
      case a: AmmatillinenTutkintoKoulutus => a.perusteenNimi
      case x => x.tunniste.getNimi
    }
}

case class SuomiFiOpiskeluoikeus(tila: LocalizedString, alku: LocalDate, loppu: Option[LocalDate], nimi: LocalizedString)
case class SuomiFiOppilaitos(nimi: LocalizedString, opiskeluoikeudet: List[SuomiFiOpiskeluoikeus])
case class SuomiFiResponse(oppilaitokset: List[SuomiFiOppilaitos])

//TODO: PIILOTA KESKENERAISET PERUSOPEUTS
//TODO: PERUSOPETUKSEN TITLEN KAIVAMINEN
