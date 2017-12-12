package fi.oph.koski.editor

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.OppijaEditorModel.{aikuistenPerusopetuksenSuoritustenJärjestysKriteeri, perusopetuksenSuoritustenJärjestysKriteeri}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema._
import fi.oph.koski.schema.annotation.Hidden
import fi.oph.koski.util.Timing

object OmatTiedotEditorModel extends Timing {
  def toEditorModel(oppija: Oppija)(implicit application: KoskiApplication, koskiSession: KoskiSession): EditorModel = timed("createModel") {
    import OppijaEditorModel.opiskeluoikeusOrdering
    val view = OmatTiedotEditorView(oppija.henkilö.asInstanceOf[TäydellisetHenkilötiedot], oppija.opiskeluoikeudet.groupBy(_.getOppilaitos).map {
      case (oppilaitos, opiskeluoikeudet) =>
        OppilaitoksenOpiskeluoikeudet(oppilaitos, opiskeluoikeudet.toList.sortBy(_.alkamispäivä).reverse.map {
          case oo: AikuistenPerusopetuksenOpiskeluoikeus => oo.copy(suoritukset = oo.suoritukset.sortBy(aikuistenPerusopetuksenSuoritustenJärjestysKriteeri))
          case oo: PerusopetuksenOpiskeluoikeus => oo.copy(suoritukset = oo.suoritukset.sortBy(perusopetuksenSuoritustenJärjestysKriteeri))
          case oo: AmmatillinenOpiskeluoikeus => oo.copy(suoritukset = oo.suoritukset.sortBy(_.alkamispäivä).reverse)
          case oo: Any => oo
        })
    }.toList.sortBy(_.opiskeluoikeudet(0).alkamispäivä).reverse)
    buildModel(view)
  }

  def buildModel(obj: AnyRef)(implicit application: KoskiApplication, koskiSession: KoskiSession): EditorModel = {
    EditorModelBuilder.buildModel(EditorSchema.deserializationContext, obj, editable = false)(koskiSession, application.koodistoViitePalvelu, application.localizationRepository)
  }
}

case class OmatTiedotEditorView(
  @Hidden
  henkilö: TäydellisetHenkilötiedot,
  opiskeluoikeudet: List[OppilaitoksenOpiskeluoikeudet]
)
