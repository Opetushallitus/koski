package fi.oph.koski.luovutuspalvelu

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.OppilaitoksenOpiskeluoikeudet
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.omattiedot.OmatTiedotEditorModel
import fi.oph.koski.schema.{AmmatillinenTutkintoKoulutus, Koodistokoodiviite, LocalizedString, Opiskeluoikeus}

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

  // Duplicates the logic from web/app/suoritus/Suoritus.js#suoritusTitle
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

  private def emptyIfNotFound(error: HttpStatus) =
    if (error.errors.exists(_.key == KoskiErrorCategory.notFound.key)) {
      Right(SuomiFiResponse(Nil))
    } else {
      Left(error)
    }


  private val ammatillinenOsittainen = Koodistokoodiviite("ammatillinentutkintoosittainen", "suorituksentyyppi")
  private val aikuistenPerusopetus = Koodistokoodiviite("aikuistenperusopetuksenoppimaara", "suorituksentyyppi")
}

case class SuomiFiOpiskeluoikeus(tila: Option[LocalizedString], alku: Option[LocalDate], loppu: Option[LocalDate], nimi: LocalizedString)
case class SuomiFiOppilaitos(nimi: LocalizedString, opiskeluoikeudet: List[SuomiFiOpiskeluoikeus])
case class SuomiFiResponse(oppilaitokset: List[SuomiFiOppilaitos])
