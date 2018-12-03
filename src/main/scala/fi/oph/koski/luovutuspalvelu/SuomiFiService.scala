package fi.oph.koski.luovutuspalvelu

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.OppilaitoksenOpiskeluoikeudet
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.omattiedot.OmatTiedotEditorModel
import fi.oph.koski.schema.{AmmatillinenTutkintoKoulutus, LocalizedString, Opiskeluoikeus}

class SuomiFiService(koskiApplication: KoskiApplication) {
  def suomiFiOpiskeluoikeudet(hetu: String)(implicit user: KoskiSession): Either[HttpStatus, SuomiFiResponse] =
    koskiApplication.oppijaFacade.findOppijaByHetuOrCreateIfInYtrOrVirta(hetu).flatMap(_.warningsToLeft)
      .map(OmatTiedotEditorModel.opiskeluoikeudetOppilaitoksittain)
      .map(convertToSuomiFi)
      .map(SuomiFiResponse)

  private def convertToSuomiFi(oppilaitosOpiskeluoikeudet: List[OppilaitoksenOpiskeluoikeudet]) = oppilaitosOpiskeluoikeudet.map { oos =>
    SuomiFiOppilaitos(oos.oppilaitos.nimi.get, oos.opiskeluoikeudet.map { oo =>
      SuomiFiOpiskeluoikeus(oo.tila.opiskeluoikeusjaksot.last.tila.nimi.get, oo.alkamispäivä.get, oo.päättymispäivä, suorituksenNimi(oo).getOrElse(oo.tyyppi.nimi.get))
    })
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
