package fi.oph.koski.luovutuspalvelu

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.{OppijaEditorModel, OppilaitoksenOpiskeluoikeudet}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.omattiedot.OmatTiedotEditorModel
import fi.oph.koski.schema.{AmmatillinenTutkintoKoulutus, LocalizedString, Opiskeluoikeus, Oppija}


class SuomiFiService(koskiApplication: KoskiApplication) {

 def suomiFiOpiskeluoikeudet(hetu: String)(implicit user: KoskiSession): Either[HttpStatus, SuomiFiResponse] = {
  val oppija = koskiApplication.oppijaFacade.findOppijaByHetuOrCreateIfInYtrOrVirta(hetu, useVirta = true, useYtr = true)
   oppija.flatMap(_.warningsToLeft)
     .map(OmatTiedotEditorModel.opiskeluoikeudetOppilaitoksittain)
     .map(_.map(convertToSuomiFi))
     .map(SuomiFiResponse)
 }

  private def convertToSuomiFi(oos: OppilaitoksenOpiskeluoikeudet) = {
    val nimi = oos.oppilaitos.nimi.get
    val opiskeluoikeudet: List[SuomiFiOpiskeluoikeus] = oos.opiskeluoikeudet.map(o => SuomiFiOpiskeluoikeus(o.tila.opiskeluoikeusjaksot.last.tila.nimi.get, o.alkamispäivä.get, o.päättymispäivä, getSuorituksenNimi(o)))
    SuomiFiOppilaitos(nimi, opiskeluoikeudet)
  }

  private def getSuorituksenNimi(oo: Opiskeluoikeus) = {
    oo.suoritukset.head.koulutusmoduuli match {
      case a: AmmatillinenTutkintoKoulutus => a.perusteenNimi.get
      case x => x.tunniste.getNimi.get
    }
  }
}

case class SuomiFiOpiskeluoikeus(tila: LocalizedString, alku: LocalDate, loppu: Option[LocalDate], nimi: LocalizedString)
case class SuomiFiOppilaitos(nimi: LocalizedString, opiskeluoikeudet: List[SuomiFiOpiskeluoikeus])
case class SuomiFiResponse(oppilaitokset: List[SuomiFiOppilaitos])

//TODO: PIILOTA KESKENERAISET PERUSOPEUTS
//TODO: PERUSOPETUKSEN TITLEN KAIVAMINEN
