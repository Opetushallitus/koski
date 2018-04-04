package fi.oph.koski.omattiedot

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.OppijaEditorModel.oppilaitoksenOpiskeluoikeudetOrdering
import fi.oph.koski.editor._
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema.PerusopetuksenOpiskeluoikeus._
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.Hidden
import fi.oph.koski.util.Timing
import mojave.traversal

object OmatTiedotEditorModel extends Timing {
  def toEditorModel(oppija: Oppija)(implicit application: KoskiApplication, koskiSession: KoskiSession): EditorModel = timed("createModel") {
    val view = OmatTiedotEditorView(oppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot], oppija.opiskeluoikeudet.groupBy(_.getOppilaitosOrKoulutusToimija).map {
      case (oppilaitos, opiskeluoikeudet) => OppijaEditorModel.toOppilaitoksenOpiskeluoikeus(oppilaitos, opiskeluoikeudet)
    }.toList.sorted(oppilaitoksenOpiskeluoikeudetOrdering))
    buildModel(view)
  }

  def buildModel(obj: AnyRef)(implicit application: KoskiApplication, koskiSession: KoskiSession): EditorModel = {
    EditorModelBuilder.buildModel(EditorSchema.deserializationContext, obj, editable = false)(koskiSession, application.koodistoViitePalvelu, application.localizationRepository)
  }

  def piilotaArvosanatKeskeneräisistäSuorituksista(oppija: Oppija) = {
    val keskeneräisetTaiLiianÄskettäinVahvistetut = traversal[Suoritus].filter { s =>
      s.vahvistus.isEmpty || !s.vahvistus.exists { v => v.päivä.plusDays(4).isBefore(LocalDate.now())}
    }.compose(päätasonSuorituksetTraversal)
    val piilotettavatOppiaineidenArvioinnit = (oppimääränArvioinnitTraversal ++ vuosiluokanArvioinnitTraversal ++ oppiaineenOppimääränArvioinnitTraversal).compose(keskeneräisetTaiLiianÄskettäinVahvistetut)
    val piilotettavaKäyttäytymisenArviointi = käyttäytymisenArviointiTraversal.compose(keskeneräisetTaiLiianÄskettäinVahvistetut)

    List(piilotettavaKäyttäytymisenArviointi, piilotettavatOppiaineidenArvioinnit).foldLeft(oppija) { (oppija, traversal) =>
      traversal.set(oppija)(None)
    }
  }
}

case class OmatTiedotEditorView(
  @Hidden
  henkilö: TäydellisetHenkilötiedot,
  opiskeluoikeudet: List[OppilaitoksenOpiskeluoikeudet]
)
