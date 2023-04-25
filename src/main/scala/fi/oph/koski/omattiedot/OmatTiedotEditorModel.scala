package fi.oph.koski.omattiedot

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.OppijaEditorModel.oppilaitoksenOpiskeluoikeudetOrdering
import fi.oph.koski.editor._
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.huoltaja.Huollettava
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.Hidden
import fi.oph.koski.util.{Timing, WithWarnings}

object OmatTiedotEditorModel extends Timing {

  def toEditorModel(oppija: Oppija)(implicit application: KoskiApplication, koskiSession: KoskiSpecificSession): EditorModel =
    toEditorModel(userOppija = oppija, näytettäväOppija = oppija, warnings = Nil)

  def toEditorModel(userOppija: WithWarnings[Oppija], näytettäväOppija: WithWarnings[Oppija])(implicit application: KoskiApplication, koskiSession: KoskiSpecificSession): EditorModel =
    toEditorModel(userOppija.getIgnoringWarnings, näytettäväOppija.getIgnoringWarnings, warnings = näytettäväOppija.warnings)

  def toEditorModel(userOppija: Oppija, näytettäväOppija: Oppija, warnings: Seq[HttpStatus])(implicit application: KoskiApplication, koskiSession: KoskiSpecificSession): EditorModel = timed("createModel") {
    buildModel(buildView(userOppija, näytettäväOppija, warnings))
  }

  def opiskeluoikeudetOppilaitoksittain(oppija: Oppija): List[OppilaitoksenOpiskeluoikeudet] = {
    oppija.opiskeluoikeudet.groupBy(_.getOppilaitosOrKoulutusToimija).map {
      case (oppilaitos, opiskeluoikeudet) => OppijaEditorModel.toOppilaitoksenOpiskeluoikeus(oppilaitos, opiskeluoikeudet)
    }.toList.sorted(oppilaitoksenOpiskeluoikeudetOrdering)
  }

  private def buildView(userOppija: Oppija, näytettäväOppija: Oppija, warnings: Seq[HttpStatus])(implicit application: KoskiApplication, koskiSession: KoskiSpecificSession) = {
    val huollettavat = koskiSession.huollettavat

    OmatTiedotEditorView(
      henkilö = näytettäväOppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot],
      userHenkilö = userOppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot],
      opiskeluoikeudet = opiskeluoikeudetOppilaitoksittain(näytettäväOppija),
      huollettavat = huollettavat.getOrElse(Nil),
      varoitukset = warnings.flatMap(_.errors).map(_.key).toList ++ huollettavat.left.map(_.errors.map(_.key)).left.getOrElse(Nil)
    )
  }

  private def buildModel(obj: AnyRef)(implicit application: KoskiApplication, koskiSession: KoskiSpecificSession): EditorModel = {
    EditorModelBuilder.buildModel(EditorSchema.deserializationContext, obj, editable = false)(koskiSession, application.koodistoViitePalvelu, application.koskiLocalizationRepository)
  }
}

case class OmatTiedotEditorView(
  @Hidden
  henkilö: TäydellisetHenkilötiedot,
  @Hidden
  userHenkilö: TäydellisetHenkilötiedot,
  opiskeluoikeudet: List[OppilaitoksenOpiskeluoikeudet],
  huollettavat: List[Huollettava],
  @Hidden
  varoitukset: List[String]
)

